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

package io.shulie.amdb.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.shulie.amdb.convert.AppServerConvert;
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.entity.TAmdbAppServer;
import io.shulie.amdb.mapper.AppServerMapper;
import io.shulie.amdb.request.query.AppServerQueryRequest;
import io.shulie.amdb.response.app.AppServerResponse;
import io.shulie.amdb.service.AppServerService;
import io.shulie.amdb.utils.PagingUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppServerServiceImpl implements AppServerService {
    @Resource
    AppServerMapper appServerMapper;

    @Override
    public int insert(TAmdbAppServer tAmdbApp) {
        return appServerMapper.insert(tAmdbApp);
    }

    @Override
    public int insertBatch(List<TAmdbAppServer> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            // 存在则不插入
            Example example = new Example(TAmdbAppServer.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("appName", tAmdbApp.getAppName());
            criteria.andEqualTo("serverName", tAmdbApp.getServerName());
            if (appServerMapper.selectCountByExample(example) == 0) {
                insert(tAmdbApp);
            }
        });
        return 1;
    }

    @Override
    public int update(TAmdbAppServer tAmdbApp) {
        return appServerMapper.updateByPrimaryKey(tAmdbApp);
    }

    @Override
    public int updateBatch(List<TAmdbAppServer> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            update(tAmdbApp);
        });
        return 1;
    }

    @Override
    public int delete(TAmdbAppServer tAmdbApp) {
        return appServerMapper.deleteByPrimaryKey(tAmdbApp.getId());
    }

    @Override
    public TAmdbAppServer select(TAmdbAppServer tAmdbApp) {
        return appServerMapper.selectByPrimaryKey(tAmdbApp.getId());
    }

    @Override
    public PageInfo<AppServerResponse> selectBatch(AppServerQueryRequest appServerQueryRequest) {
        Example example = new Example(AppDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appName", appServerQueryRequest.getAppName());
        criteria.andCondition("(flag&1)=1");
        PageHelper.startPage(appServerQueryRequest.getCurrentPage(), appServerQueryRequest.getPageSize());
        List<TAmdbAppServer> amdbAppServers = appServerMapper.selectByExample(example);
        List<AppServerResponse> appServerResponses = amdbAppServers.stream().map(
                amdbAppServer -> AppServerConvert.convertAppServerResponse(amdbAppServer)
        ).collect(Collectors.toList());
        return PagingUtils.result(amdbAppServers, appServerResponses);
    }

    @Override
    public void deleteByParams(AppServerQueryRequest appServerQueryRequest) {
        Example example = new Example(AppDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("appName", Arrays.asList(appServerQueryRequest.getAppName().split(",")));
        appServerMapper.deleteByExample(example);
    }
}
