package io.shulie.amdb.run;

import java.io.InputStream;
import java.util.Properties;

import com.alibaba.fastjson.JSON;

import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.common.zk.ZkClient.CreateMode;
import io.shulie.surge.data.common.zk.ZkClientSpec;
import io.shulie.surge.data.runtime.common.zk.NetflixCuratorZkClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VersionRegister implements ApplicationListener<ApplicationStartedEvent> {

    private static final String REGISTER_PATH = "/pradar/config/version/amdb";

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        String zk_servers = System.getProperty("zookeeper.servers", "default.zookeeper:2181");
        int connection_timeout = NumberUtils.toInt(System.getProperty("zookeeper.connection.timeout", "30000"));
        int session_timeout = NumberUtils.toInt(System.getProperty("zookeeper.session.timeout", "20000"));
        ZkClientSpec spec = new ZkClientSpec(zk_servers);
        spec.setConnectionTimeoutMillis(connection_timeout).setSessionTimeoutMillis(session_timeout);
        try {
            ZkClient zkClient = new NetflixCuratorZkClientFactory().create(spec);
            registerVersion(zkClient);
        } catch (Exception e) {
            log.error("注册版本信息异常", e);
        }
    }

    private void registerVersion(ZkClient zkClient) throws Exception {
        zkClient.deleteQuietly(REGISTER_PATH);
        zkClient.ensureParentExists(REGISTER_PATH);
        zkClient.createNode(REGISTER_PATH, JSON.toJSONBytes(readGitVersion()), CreateMode.PERSISTENT);
    }

    public static Properties readGitVersion() {
        Properties properties = new Properties();
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        if (resource.exists()) {
            try (InputStream stream = resource.getInputStream()) {
                properties.load(stream);
            } catch (Exception ignore) {
            }
        }
        return properties;
    }
}
