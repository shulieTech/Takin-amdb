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

import io.shulie.amdb.entity.TAmdbAgentConfigDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TAmdbAgentConfigDOMapper extends Mapper<TAmdbAgentConfigDO> {

    @Insert({
            "<script>",
            "insert into t_amdb_agent_config(app_name, agent_id, config_key, config_value, status,user_app_key,env_code,user_id) values ",
            "<foreach collection='agentConfigs' item='agentConfig' index='index' separator=','>",
            "(#{agentConfig.appName}, #{agentConfig.agentId}, #{agentConfig.configKey}, #{agentConfig.configValue}, #{agentConfig.status},#{agentConfig.userAppKey},#{agentConfig.envCode},#{agentConfig.userId})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(@Param(value = "agentConfigs") List<TAmdbAgentConfigDO> agentConfigs);

    @Update("truncate table t_amdb_agent_config")
    void truncateTable();

    @Delete("delete from t_amdb_agent_config where 1=1")
    void deleteAll();
}