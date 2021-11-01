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

import io.shulie.surge.data.deploy.pradar.link.processor.LinkProcessor;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

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
    public ClickHouseSupport clickHouseSupport(@Value("${config.clickhouse.url}") String clickhouseUrl,
                                               @Value("${config.clickhouse.username}") String clickhouseUserName,
                                               @Value("${config.clickhouse.password}") String clickhousePassword) {
        return new ClickHouseSupport(clickhouseUrl, clickhouseUserName, clickhousePassword, 200, false);
    }


    @Bean
    public LinkProcessor linkProcessor(@Autowired ClickHouseSupport clickHouseSupport,
                                       @Autowired MysqlSupport mysqlSupport,
                                       @Value("${datasource.traceAll}") String dataSourceType) {

        LinkProcessor linkProcessor = new LinkProcessor();

        try {
            Field mysqlSupportField = LinkProcessor.class.getDeclaredField("mysqlSupport");
            mysqlSupportField.setAccessible(true);
            mysqlSupportField.set(linkProcessor, mysqlSupport);

            Field clickHouseSupportFile = LinkProcessor.class.getDeclaredField("clickHouseSupport");
            clickHouseSupportFile.setAccessible(true);
            clickHouseSupportFile.set(linkProcessor, clickHouseSupport);

            linkProcessor.setDataSourceType(dataSourceType);

            Field traceQuerylimit = LinkProcessor.class.getDeclaredField("traceQuerylimit");
            traceQuerylimit.setAccessible(true);
            traceQuerylimit.set(linkProcessor, "");

        } catch (Throwable e) {
            throw new RuntimeException("init linkProcessor error", e);
        }

        return linkProcessor;
    }
}
