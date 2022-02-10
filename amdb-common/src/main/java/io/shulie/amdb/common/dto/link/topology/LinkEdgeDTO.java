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

package io.shulie.amdb.common.dto.link.topology;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("链路拓扑图点")
public class LinkEdgeDTO {
    @ApiModelProperty("上游节点唯一标识")
    String sourceId;
    @ApiModelProperty("下游节点唯一标识")
    String targetId;
    @ApiModelProperty("边唯一标识")
    String eagleId;
    @ApiModelProperty("边调用类型")
    String eagleType;
    @ApiModelProperty("边调用类型分组(大类)")
    String eagleTypeGroup;
    @ApiModelProperty("上游应用名称")
    String serverAppName;
    //Object extendInfo;
    @ApiModelProperty("服务名")
    String service;
    @ApiModelProperty("方法")
    String method;
    @ApiModelProperty("扩展信息")
    String extend;
    @ApiModelProperty("应用名称")
    String appName;
    @ApiModelProperty("RPC类型")
    String rpcType;
    @ApiModelProperty("日志类型")
    String logType;
    @ApiModelProperty("中间件名称")
    String middlewareName;
}
