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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("MetricsFromInfluxdbRequest")
public class MetricsFromInfluxdbRequest {
    @ApiModelProperty("开始时间,时间戳格式")
    long startMilli = 0L;
    @ApiModelProperty("结束时间,时间戳格式")
    long endMilli = 0L;
    @ApiModelProperty("真实时间间隔")
    long realSeconds = 0L;      //秒数
    @ApiModelProperty("是否压测流量")
    Boolean metricsType = null; //流量类型
    @ApiModelProperty("链路图唯一边ID")
    String eagleId = "";        //边ID
    @ApiModelProperty("链路图唯一边ID集合")
    List<String> eagleIds;        //边ID集合
}
