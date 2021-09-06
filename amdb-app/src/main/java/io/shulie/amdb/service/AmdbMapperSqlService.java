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

package io.shulie.amdb.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.shulie.amdb.mapper.TAmdbMapperSqlInfoMapper;
import io.shulie.amdb.entity.TAmdbMapperSqlInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AmdbMapperSqlService {
    @Resource
    TAmdbMapperSqlInfoMapper tAmdbMapperSqlInfoMapper;

    public int insert(TAmdbMapperSqlInfo tAmdbApp) {
        return tAmdbMapperSqlInfoMapper.insert(tAmdbApp);
    }

    public int insertBatch(List<TAmdbMapperSqlInfo> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            insert(tAmdbApp);
        });
        return 1;
    }

    public int update(TAmdbMapperSqlInfo tAmdbApp) {
        return tAmdbMapperSqlInfoMapper.updateByPrimaryKey(tAmdbApp);
    }

    public int updateBatch(List<TAmdbMapperSqlInfo> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            update(tAmdbApp);
        });
        return 1;
    }

    public int delete(TAmdbMapperSqlInfo tAmdbApp) {
        return tAmdbMapperSqlInfoMapper.deleteByPrimaryKey(tAmdbApp.getId());
    }

    public TAmdbMapperSqlInfo selectByPrimaryKey(TAmdbMapperSqlInfo tAmdbApp) {
        return tAmdbMapperSqlInfoMapper.selectByPrimaryKey(tAmdbApp.getId());
    }

    public List<TAmdbMapperSqlInfo> selectList(TAmdbMapperSqlInfo tAmdbApp) {
        return tAmdbMapperSqlInfoMapper.selectList(tAmdbApp);
    }

    public List<TAmdbMapperSqlInfo> selectByFilter(String filter, Integer page, Integer pageSize) {
        if (page == null || page == 0) {
            page = 1;
        }
        if (pageSize == null || pageSize == 0) {
            pageSize = 100;
        }
        Page<TAmdbMapperSqlInfo> tAmdbMapperSqlInfoPage = PageHelper.startPage(page, pageSize).doSelectPage(() -> {
            tAmdbMapperSqlInfoMapper.selectByFilter(filter);
        });
        System.err.println(JSON.toJSON(tAmdbMapperSqlInfoPage));
        return tAmdbMapperSqlInfoPage;
    }
}
