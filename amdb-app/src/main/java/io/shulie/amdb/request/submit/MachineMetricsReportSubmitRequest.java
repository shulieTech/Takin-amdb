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

package io.shulie.amdb.request.submit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("机器性能指标报表数据上报")
public class MachineMetricsReportSubmitRequest {
    /**
     * IP地址
     */
    @ApiModelProperty("IP地址")
    private String ipAddress;
    /**
     * cpu使用率
     */
    @ApiModelProperty("cpu使用率")
    private Float cpuUs;
    /**
     * IO使用率
     */
    @ApiModelProperty("IO使用率")
    private Float ioUs;
    /**
     * 内存占用率
     */
    @ApiModelProperty("内存占用率")
    private Float memUs;
    /**
     * IO响应时间
     */
    @ApiModelProperty("IO响应时间")
    private Float ioRt;
    /**
     * 记录时间
     */
    @ApiModelProperty("记录时间")
    private Date timestamp;
}
