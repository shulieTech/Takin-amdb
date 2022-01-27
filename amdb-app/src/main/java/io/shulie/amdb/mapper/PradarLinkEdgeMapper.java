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
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface PradarLinkEdgeMapper extends Mapper<TAmdbPradarLinkEdgeDO>, MySqlMapper<TAmdbPradarLinkEdgeDO> {
    @Insert("insert ignore into t_amdb_pradar_link_edge(link_id,service,method,extend,app_name,trace_app_name,server_app_name,rpc_type,log_type,middleware_name,entrance_id,from_app_id,to_app_id,edge_id) " +
            " values(#{linkId},#{service},#{method},#{extend},#{appName},#{traceAppName},#{serverAppName},#{rpcType},#{logType},#{middlewareName},#{entranceId},#{fromAppId},#{toAppId},#{edgeId})")
    void insertIgnore(TAmdbPradarLinkEdgeDO edgeDO);

    @Select("select count(1) as ct,link_id as linkId,from_app_id as fromAppId,to_app_id as toAppId,rpc_type as rpcType,log_type as logType," +
            "service,method,app_name as appName,server_app_name as serverAppName,middleware_name as middlewareName from t_amdb_pradar_link_edge \n" +
            "where link_id not like '%_bak'\n" +
            "group by \n" +
            "link_id,from_app_id,to_app_id,rpc_type,log_type,service,method,app_name,server_app_name,middleware_name \n" +
            "having ct >1")
    List<TAmdbPradarLinkEdgeDO> getExpiredEdge();

    @Select("select id from t_amdb_pradar_link_edge i\n" +
            "where \n" +
            "i.link_id=#{linkId}\n" +
            "and i.from_app_id = #{fromAppId}\n" +
            "and i.to_app_id = #{toAppId}\n" +
            "and i.rpc_type = #{rpcType}\n" +
            "and i.log_type = #{logType}\n" +
            "and i.service = #{service}\n" +
            "and i.method = #{method}\n" +
            "and i.app_name = #{appName}\n" +
            "and i.server_app_name = #{serverAppName}\n" +
            "and i.middleware_name = #{middlewareName}\n" +
            "order by i.gmt_modify desc limit 1")
    Integer getLatestEdge(TAmdbPradarLinkEdgeDO edgeDO);

    @Update("update t_amdb_pradar_link_edge set link_id = CONCAT(link_id,'_bak')\n" +
            "where id !=#{id}\n" +
            "and link_id=#{linkId}\n" +
            "and from_app_id = #{fromAppId}\n" +
            "and to_app_id = #{toAppId}\n" +
            "and rpc_type = #{rpcType}\n" +
            "and log_type = #{logType}\n" +
            "and service = #{service}\n" +
            "and method = #{method}\n" +
            "and app_name = #{appName}\n" +
            "and server_app_name = #{serverAppName}\n" +
            "and middleware_name = #{middlewareName}")
    Integer clearExpiredEdge(TAmdbPradarLinkEdgeDO edgeDO);

    @Delete("delete from t_amdb_pradar_link_edge where link_id like '%_bak'")
    Integer deleteExpiredEdge();

    /**
     * @return
     */
    @Select("select distinct CONCAT(app_name,'#',service,'#',method) as service,link_id as linkId from t_amdb_pradar_link_edge where app_name != 'UNKNOWN' and log_type != '2'")
    List<TAmdbPradarLinkEdgeDO> getAllEdge1();


    /**
     * @return
     */
    @Select("select distinct CONCAT(edge.app_name,'#',edge.service,'#',edge.method) as service,\n" +
            "CONCAT(config.app_name,'#',config.service,'#',config.method) as extend\n" +
            "from (select * from t_amdb_pradar_link_edge  where app_name != 'UNKNOWN' and log_type != '2') edge,t_amdb_pradar_link_config config where config.link_id = edge.link_id ")
    List<TAmdbPradarLinkEdgeDO> getAllEdge2();

    @Select("select link_id from t_amdb_pradar_link_config where app_name = #{appName} and service = #{service} and `method` = #{method} limit 1")
    String getLinkId(String appName, String service, String method);

}
