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

package io.shulie.amdb.common.request.trace;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class TraceStackQueryParam extends AbstractAmdbBaseRequest {
    @ApiModelProperty("调用类型")
    String rpcType;
    @ApiModelProperty("接口名称")
    String serviceName;
    @ApiModelProperty("方法名称")
    String methodName;
    @ApiModelProperty("应用名称")
    String appName;
    @ApiModelProperty("traceId")
    String traceId;
    @ApiModelProperty("rpcId")
    String rpcId;
    @ApiModelProperty("日志类型")
    String logType;
    @ApiModelProperty("查询开始时间")
    String startTime;
    @ApiModelProperty("查询结束时间")
    String endTime;

}