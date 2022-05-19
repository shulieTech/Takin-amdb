package io.shulie.amdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pamirs.pradar.remoting.protocol.CommandCode;
import io.shulie.amdb.common.request.trodata.LogCompensateCallbackRequest;
import io.shulie.amdb.service.log.PushLogService;
import io.shulie.amdb.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Sunsy
 * @date 2022/5/19
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Service
@Slf4j
public class SurgePushLogServiceImpl implements PushLogService {

    private CloseableHttpClient httpClient = null;

    /**
     * 向AMDB推送数据
     *
     * @param data    数据
     * @param version 版本
     */
    @Override
    public void pushLogToAmdb(byte[] data, String version, String address) throws Exception {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(address);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("time", String.valueOf(System.currentTimeMillis()));
            httpPost.setHeader("dataType", "1");
            httpPost.setHeader("version", version);
            httpPost.setHeader("hostIp", InetAddress.getLocalHost().getHostAddress());
            httpPost.setHeader("Accept-Encoding", "gzip,deflate,sdch");

            ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data);
            GzipCompressingEntity gzipEntity = new GzipCompressingEntity(byteArrayEntity);
            httpPost.setEntity(gzipEntity);
            response = httpClient.execute(httpPost);
            if (response == null) {
                throw new IllegalStateException("push log failed,response is null.");
            }

            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != HttpStatus.SC_OK) {
                throw new IllegalStateException("push log failed,code is incorrect.");
            }
            String content = EntityUtils.toString(response.getEntity());
            if (StringUtils.isBlank(content)) {
                throw new IllegalStateException("push log failed,response is empty.");
            }
            JSONObject jsonObject = JSON.parseObject(content);
            Integer responseCode = jsonObject.getInteger("responseCode");
            if (responseCode == CommandCode.SUCCESS) {
                return;
            }
        } catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("http log push error", e);
            }
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e1) {
                    log.error("callback consume response entity exception", e);
                }
            }
            throw e;
        }
    }

    @Override
    public void callbackTakin(String callbackUrl, LogCompensateCallbackRequest request) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(callbackUrl);
            httpPost.setHeader("Content-Type", "application/json");

            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(request)));
            response = httpClient.execute(httpPost);
            if (response == null) {
                log.warn("callbackTakin failed,response is null.");
            }

            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != HttpStatus.SC_OK) {
                log.warn("callbackTakin failed,code is incorrect.");
            }
            String content = EntityUtils.toString(response.getEntity());
            if (StringUtils.isBlank(content)) {
                log.warn("callbackTakin failed,response is empty.");
            }

            if ("true".equals(StringUtil.parseStr(JSONObject.parseObject(content).get("success")))) {
                log.info("takin-web回调成功:{}", callbackUrl);
            }
        } catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("callbackTakin error", e);
            }
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e1) {
                    log.error("callback consume response entity exception", e);
                }
            }
        }
    }


    @Override
    @PostConstruct
    public boolean init() {
        try {
            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            // 总连接池数量
            connectionManager.setMaxTotal(10);
            connectionManager.setDefaultMaxPerRoute(10);
            // setConnectTimeout表示设置建立连接的超时时间
            // setConnectionRequestTimeout表示从连接池中拿连接的等待超时时间
            // setSocketTimeout表示发出请求后等待对端应答的超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(3000)
                    .setConnectionRequestTimeout(3000)
                    .setSocketTimeout(3000)
                    .build();
            // 重试处理器，StandardHttpRequestRetryHandler这个是官方提供的，看了下感觉比较挫，很多错误不能重试，可自己实现HttpRequestRetryHandler接口去做
            HttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler();

            httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .setRetryHandler(retryHandler)
                    .build();

            // 服务端假设关闭了连接，对客户端是不透明的，HttpClient为了缓解这一问题，在某个连接使用前会检测这个连接是否过时，如果过时则连接失效，但是这种做法会为每个请求
            // 增加一定额外开销，因此有一个定时任务专门回收长时间不活动而被判定为失效的连接，可以某种程度上解决这个问题
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        // 关闭失效连接并从连接池中移除
                        connectionManager.closeExpiredConnections();
                        // 关闭60秒钟内不活动的连接并从连接池中移除，空闲时间从交还给连接管理器时开始
                        connectionManager.closeIdleConnections(60, TimeUnit.SECONDS);
                    } catch (Throwable t) {
                        log.error("closeExpiredConnections error", t);
                    }
                }
            }, 0, 1000 * 5);

            // jvm 停止或重启时，关闭连接池释放连接资源
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        log.error("HttpClient close exception", e);
                    }
                }
            });
            return true;
        } catch (Throwable e) {
            log.error("httpDataPush init error", e);
            return false;
        }

    }

    @PreDestroy
    @Override
    public void stop() {
        try {
            this.httpClient.close();
        } catch (Throwable e) {
            log.error("close httpClient err!", e);
        }
    }

}
