package io.shulie.amdb.adaptors.connector;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Queues;
import io.shulie.amdb.adaptors.instance.InstanceAdaptor;
import io.shulie.amdb.adaptors.instance.InstanceStatusAdaptor;
import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.takin.sdk.kafka.MessageReceiveCallBack;
import io.shulie.takin.sdk.kafka.MessageReceiveService;
import io.shulie.takin.sdk.kafka.entity.MessageEntity;
import io.shulie.takin.sdk.kafka.impl.KafkaSendServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class KafkaConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private MessageReceiveService messageReceiveService;

    Set<String> pathCache = new HashSet<>();

    Map<String, String> pathTopicMap = new HashMap<>();

    Set<String> instancePathTimeSet = new HashSet<>(100);

    private String redisPer = "agentInstance:";

    private static JedisPool pool;
    private static Jedis jedis;

    @Override
    public boolean close() throws Exception {
        messageReceiveService.stop();
        instancePathTimeSet.clear();
        pathCache.clear();
        return true;
    }

    @Override
    public void init() throws Exception {
        String redisUrl = System.getProperty("redis.url");
        String redisPassword = System.getProperty("redis.password");
        String redisPort = System.getProperty("redis.port", "6379");
        pool = new JedisPool(redisUrl, Integer.parseInt(redisPort));
        jedis = pool.getResource();
        if (redisPassword != null){
            jedis.auth(redisPassword);
        }

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

        if (!pathTopicMap.containsKey(path)) {
            return;
        }

        // 只触发最新的一次 Update 更新
        final NamedThreadFactory threadFactory = new NamedThreadFactory("KafkaConnector-" + path, true);
        ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                Queues.<Runnable>newArrayBlockingQueue(2), threadFactory,
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
                    instancePathTimeSet.add(instancePath);
                    jedis.set(redisPer + instancePath, System.currentTimeMillis() + "");
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

        executor.execute(() -> {
            while (true) {
                long currentTimeMillis = System.currentTimeMillis();
                instancePathTimeSet.forEach(instancePath -> {
                    //连续两分钟没有接收到该节点的信息，认为当前节点已下线
                    String millStr = jedis.get(instancePath);
                    if (millStr == null || currentTimeMillis - Long.parseLong(millStr) > 1000 * 60 * 2){
                        DataContext dataContext = processor.getContext();
                        dataContext.setPath(instancePath);
                        processor.process(dataContext);
                    }
                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
