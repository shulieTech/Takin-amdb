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

import io.shulie.amdb.entity.TAMDBPradarE2EAssertConfigDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface PradarE2EAssertMapper extends Mapper<TAMDBPradarE2EAssertConfigDO>, MySqlMapper<TAMDBPradarE2EAssertConfigDO> {
    @Override
    @Insert("insert ignore into t_amdb_pradar_e2e_assert_config(node_id,assert_code,assert_condition,user_app_key,env_code) " +
            " values(#{nodeId},#{assertCode},#{assertCondition},#{userAppKey},#{envCode})")
    int insert(TAMDBPradarE2EAssertConfigDO e2eAssertConfigDo);

    @Override
    @Delete("<script>"
        + "delete from t_amdb_pradar_e2e_assert_config where node_id=#{nodeId} and assert_code=#{assertCode}"
        + "<if test='userAppKey != null'> and user_app_key=#{userAppKey} </if>"
        + "<if test='envCode != null'> and env_code=#{envCode} </if>"
        + "</script>")
    int delete(TAMDBPradarE2EAssertConfigDO e2eAssertConfigDo);
}
