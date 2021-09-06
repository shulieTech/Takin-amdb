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

@Data
@ApiModel("接口统计指标查询")
public class AppServerMetricsReportQueryRequest{
    /**
     * ApiPerformance结构体为默认返回的数据；
     * 需返回的字段列表，多个字段之间用","分隔.可选值有:
     * 1.rt,响应时间
     * 2.qps，每秒查询
     * 3.tps，每秒事务
     * 4.sr，成功率
     */
    @ApiModelProperty("需返回的字段列表")
    private String fields;
    /**
     * 接口完整地址
     */
    @ApiModelProperty("接口地址")
    private String apiUrl;
    /**
     * 接口协议，http/dubbo，都是指业务入口的接口
     */
    @ApiModelProperty("接口协议，http/dubbo")
    private String apiProtocol;
    /**
     * 接口所属应用名称
     */
    @ApiModelProperty("接口所属应用名称")
    private String belongApp;
    /**
     * 应用实例ip 地址
     */
    @ApiModelProperty("应用实例ip 地址")
    private String ipAddress;
    /**
     * 采样周期，单位：秒
     */
    @ApiModelProperty("采样周期")
    private Integer samplingInterval;
    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    private Date timeScopeStart;
    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    private Date timeScopeEnd;
}
