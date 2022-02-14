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

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@ApiModel("拓扑图查询参数")
public class TopologyQueryParam extends AbstractAmdbBaseRequest {
    @ApiModelProperty("拓扑图唯一标识")
    String linkId;
    @ApiModelProperty(value = "应用名称", required = true)
    String appName;
    @ApiModelProperty(value = "服务名称", required = true)
    String serviceName;
    @ApiModelProperty(value = "方法名称", required = true)
    String method;
    @ApiModelProperty(value = "调用类型", required = true)
    String rpcType;
    @ApiModelProperty("扩展信息(默认为空字符)")
    String extend = "";
    @ApiModelProperty("是否入口")
    Boolean isTrace;
    @ApiModelProperty("节点唯一标识")
    private String id;

    public boolean isTrace() {
        if (StringUtils.isEmpty(isTrace)) {
            isTrace = true;
        }
        return isTrace;
    }
}
