package io.shulie.amdb.utils;

import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author zhangz
 * Created on 2024/6/13 下午4:53
 * Email: zz052831@163.com
 */

public class AuthZkConnection extends ZkConnection {
    private ZooKeeper zooKeeper;

    public AuthZkConnection(String zkServers, int sessionTimeout, String username, String password) throws Exception {
        super(zkServers, sessionTimeout);
        this.zooKeeper = new ZooKeeper(zkServers, sessionTimeout, event -> {
        });
        this.zooKeeper.addAuthInfo("digest", (username + ":" + password).getBytes());
    }

    @Override
    public ZooKeeper getZookeeper() {
        return this.zooKeeper;
    }
}
