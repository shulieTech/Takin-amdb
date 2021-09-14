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

package io.shulie.amdb.request.query;

import io.shulie.amdb.common.request.PagingRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("实例状态查询")
public class AppInstanceStatusQueryRequest extends PagingRequest {
    /**
     * 应用名称 用,分割  eg. name1,name2
     */
    @ApiModelProperty("应用名称 用,分割  eg. name1,name2")
    private String appNames;
    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * IP地址
     */
    @ApiModelProperty("IP地址")
    private String ip;

    /**
     * pid
     */
    @ApiModelProperty("进程号")
    private String pid;

    /**
     * AgentId
     */
    @ApiModelProperty("AgentId")
    private String agentId;

    /**
     * 探针状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)
     */
    @ApiModelProperty("探针状态 0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态")
    private String probeStatus;

    /**
     * agent状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)
     */
    @ApiModelProperty("agent状态 0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态")
    private String agentStatus;

    /**
     * 查询条件 更新时间下限
     */
    @ApiModelProperty("查询条件 更新时间下限")
    private Long minUpdateDate;
}
