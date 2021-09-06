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
@ApiModel("API性能指标报表数据上报")
public class AppServerMetricsReportSubmitRequest {
    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Long id;
    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;
    /**
     * 调用来源
     */
    @ApiModelProperty("调用来源")
    private String srcAppName;
    /**
     * 调用类型
     */
    @ApiModelProperty("调用类型")
    private String callType;
    /**
     * 调用目标
     */
    @ApiModelProperty("调用目标")
    private String callEvent;
    /**
     * 平均RT
     */
    @ApiModelProperty("平均RT")
    private Float averageRt;
    /**
     * 最小RT
     */
    @ApiModelProperty("最小RT")
    private Float minRt;
    /**
     * 最大RT
     */
    @ApiModelProperty("最大RT")
    private Float maxRt;
    /**
     * P90RT
     */
    @ApiModelProperty("P90RT")
    private Float p90Rt;
    /**
     * P95RT
     */
    @ApiModelProperty("P95RT")
    private Float p95Rt;
    /**
     * P99RT
     */
    @ApiModelProperty("P99RT")
    private Float p99Rt;
    /**
     * QPS
     */
    @ApiModelProperty("QPS")
    private Float qps;
    /**
     * 成功率
     */
    @ApiModelProperty("成功率")
    private Float successRate;
    /**
     * 记录采样间隔
     */
    @ApiModelProperty("记录采样间隔")
    private Integer samplingInterval;
    /**
     * 扩展字段
     */
    @ApiModelProperty("扩展字段")
    private String ext;
    /**
     * 统计开始时间
     */
    @ApiModelProperty("统计开始时间")
    private Date statisticalStart;
    /**
     * 统计结束时间
     */
    @ApiModelProperty("统计结束时间")
    private Date statisticalEnd;
    /**
     * 标记位
     */
    @ApiModelProperty("标记位")
    private Long flag;
    /**
     * 记录时间
     */
    @ApiModelProperty("记录时间")
    private Date timestamp;
}