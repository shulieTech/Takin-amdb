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

import io.shulie.amdb.entity.TAmdbMapperSqlInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TAmdbMapperSqlInfoMapper extends Mapper<TAmdbMapperSqlInfo> {
    int insert(TAmdbMapperSqlInfo record);

    int insertSelective(TAmdbMapperSqlInfo record);

    List<TAmdbMapperSqlInfo> selectList(TAmdbMapperSqlInfo tAmdbMapperSqlInfo);

    List<TAmdbMapperSqlInfo> selectByFilter(@Param("filter") String filter);

    int updateByPrimaryKeySelective(TAmdbMapperSqlInfo record);

    int updateByPrimaryKey(TAmdbMapperSqlInfo record);
}