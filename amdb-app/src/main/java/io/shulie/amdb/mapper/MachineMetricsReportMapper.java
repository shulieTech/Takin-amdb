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

package io.shulie.amdb.mapper;

import io.shulie.amdb.entity.MachineMetricsReportDO;
import io.shulie.amdb.dto.MachineMetricsReportDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface MachineMetricsReportMapper extends Mapper<MachineMetricsReportDO>, MySqlMapper<MachineMetricsReportDO> {
    @Select("select avg(cpu_us) as avgCpuUs, avg(io_us) as avgIoUs, avg(mem_us) as avgMemUs, avg(io_rt) as avgIoRt, floor((UNIX_TIMESTAMP(timestamp) - UNIX_TIMESTAMP('${start}'))/(${samplingInterval})) as m from t_amdb_machine_metrics_reports where ip_address in (${ipList}) and timestamp >= '${start}' and timestamp <= '${end}' group by m")
    List<MachineMetricsReportDTO> getReportsBySamplingInterval(@Param("samplingInterval") Integer samplingInterval , @Param("start") String start, @Param("end") String end, @Param("ipList") String ipList);

    @Insert("insert ignore into t_amdb_machine_metrics_reports"
        + "(`ip_address`, `cpu_us`, `io_us`, `mem_us`, `io_rt`, `timestamp`, `gmt_create`, `gmt_modify`) "
        + "values(#{ipAddress,jdbcType=VARCHAR},#{cpuUs,jdbcType=FLOAT},#{ioUs,jdbcType=FLOAT},#{memUs,jdbcType=FLOAT},#{ioRt,jdbcType=FLOAT}"
        + ",#{timestamp,jdbcType=TIMESTAMP},#{gmtCreate,jdbcType=TIMESTAMP},#{gmtModify,jdbcType=TIMESTAMP})")
    int insert(MachineMetricsReportDO reportDO);
}
