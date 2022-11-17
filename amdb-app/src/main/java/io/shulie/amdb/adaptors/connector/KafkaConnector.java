package io.shulie.amdb.adaptors.connector;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Queues;
import io.shulie.amdb.adaptors.instance.InstanceAdaptor;
import io.shulie.amdb.adaptors.instance.InstanceStatusAdaptor;
import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.surge.data.common.utils.Bytes;
import io.shulie.takin.sdk.kafka.MessageReceiveCallBack;
import io.shulie.takin.sdk.kafka.MessageReceiveService;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class KafkaConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private MessageReceiveService messageReceiveService;

    Set<String> pathCache = new HashSet<>();

    Map<String, String> pathTopicMap = new HashMap<>();


    @Override
    public boolean close() throws Exception {
        messageReceiveService.stop();
        return true;
    }

    @Override
    public void init() throws Exception {
        messageReceiveService = new KafkaSendServiceFactory().getKafkaMessageReceiveInstance();
        pathTopicMap.put(InstanceStatusAdaptor.INSTANCE_STATUS_PATH, "stress-test-config-log-pradar-status");
        pathTopicMap.put(InstanceAdaptor.INSTANCE_PATH, "stress-test-config-log-pradar-client");
    }

    @Override
    public void init(int... ports) throws Exception {
        throw new UnsupportedOperationException("Method is not supported.");
    }

    @Override
    public void init(String zookeepers, int connectionTimeout, int sessionTimeout) throws Exception {
        throw new UnsupportedOperationException("Method is not supported.");
    }

    @Override
    public <T> void addPath(String path, Class<T> paramsClazz, Processor processor) throws Exception {
        // 已处理过的path进来后不再处理
        if (pathCache.contains(path)) {
            return;
        } else {
            pathCache.add(path);
        }

        if (!pathTopicMap.containsKey(path)){
            return;
        }

        // 只触发最新的一次 Update 更新
        final NamedThreadFactory threadFactory = new NamedThreadFactory("KafkaConnector-" + path, true);
        ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                Queues.<Runnable>newArrayBlockingQueue(1), threadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.execute(() -> {
            messageReceiveService.receive(ListUtil.of(pathTopicMap.get(path)), new MessageReceiveCallBack() {
                @Override
                public void success(MessageEntity messageEntity) {
                    Map entityBody = messageEntity.getBody();
                    Object appName = entityBody.get("appName");
                    Object agentId = entityBody.get("agentId");
                    if (appName == null) {
                        logger.warn("接收到的节点信息应用名为空，数据出现问题，接收到的数据为数据为:{}", JSON.toJSONString(entityBody));
                        return;
                    }
                    if (agentId == null) {
                        logger.warn("接收到的节点信息agentId为空，数据出现问题，接收到的数据为数据为:{}", JSON.toJSONString(entityBody));
                        return;
                    }

                    String instancePath = path + "/" + appName + "/" + agentId;
                    DataContext dataContext = processor.getContext();
                    dataContext.setPath(instancePath);

                    String body = JSON.toJSONString(entityBody);
                    Object object = JSON.parseObject(body, paramsClazz);
                    dataContext.setModel(object);
                    processor.process(dataContext);
                }

                @Override
                public void fail(String errorMessage) {
                    logger.error("节点信息接收kafka消息出现异常，errorMessage:{}", errorMessage);
                }
            });
        });
    }

    @Override
    public List<String> getChildrenPath(String path) throws Exception {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.KFK_CONSUMER;
    }
}
