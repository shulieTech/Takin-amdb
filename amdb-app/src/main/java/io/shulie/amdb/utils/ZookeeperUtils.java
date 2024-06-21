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

package io.shulie.amdb.utils;

import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.common.zk.impl.NetflixCuratorZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@Slf4j
//@Component
public class ZookeeperUtils {
    private ZkClient zkClient;

    @Value("${config.zk.servers}")
    String zkServers;
    @Value("${config.zk.sessionTimeout}")
    int sessionTimeout;
    @Value("${config.zk.connectionTimeout}")
    int connectionTimeout;
    @Value("${zookeeper.session.digest.enabled: false}")
    boolean isDigestEnabled;
    @Value("${zookeeper.session.digest.username:admin}")
    String username;
    @Value("${zookeeper.session.digest.password:Shulie@2020}")
    String password;
    @PostConstruct
    public void init() {
        try {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(zkServers)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .connectionTimeoutMs(sessionTimeout)
                    .sessionTimeoutMs(connectionTimeout)
                    .threadFactory(new NamedThreadFactory("curator", true));
            if (isDigestEnabled){
                builder.authorization("digest", (username + ":" + password).getBytes());
            }
            CuratorFramework client = builder.build();
            client.start();

            zkClient = new NetflixCuratorZkClient(client, zkServers);
        } catch (Exception e) {
            log.error("init zk error", e);
        }
    }

    public ZkClient getZkClient() {
        return zkClient;
    }
}
