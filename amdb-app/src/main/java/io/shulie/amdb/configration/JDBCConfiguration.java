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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * @author anjone
 * @date 2021/9/1
 */
@Configuration
public class JDBCConfiguration {

    @Lazy
    @Primary
    @Bean(name = "mysqlJdbcTemplate")
    //dataSource 是spring自动加载的
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("amdbDataSource") DataSource dataSource) throws Exception {
        return new JdbcTemplate(dataSource);
    }

    @Lazy
    @Bean(name = "clickhouseJdbcTemplate")
    public JdbcTemplate clickhouseJdbcTemplate(
            @Value("${config.clickhouse.url}") String clickhouseUrl,
            @Value("${config.clickhouse.username}") String clickhouseUserName,
            @Value("${config.clickhouse.password}") String clickhousePassword
    ) throws Exception {

        Objects.requireNonNull(clickhouseUrl);
        Objects.requireNonNull(clickhouseUserName);
        Objects.requireNonNull(clickhousePassword);

        // create clickhouse JDBCTemplate
        ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
        if (StringUtils.isNotBlank(clickhouseUserName)) {
            clickHouseProperties.setUser(clickhouseUserName);
        }
        if (StringUtils.isNotBlank(clickhousePassword)) {
            clickHouseProperties.setPassword(clickhousePassword);
        }
        DataSource clickHouseDataSource = new BalancedClickhouseDataSource(clickhouseUrl, clickHouseProperties);
        return new JdbcTemplate(clickHouseDataSource);
    }

}
