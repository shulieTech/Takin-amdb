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
public class LinkNodeDTO {
    @ApiModelProperty("节点唯一标识")
    String nodeId;
    @ApiModelProperty("节点名称")
    String nodeName;
    @ApiModelProperty("是否入口节点")
    boolean root;
    @ApiModelProperty("节点类型")
    String nodeType;
    @ApiModelProperty("节点类型分组(大类)")
    String nodeTypeGroup;
    @ApiModelProperty("扩展信息")
    Object extendInfo;
}
