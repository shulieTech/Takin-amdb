/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.shulie.amdb.adaptors.connector;

import com.alibaba.fastjson.JSON;
import io.shulie.surge.data.common.utils.Bytes;
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.common.zk.ZkClientSpec;
import io.shulie.surge.data.common.zk.ZkNodeCache;
import io.shulie.surge.data.runtime.common.zk.NetflixCuratorZkClientFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ZookeeperNodeConnector implements Connector {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNodeConnector.class);
    private Map<String, ZkNodeCache> nodeCache = new ConcurrentHashMap<>();

    private static final String ZK_SERVERS = System.getProperty("zookeeper.servers", "default.zookeeper:2181");
    private static final int CONNECTION_TIMEOUT = NumberUtils.toInt(System.getProperty("zookeeper.connection.timeout", "30000"));
    private static final int SESSION_TIMEOUT = NumberUtils.toInt(System.getProperty("zookeeper.session.timeout", "20000"));

    private ZkClient zkClient;

    @Value("${zk_node_thread_num:10}")
    private int thread_num;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public void init() throws Exception {
        init(ZK_SERVERS, CONNECTION_TIMEOUT, SESSION_TIMEOUT);
    }

    @Override
    public void init(int... ports) throws Exception {
        throw new UnsupportedOperationException("Method is not supported.");
    }

    @Override
    public void init(String zookeepers, int connectionTimeout, int sessionTimeout) throws Exception {
        ZkClientSpec spec = new ZkClientSpec(zookeepers);
        spec.setConnectionTimeoutMillis(connectionTimeout)
                .setSessionTimeoutMillis(sessionTimeout);
        try {
            this.zkClient = new NetflixCuratorZkClientFactory().create(spec);
        } catch (Exception e) {
            logger.error("zookeeper客户端初始化异常,请检查ZK集群连接是否正常", e);
        }
        if (this.zkClient == null) {
            logger.error("zookeeper客户端初始化异常,请检查ZK集群连接是否正常");
        }
    }

    @Override
    public <T> void addPath(String path, Class<T> paramsClazz, Processor processor) throws Exception {
        try {
            createIfNotExistsDirectory(path);
            if (nodeCache.get(path) != null) {
                return;
            }
            ZkNodeCache zkNodeCache = zkClient.createZkNodeCache(path, false);
            zkNodeCache.setUpdateExecutor(executor);
            nodeCache.put(path, zkNodeCache);

            // 回调处理
            DataContext dataContext = processor.getContext();
            dataContext.setPath(path);
            Thread callThread = new Thread(() -> {
                try {
                    String body = Bytes.toString(zkClient.getData(path));
                    Object object = JSON.parseObject(body, paramsClazz);
                    dataContext.setModel(object);
                    processor.process(dataContext);
                } catch (KeeperException.NoNodeException e) {
                    logger.warn("节点下线:{}", path);
                    processor.process(dataContext);
                    if (nodeCache.get(path) != null) {
                        nodeCache.get(path).stop();
                        nodeCache.remove(path);
                    }
                } catch (Exception e) {
                    logger.error("processor处理发生异常:{},异常堆栈:{}，path:{}，dataContext:{}", e, e.getStackTrace(), path, dataContext);
                }
            });

            callThread.start();
            zkNodeCache.setUpdateListener(callThread);
            zkNodeCache.startAndRefresh();
        } catch (Exception e) {
            throw new RuntimeException("fail to start heartbeat node for path:" + path, e);
        }
    }

    @Override
    public List<String> getChildrenPath(String path) throws Exception {
        createIfNotExistsDirectory(path);
        return zkClient.listChildren(path);
    }

    private void createIfNotExistsDirectory(String path) {
        try {
            zkClient.ensureDirectoryExists(path);
        } catch (Exception e) {
            logger.error("createIfNotExistsDirectory err!", e);
        }
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public boolean close() throws Exception {
        for (ZkNodeCache zkNodeCache : nodeCache.values()) {
            try {
                zkNodeCache.stop();
            } catch (Exception e) {
                logger.error("instance cache start failed");
            }
        }
        nodeCache.clear();

        // 关闭线程池
        if (executor != null) {
            executor.shutdownNow();
        }
        return true;
    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.ZOOKEEPER_NODE;
    }
}