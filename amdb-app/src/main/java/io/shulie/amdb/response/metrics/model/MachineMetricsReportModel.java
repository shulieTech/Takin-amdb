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

package io.shulie.amdb.response.metrics.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("机器指标数据模型")
public class MachineMetricsReportModel implements Serializable {
    /**
     * 平均CPU使用率
     */
    @ApiModelProperty("平均CPU使用率")
    private Double avgCpuUs;
    /**
     * 最大CPU使用率
     */
    @ApiModelProperty("最大CPU使用率")
    private Double maxCpuUs;
    /**
     * 平均IO使用率
     */
    @ApiModelProperty("平均IO使用率")
    private Double avgIoUs;
    /**
     * 平均内存使用率
     */
    @ApiModelProperty("平均内存使用率")
    private Double avgMemUs;
    /**
     * 最大内存使用率
     */
    @ApiModelProperty("最大内存使用率")
    private Double maxMemUs;
    /**
     * 平均 IO RT
     */
    @ApiModelProperty("平均IO_RT")
    private Double avgIoRt;
    /**
     * 日期
     */
    @ApiModelProperty("日期")
    private String day;
}
