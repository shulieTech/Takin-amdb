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

import io.shulie.amdb.entity.TAmdbPradarLinkEdgeDO;
import io.shulie.amdb.entity.TAmdbPradarLinkNodeDO;
import org.apache.ibatis.annotations.Insert;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface PradarLinkEdgeMapper extends Mapper<TAmdbPradarLinkEdgeDO>, MySqlMapper<TAmdbPradarLinkEdgeDO> {
    @Insert("insert ignore into t_amdb_pradar_link_edge(link_id,service,method,extend,app_name,trace_app_name,server_app_name,rpc_type,log_type,middleware_name,entrance_id,from_app_id,to_app_id,edge_id) " +
            " values(#{linkId},#{service},#{method},#{extend},#{appName},#{traceAppName},#{serverAppName},#{rpcType},#{logType},#{middlewareName},#{entranceId},#{fromAppId},#{toAppId},#{edgeId})")
    void insertIgnore(TAmdbPradarLinkEdgeDO edgeDO);
}
