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
import io.shulie.amdb.entity.TAmdbConfigDO;
import io.shulie.amdb.mapper.TAmdbConfigMapper;
import io.shulie.amdb.request.query.ConfigQueryRequest;
import io.shulie.amdb.request.submit.ConfigAddOrUpdateSubmitRequest;
import io.shulie.amdb.service.ConfigService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Resource
    TAmdbConfigMapper tAmdbConfigMapper;

    @Override
    public Response updateOrInsertConfig(ConfigAddOrUpdateSubmitRequest submitRequest) {
        String atrName = submitRequest.getAtrName();
        String atrValue = submitRequest.getAtrValue();
        String tenant = submitRequest.getTenant();
        // 判断配置是否已存在
        Example example = new Example(TAmdbConfigDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("atrName", atrName);
        criteria.andEqualTo("tenant", tenant);
        TAmdbConfigDO configDO = tAmdbConfigMapper.selectOneByExample(example);
        Date now = new Date();
        if (configDO == null) {
            configDO = new TAmdbConfigDO();
            configDO.setAtrName(atrName);
            configDO.setAtrValue(atrValue);
            configDO.setTenant(tenant);
            configDO.setCreator(submitRequest.getUserId());
            configDO.setCreatorName(submitRequest.getUserName());
            configDO.setModifier(submitRequest.getUserId());
            configDO.setModifierName(submitRequest.getUserName());
            configDO.setGmtCreate(now);
            configDO.setGmtModify(now);
            tAmdbConfigMapper.insert(configDO);
        } else {
            configDO.setAtrValue(atrValue);
            configDO.setModifier(submitRequest.getUserId());
            configDO.setModifierName(submitRequest.getUserName());
            configDO.setGmtModify(now);
            tAmdbConfigMapper.updateByPrimaryKeySelective(configDO);
        }
        return Response.emptySuccess();
    }

    @Override
    public String getConfigValue(ConfigQueryRequest queryRequest) {
        return getConfigValue(queryRequest.getTenant(), queryRequest.getAtrName());
    }

    @Override
    public String getConfigValue(String tenant, String atrName) {
        // 判断配置是否已存在
        Example example = new Example(TAmdbConfigDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("atrName", atrName);
        criteria.andEqualTo("tenant", tenant);
        TAmdbConfigDO configDO = tAmdbConfigMapper.selectOneByExample(example);
        if (configDO != null) {
            return configDO.getAtrValue();
        }
        return "";
    }
}
