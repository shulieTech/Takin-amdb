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

package io.shulie.amdb.common.request.link;

import io.shulie.amdb.common.request.PagingRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("入参类-出入口查询")
public class ExitQueryParam extends PagingRequest {
    @ApiModelProperty(value = "查询服务类型:0-入口,1-出口", required = true)
    String queryTye;
    @ApiModelProperty("应用名称(查询入口时传入服务端应用,查询出口时传入客户端应用名)")
    String appName;
    @ApiModelProperty("客户端应用名称,针对入口(服务端日志)")
    String upAppName;
    @ApiModelProperty("调用类型")
    String rpcType;
    @ApiModelProperty("服务名")
    String serviceName;
    @ApiModelProperty("方法名")
    String methodName;
    @ApiModelProperty("中间件名称:用逗号分割可查询多个中间件,中间件名称为大写")
    String middlewareName;
    @ApiModelProperty(value = "需返回的字段列表", required = true)
    String fieldNames;
    @ApiModelProperty("默认白名单标识,不传或者传入false查询正常出口,true为查询默认白名单")
    boolean defaultWhiteFlag;

    public boolean isDefaultWhiteFlag() {
        return defaultWhiteFlag;
    }

    public void setDefaultWhiteFlag(boolean defaultWhiteFlag) {
        this.defaultWhiteFlag = defaultWhiteFlag;
    }
}
