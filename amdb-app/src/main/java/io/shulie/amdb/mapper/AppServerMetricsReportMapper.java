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

import io.shulie.amdb.dto.AppServerMetricsReportDTO;
import io.shulie.amdb.dto.MachineMetricsReportDTO;
import io.shulie.amdb.entity.AppServerMetricsReportDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppServerMetricsReportMapper extends Mapper<AppServerMetricsReportDO>, MySqlMapper<AppServerMetricsReportDO> {
    @Select("select avg(p95_rt) as avgP95,avg(qps) as avgQps, floor((UNIX_TIMESTAMP(statistical_end) - UNIX_TIMESTAMP('${start}'))/(${samplingInterval})) as m from t_amdb_app_server_metrics_reports where app_name = '${appName}' and call_event = '${apiUrl}' and call_type = '${apiProtocol}' and statistical_end >= '${start}' and statistical_end <= '${end}' group by m")
    List<AppServerMetricsReportDTO> getReportsBySamplingInterval(@Param("samplingInterval") Integer samplingInterval , @Param("start") String start, @Param("end") String end, @Param("appName") String appName, @Param("apiUrl") String apiUrl, @Param("apiProtocol") String apiProtocol);
}
