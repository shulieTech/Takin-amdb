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

package io.shulie.amdb.service.impl;

import java.util.List;

import javax.annotation.Resource;

import io.shulie.amdb.entity.ConfigurationDO;
import io.shulie.amdb.request.query.ClickhouseCreateTableRequest;
import io.shulie.amdb.request.query.MiddlewareQueryRequest;
import io.shulie.amdb.response.app.model.Configuration;
import io.shulie.amdb.service.ConfigurationService;
import io.shulie.amdb.service.MiddlewareConfigService;
import io.shulie.surge.config.clickhouse.ClickhouseClusterConfigEntity;
import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MiddlewareConfigServiceImpl implements MiddlewareConfigService {

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private ClickhouseTemplateManager clickhouseTemplateManager;

    // 现在这种设计自己拼接sql条件
    @Override
    public List<Configuration> queryClusterConfig(MiddlewareQueryRequest request) {
        ConfigurationDO queryDo = new ConfigurationDO();
        queryDo.setAvailableEnv("prod".equals(request.getEnvCode()) ? 1 : 0);
        return configurationService.query(queryDo, false);
    }

    @Override
    public void createClickhouseClusterTable(ClickhouseCreateTableRequest request) throws Exception {
        ClickhouseClusterConfigEntity entity = new ClickhouseClusterConfigEntity();
        entity.setTenantName(request.getTenantName());
        entity.setUserAppKey(request.getTenantAppKey());
        entity.setEnvCode(request.getEnvCode());
        entity.setClusterAddress(request.getClusterAddress());
        entity.setUserName(request.getUserName());
        entity.setPassword(request.getPassword());
        entity.setTtl(request.getTtl());
        // 委托clickhouseTemplateManager执行建表职责
        clickhouseTemplateManager.createTableByScriptFile(entity);
    }
}
