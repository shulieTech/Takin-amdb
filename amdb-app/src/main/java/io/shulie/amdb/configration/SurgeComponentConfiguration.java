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

package io.shulie.amdb.configration;

import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import io.shulie.surge.data.deploy.pradar.link.processor.LinkProcessor;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Properties;

import static io.shulie.surge.config.clickhouse.ClickhouseTemplateManager.*;

/**
 * @author anjone
 * @date 2021/9/1
 */
@Configuration
public class SurgeComponentConfiguration {


    @Bean
    public MysqlSupport mysqlSupport(@Value("${spring.datasource.url}") String url,
                                     @Value("${spring.datasource.username}") String userName,
                                     @Value("${spring.datasource.password}") String password) {
        return new MysqlSupport(url, userName, password, 1, 1, 20);
    }

    @Bean
    public LinkProcessor linkProcessor(@Autowired ClickhouseTemplateManager clickhouseTemplateManager,
                                       @Autowired MysqlSupport mysqlSupport,
                                       @Value("${datasource.traceAll}") String dataSourceType) {

        LinkProcessor linkProcessor = new LinkProcessor();

        try {
            Field mysqlSupportField = LinkProcessor.class.getDeclaredField("mysqlSupport");
            mysqlSupportField.setAccessible(true);
            mysqlSupportField.set(linkProcessor, mysqlSupport);

            Field clickhouseTemplateManagerField = LinkProcessor.class.getDeclaredField("clickhouseTemplateManager");
            clickhouseTemplateManagerField.setAccessible(true);
            clickhouseTemplateManagerField.set(linkProcessor, clickhouseTemplateManager);

            linkProcessor.setDataSourceType(dataSourceType);

            Field traceQuerylimit = LinkProcessor.class.getDeclaredField("traceQuerylimit");
            traceQuerylimit.setAccessible(true);
            traceQuerylimit.set(linkProcessor, "");

        } catch (Throwable e) {
            throw new RuntimeException("init linkProcessor error", e);
        }

        return linkProcessor;
    }

    @Bean
    public ClickhouseTemplateManager clickhouseTemplateManager(MysqlSupport mysqlSupport,
        @Autowired @Qualifier("clickhouseConfig") Properties clickhouseConfig,
        @Autowired @Qualifier("troWebConfig") Properties troWebConfig,
        @Value("${config.script-path.clickhouse:}") String scriptFilePath,
        @Value("${datasource.traceAll:clickhouse}") String dataSourceType
    ) {
        Properties properties = new Properties();
        properties.putAll(clickhouseConfig);
        properties.putAll(troWebConfig);
        ClickhouseTemplateManager clickhouseTemplateManager = new ClickhouseTemplateManager(mysqlSupport, dataSourceType);
        if (StringUtils.isBlank(scriptFilePath)) {
            scriptFilePath = SurgeComponentConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "config";
        }
        clickhouseTemplateManager.setFileParentPath(scriptFilePath);
        clickhouseTemplateManager.init(properties);
        return clickhouseTemplateManager;
    }

    @Bean
    public Properties clickhouseConfig(@Value("${config.clickhouse.url:}") String clickhouseUrl,
        @Value("${config.clickhouse.username:}") String clickhouseUserName,
        @Value("${config.clickhouse.password:}") String clickhousePassword
    ) {
        Properties properties = new Properties();
        properties.put(CONFIG_CLICKHOUSE_URL, clickhouseUrl);
        properties.put(CONFIG_CLICKHOUSE_USER_NAME, clickhouseUserName);
        properties.put(CONFIG_CLICKHOUSE_PASSWORD, clickhousePassword);
        return properties;
    }

    @Bean
    public Properties troWebConfig(@Value("${tro.url.ip:}") String troIp, @Value("${tro.port:}") String troPort,
        @Value("${tro.api.config.path:}") String troCkClusterPath
    ) {
        Properties properties = new Properties();
        properties.put(TRO_IP, troIp);
        properties.put(TRO_PORT, troPort);
        properties.put(TRO_CONFIG_PATH, troCkClusterPath);
        return properties;
    }
}
