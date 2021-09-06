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

import java.util.Date;

@ApiModel("机器指标查询")
@Data
public class MachineMetricsReportQueryRequest {
    /**
     * 服务类型
     */
    @ApiModelProperty("服务类型")
    private String serverType;
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serverName;
    /**
     * ip列表
     */
    @ApiModelProperty("ip列表")
    private String ipList;
    /**
     * 取样间隔(单位：s)
     */
    @ApiModelProperty("取样间隔(单位：s)")
    private Integer samplingInterval;
    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    private Date startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    private Date endTime;
}
