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

import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface AppInstanceStatusMapper extends Mapper<TAmdbAppInstanceStatusDO>, MySqlMapper<TAmdbAppInstanceStatusDO> {
    int insert(TAmdbAppInstanceStatusDO record);

    int insertSelective(TAmdbAppInstanceStatusDO record);

    TAmdbAppInstanceStatusDO selectOneByParam(TAmdbAppInstanceStatusDO record);

    int updateByPrimaryKeySelective(TAmdbAppInstanceStatusDO record);

    int updateByPrimaryKey(TAmdbAppInstanceStatusDO record);

    List<TAmdbAppInstanceStatusDO> selectByFilter(@Param("filter") String filter);

    List<String> selectDistinctVersionByParam(TAmdbAppInstanceStatusDO record);

    void truncateTable();

    void deleteAll();

}