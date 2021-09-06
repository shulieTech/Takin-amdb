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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LinkCheckResultRequest extends PagingRequest {
    @ApiModelProperty("应用名称")
    String appName;
    @ApiModelProperty("服务名称")
    String serviceName;
    @ApiModelProperty("方法")
    String method;
    @ApiModelProperty("rpcType")
    String rpcType;
}