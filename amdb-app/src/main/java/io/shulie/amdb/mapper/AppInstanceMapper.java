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

import io.shulie.amdb.common.dto.instance.AppInfo;
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;
import java.util.Map;

public interface AppInstanceMapper extends Mapper<TAmdbAppInstanceDO>, MySqlMapper<TAmdbAppInstanceDO> {
    int insert(TAmdbAppInstanceDO record);

    TAmdbAppInstanceDO selectOneByParam(TAmdbAppInstanceDO record);

    int updateByPrimaryKeySelective(TAmdbAppInstanceDO record);

    int updateByPrimaryKey(TAmdbAppInstanceDO record);

    List<TAmdbAppInstanceDO> selectByFilter(@Param("filter") String filter);

    @Update("update t_amdb_app_instance set flag=(flag^1) where (flag&1)=1")
    void initOnlineStatus();

    @Select("select ip from t_amdb_app_instance where app_name = #{appName} and (flag&1)=1")
    List<String> selectIpListByAppName(@Param("appName") String appName);

    int insertSelective(TAmdbAppInstanceDO record);

    @Select({"<script>",
            "SELECT app_id,flag FROM t_amdb_app_instance WHERE app_id IN",
            "<foreach item='appDo' index='index' collection='appDos' open='(' separator=',' close=')'>",
            "#{appDo.id}",
            "</foreach>",
            "</script>"}
    )
    @Results(value = {@Result(column = "flag", property = "flag"), @Result(column = "app_id", property = "appId")})
    List<TAmdbAppInstanceDO> selectFlagByAppId(@Param("appDos") List<AppDO> amdbApps);

    @Select("select user_app_key,env_code,app_name from t_amdb_app_instance where flag in ('1','3') group by user_app_key,env_code,app_name")
    @Results(value = {@Result(column = "user_app_key", property = "userAppKey"), @Result(column = "env_code", property = "envCode"), @Result(column = "app_name", property = "appName")})
    List<TAmdbAppInstanceDO> selectOnlineAppList();

    List<AppInfo> selectSummaryAppInfo(Map<String, Object> request);

}