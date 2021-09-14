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

import io.shulie.amdb.adaptors.starter.ClientAdaptorStarter;
import io.shulie.amdb.mapper.TAmdbAgentConfigDOMapper;
import io.shulie.amdb.service.AppInstanceService;
import io.shulie.amdb.service.AppInstanceStatusService;
import io.shulie.amdb.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AdaptorConfiguration {

    @Value("${zookeeper.server}")
    private String zkPath;
    @Autowired
    private AppService appService;
    @Autowired
    private AppInstanceService appInstanceService;
    @Autowired
    private AppInstanceStatusService appInstanceStatusService;
    @Autowired
    private TAmdbAgentConfigDOMapper agentConfigDOMapper;

    @Bean
    public ClientAdaptorStarter adaptorStarter() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("appService", appService);
        config.put("appInstanceService", appInstanceService);
        config.put("appInstanceStatusService", appInstanceStatusService);
        config.put("agentConfigDOMapper", agentConfigDOMapper);
        System.setProperty("zookeeper.servers", zkPath);
        return new ClientAdaptorStarter(config);
    }
}
