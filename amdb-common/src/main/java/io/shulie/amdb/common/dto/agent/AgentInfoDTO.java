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

package io.shulie.amdb.common.dto.agent;

import lombok.Data;

import java.io.Serializable;

/**
 * @author anjone
 * @date 2021/8/17
 */
@Data
public class AgentInfoDTO implements Serializable {

    private String agentId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * agentip
     */
    private String ip;

    /**
     * agent端口
     */
    private Integer port;

    private String userAppKey;

    /**
     * agent日志时间
     */
    private Long agentTimestamp;

    /**
     * agent日志
     */
    private String agentInfo;
}
