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

import io.shulie.amdb.entity.MiddleWareInstanceDO;
import io.shulie.amdb.mapper.MiddleWareInstanceMapper;
import io.shulie.amdb.request.query.MiddleWareInstanceQueryRequest;
import io.shulie.amdb.response.instance.MiddleWareInstanceResponse;
import io.shulie.amdb.service.MiddleWareInstanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MiddleWareInstanceServiceImpl implements MiddleWareInstanceService {

    @Resource
    MiddleWareInstanceMapper middleWareInstanceMapper;

    @Override
    public List<MiddleWareInstanceResponse> batchQuery(MiddleWareInstanceQueryRequest request) {
        Example example = new Example(MiddleWareInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("serverType", request.getType().toUpperCase());
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andEqualTo("serverName", request.getName());
        }
        List<MiddleWareInstanceDO> middleWareInstanceDos = middleWareInstanceMapper.selectByExample(example);
        Map<String, List<String>> middleWareInfo = new HashMap<>();// type#name->ipList;
        middleWareInstanceDos.forEach(middleWareInstanceDO -> {
            if (middleWareInfo.get(middleWareInstanceDO.getServerName()) == null) {
                middleWareInfo.put(middleWareInstanceDO.getServerName(), new ArrayList<>());
            }
            middleWareInfo.get(middleWareInstanceDO.getServerName()).add(middleWareInstanceDO.getIpAddress());
        });
        List<MiddleWareInstanceResponse> responseParamList = new ArrayList<>();
        middleWareInfo.keySet().forEach(name -> {
            MiddleWareInstanceResponse responseParam = new MiddleWareInstanceResponse();
            responseParam.setName(name);
            responseParam.setIpList(middleWareInfo.get(name));
            responseParamList.add(responseParam);
        });
        return responseParamList;
    }
}
