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

import io.shulie.amdb.entity.MiddleWareInstanceDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface MiddleWareInstanceMapper extends Mapper<MiddleWareInstanceDO>, MySqlMapper<MiddleWareInstanceDO> {
    @Select("select ip_address from t_amdb_middle_ware_instance where server_type=#{type} and server_name = #{name}")
    List<String> selectIpListByTypeAndName(@Param("type") String type,@Param("name") String name);

    @Select("select ip_address from t_amdb_middle_ware_instance where server_type=#{type}")
    List<String> selectIpListByType(@Param("type") String type);
}
