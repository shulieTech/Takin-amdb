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

package io.shulie.amdb.response.metrics;

import io.shulie.amdb.response.metrics.model.MachineMetricsReportModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("机器性能指标查询结果")
public class MachineMetricsReportResponse implements Serializable {
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    String serverName;
    /**
     * 服务类型
     */
    @ApiModelProperty("服务类型")
    String serverType;
    /**
     * IP列表
     */
    @ApiModelProperty("IP列表")
    List<String> ips;
    /**
     * 指标列表
     */
    @ApiModelProperty("指标列表")
    List<MachineMetricsReportModel> metricsList;
}
