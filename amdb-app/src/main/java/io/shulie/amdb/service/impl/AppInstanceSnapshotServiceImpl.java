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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.TAmdbAppInstanceSnapshotDO;
import io.shulie.amdb.mapper.AppInstanceSnapshotMapper;
import io.shulie.amdb.request.query.AppInstanceSnapshotQueryRequest;
import io.shulie.amdb.response.app.AppInstanceSnapshotResponse;
import io.shulie.amdb.service.AppInstanceSnapshotService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AppInstanceSnapshotServiceImpl implements AppInstanceSnapshotService {

    @Resource
    AppInstanceSnapshotMapper appInstanceSnapshotMapper;

    @Override
    public Response<List<AppInstanceSnapshotResponse>> queryBatch(AppInstanceSnapshotQueryRequest requestParam) {
        Example example = new Example(TAmdbAppInstanceSnapshotDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("appName", Arrays.asList(requestParam.getAppName().split(",")));
        criteria.andBetween("snapshotDate", requestParam.getSnapshotDateStart(), requestParam.getSnapshotDateEnd());
        List<TAmdbAppInstanceSnapshotDO> snapshotDOList = appInstanceSnapshotMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(snapshotDOList)) {
            Map<String, List<String>> map = new HashMap<>();
            snapshotDOList.forEach(appInstanceSnapshotDO -> {
                String appName = appInstanceSnapshotDO.getAppName();
                String snapshotDate = DateFormatUtils.format(appInstanceSnapshotDO.getSnapshotDate(), "yyyy-MM-dd");
                String key = appName + "#" + snapshotDate;
                if(map.get(key)==null){
                    map.put(key,new ArrayList<>());
                }
                map.get(key).add(appInstanceSnapshotDO.getIp());
            });
            List<AppInstanceSnapshotResponse> responseParamList=new ArrayList<>();
            map.keySet().forEach(key->{
                String keys[] = key.split("#");
                AppInstanceSnapshotResponse responseParam = new AppInstanceSnapshotResponse();
                responseParam.setAppName(keys[0]);
                responseParam.setSnapshotDate(keys[1]);
                responseParam.setIpList(map.get(key));
                responseParamList.add(responseParam);
            });
            return Response.success(responseParamList);
        }
        return Response.emptySuccess();
    }
}
