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

import io.shulie.amdb.entity.TAMDBPradarLinkConfigDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface PradarLinkConfigMapper extends Mapper<TAMDBPradarLinkConfigDO>, MySqlMapper<TAMDBPradarLinkConfigDO> {
    @Override
    @Insert("insert ignore into t_amdb_pradar_link_config(link_id,service,method,extend,app_name,rpc_type,user_app_key,env_code) " +
            " values(#{linkId},#{service},#{method},#{extend},#{appName},#{rpcType},#{userAppKey},#{envCode})")
    int insert(TAMDBPradarLinkConfigDO tamdbPradarLinkConfigDO);


    @Select("select link_id from t_amdb_pradar_link_config")
    List<String> selectConfigId();
}
