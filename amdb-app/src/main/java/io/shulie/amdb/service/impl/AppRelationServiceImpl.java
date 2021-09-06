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

import io.shulie.amdb.entity.AppRelationDO;
import io.shulie.amdb.mapper.AppRelationMapper;
import io.shulie.amdb.request.submit.AppRelationSubmitRequest;
import io.shulie.amdb.service.AppRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

@Service
@Slf4j
public class AppRelationServiceImpl implements AppRelationService {

    @Resource
    AppRelationMapper appRelationMapper;

    @Override
    @Async
    public void addRelation(AppRelationSubmitRequest relationSubmitRequest) {
        Example example = new Example(AppRelationDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("fromAppName", relationSubmitRequest.getFromAppName());
        criteria.andEqualTo("toAppName", relationSubmitRequest.getToAppName());
        criteria.andEqualTo("tenant", relationSubmitRequest.getTenant());
        int count = appRelationMapper.selectCountByExample(example);
        try {
            if (count == 0) {
                AppRelationDO appRelationDO = new AppRelationDO();
                appRelationDO.setFromAppName(relationSubmitRequest.getFromAppName());
                appRelationDO.setToAppName(relationSubmitRequest.getToAppName());
                appRelationDO.setTenant(relationSubmitRequest.getTenant());
                appRelationDO.setCreator(relationSubmitRequest.getUserId());
                appRelationDO.setCreatorName(relationSubmitRequest.getUserName());
                appRelationMapper.insert(appRelationDO);
            }
        } catch (Exception e) {
            log.error("关系处理失败", e);
        }
    }
}
