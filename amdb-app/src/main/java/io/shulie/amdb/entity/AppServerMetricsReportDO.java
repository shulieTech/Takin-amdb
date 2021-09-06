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

package io.shulie.amdb.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
@Table(name = "`t_amdb_app_server_metrics_reports`")
public class AppServerMetricsReportDO {
    /**
     * 主键
     */
    @Column(name = "`id`")
    private Long id;
    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    private String appName;
    /**
     * 调用来源
     */
    @Column(name = "`src_app_name`")
    private String srcAppName;
    /**
     * 调用类型
     */
    @Column(name = "`call_type`")
    private String callType;
    /**
     * 调用目标
     */
    @Column(name = "`call_event`")
    private String callEvent;
    /**
     * 平均RT
     */
    @Column(name = "`average_rt`")
    private Float averageRt;
    /**
     * 最小RT
     */
    @Column(name = "`min_rt`")
    private Float minRt;
    /**
     * 最大RT
     */
    @Column(name = "`max_rt`")
    private Float maxRt;
    /**
     * P90RT
     */
    @Column(name = "`p90_rt`")
    private Float p90Rt;
    /**
     * P95RT
     */
    @Column(name = "`p95_rt`")
    private Float p95Rt;
    /**
     * P99RT
     */
    @Column(name = "`p99_rt`")
    private Float p99Rt;
    /**
     * QPS
     */
    @Column(name = "`qps`")
    private Float qps;
    /**
     * 成功率
     */
    @Column(name = "`success_rate`")
    private Float successRate;
    /**
     * 记录采样间隔
     */
    @Column(name = "`sampling_interval`")
    private Integer samplingInterval;
    /**
     * 扩展字段
     */
    @Column(name = "`ext`")
    private String ext;
    /**
     * 统计开始时间
     */
    @Column(name = "`statistical_start`")
    private Date statisticalStart;
    /**
     * 统计结束时间
     */
    @Column(name = "`statistical_end`")
    private Date statisticalEnd;
    /**
     * 标记位
     */
    @Column(name = "`flag`")
    private Long flag;
    /**
     * 记录时间
     */
    @Column(name = "`timestamp`")
    private Date timestamp;
}
