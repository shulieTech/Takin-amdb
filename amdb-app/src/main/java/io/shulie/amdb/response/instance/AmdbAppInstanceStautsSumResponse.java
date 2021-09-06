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

package io.shulie.amdb.response.instance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("出参-应用实例状态汇总信息")
public class AmdbAppInstanceStautsSumResponse implements Serializable {
    //在线节点总数
    @ApiModelProperty("在线节点总数")
    int onlineNodesCount;
    //已安装节点总数
    @ApiModelProperty("特定状态节点总数")
    int specificStatusNodesCount;
    //节点所有版本
    @ApiModelProperty("节点所有版本")
    List<String> versionList;

}