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

package io.shulie.amdb.common.dto.link.entrance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ExitInfoDTO {
    @ApiModelProperty("应用名称(入口为客户端应用名称,出口为服务端应用名称)")
    String appName;
    @ApiModelProperty("接口名称")
    String serviceName;
    @ApiModelProperty("方法名称")
    String methodName;
    @ApiModelProperty("中间件名称")
    String middlewareName;
    @ApiModelProperty("中间件详细名称")
    String middlewareDetail;
    @ApiModelProperty("调用类型")
    String rpcType;
    @ApiModelProperty("扩展信息")
    String extend;
    @ApiModelProperty("客户端应用名称,针对入口(服务端日志)")
    String upAppName;
    @ApiModelProperty("服务端应用名称,针对出口(客户端日志)")
    String downAppName;
    @ApiModelProperty("默认白名单信息")
    String defaultWhiteInfo;
}