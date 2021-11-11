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

package io.shulie.amdb.common.request.agent;

import io.shulie.amdb.common.request.PagingRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author anjone
 * @date 2021/8/17
 */
@Data
@ApiModel("agent实例查询")
public class AmdbAgentInfoQueryRequest extends PagingRequest implements Serializable {
    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appNames;

    /**
     * agent状态
     */
    @ApiModelProperty("agent状态")
    private String agentStatus;

    /**
     * AgentId
     */
    @ApiModelProperty("AgentId")
    private String agentId;

    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    private Long startDate;

    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    private Long endDate;

    /**
     * agent日志模糊匹配
     */
    private String agentInfo;
}

