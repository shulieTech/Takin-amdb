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

package io.shulie.amdb.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MachineMetricsReportDTO {
    /**
     * 平均CPU使用率
     */
    private Float avgCpuUs;
    /**
     * 平均IO使用率
     */
    private Float avgIoUs;
    /**
     * 平均内存使用率
     */
    private Float avgMemUs;
    /**
     * 平均 IO RT
     */
    private Float avgIoRt;
    /**
     * 取样时间点 = 取样点*取样时间间隔
     */
    private Date time;
    /**
     * 取样点
     */
    private int m;
}
