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

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "`t_amdb_machine_metrics_reports`")
public class MachineMetricsReportDO implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    private Long id;
    /**
     * IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;
    /**
     * cpu使用率
     */
    @Column(name = "cpu_us")
    private Float cpuUs;
    /**
     * IO使用率
     */
    @Column(name = "io_us")
    private Float ioUs;
    /**
     * 内存占用率
     */
    @Column(name = "mem_us")
    private Float memUs;
    /**
     * IO响应时间
     */
    @Column(name = "io_rt")
    private Float ioRt;
    /**
     * 记录时间
     */
    @Column(name = "timestamp")
    private Date timestamp;
    /**
     * 创建时间
     */
    @Column(name = "gmt_create")
    private Date gmtCreate;
    /**
     * 更新时间
     */
    @Column(name = "gmt_modify")
    private Date gmtModify;
}
