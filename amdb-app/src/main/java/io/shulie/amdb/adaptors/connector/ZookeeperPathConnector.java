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
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.common.zk.ZkClientSpec;
import io.shulie.surge.data.common.zk.ZkPathChildrenCache;
import io.shulie.surge.data.runtime.common.zk.NetflixCuratorZkClientFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ZookeeperPathConnector implements Connector {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperPathConnector.class);

    private static final String ZK_SERVERS = System.getProperty("zookeeper.servers", "default.zookeeper:2181");
    private static final int CONNECTION_TIMEOUT = NumberUtils.toInt(System.getProperty("zookeeper.connection.timeout", "30000"));
    private static final int SESSION_TIMEOUT = NumberUtils.toInt(System.getProperty("zookeeper.session.timeout", "20000"));
    private Map<String, ZkPathChildrenCache> childCache = new ConcurrentHashMap<>();
    private ZkClient zkClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(10, new CustomizableThreadFactory("zk-path-connector-"));

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
        this.zkClient = new NetflixCuratorZkClientFactory().create(spec);

        if (this.zkClient == null) {
            logger.error("ZooKeeper client initialization failed. Please check if the ZooKeeper cluster connection is correct.");
        }
    }

    @Override
    public <T> void addPath(String path, Class<T> paramsClazz, Processor processor) throws Exception {
        createIfNotExistsDirectory(path);
        // 已处理过的path进来后不再处理
        if (childCache.get(path) != null) {
            return;
        }
        ZkPathChildrenCache instanceCache = zkClient.createPathChildrenCache(path);
        instanceCache.setUpdateExecutor(executor);
        childCache.put(path, instanceCache);

        // 初始化时先触发一次回调处理
        processDataChanges(path, paramsClazz, processor);

        // 设置监听器，当子节点发生变化时触发回调处理
        instanceCache.setUpdateListener(() -> processDataChanges(path, paramsClazz, processor));
        instanceCache.startAndRefresh();
    }

    @Override
    public List<String> getChildrenPath(String path) throws Exception {
        createIfNotExistsDirectory(path);
        return zkClient.listChildren(path);
    }

    // 创建指定路径，如果路径不存在的话
    private void createIfNotExistsDirectory(String path) {
        try {
            zkClient.ensureDirectoryExists(path);
        } catch (Exception e) {
            logger.error("Failed to create directory if not exists.", e);
        }
    }

    // 处理子节点的数据变化
    private <T> void processDataChanges(String path, Class<T> paramsClazz, Processor processor) {
        DataContext dataContext = processor.getContext();
        dataContext.setPath(path);
        try {
            List<String> childPaths = zkClient.listChildren(path);
            dataContext.setChildPaths(childPaths);

            for (String childPath : childPaths) {
                String childFullPath = path + "/" + childPath;
                byte[] childDataBytes = zkClient.getData(childFullPath);
                if (childDataBytes != null) {
                    String childData = new String(childDataBytes, StandardCharsets.UTF_8);
                    if (childData != null) {
                        Object object = JSON.parseObject(childData, paramsClazz);
                        dataContext.setModel(object);
                        processor.process(dataContext);
                    }
                }
            }
        } catch (KeeperException.NoNodeException e) {
            logger.warn("Node is offline: {}", path);
            // 执行删除表中历史节点数据操作
            processor.process(dataContext);

            if (childCache.get(path) != null) {
                childCache.get(path).stop();
                childCache.remove(path);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while processing data: {}", e.getMessage(), e);
        }
    }

    @Override
    public void start() throws Exception {
        // 启动操作，根据需要实现
    }

    @Override
    public boolean close() throws Exception {
        for (ZkPathChildrenCache instanceCache : childCache.values()) {
            try {
                instanceCache.stop();
            } catch (Exception e) {
                logger.error("Failed to stop instance cache: {}", e.getMessage(), e);
            }
        }
        // 清空子节点缓存
        childCache.clear();
        return true;
    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.ZOOKEEPER_PATH;
    }
}



