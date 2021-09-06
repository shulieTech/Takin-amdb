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
import io.shulie.amdb.entity.TAmdbMqSubscribeDO;
import io.shulie.amdb.mapper.TAmdbMqSubscribeMapper;
import io.shulie.amdb.request.submit.MqSubscribeAddSubmitRequest;
import io.shulie.amdb.request.submit.MqSubscribeDeleteRequest;
import io.shulie.amdb.request.submit.MqSubscribeUpdateSubmitRequest;
import io.shulie.amdb.response.subscribe.MqSubscribeResponse;
import io.shulie.amdb.service.MqSubscribeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;

//@Service
public class MqSubscribeServiceImpl implements MqSubscribeService {

    @Value("${rocketMq.topicCreateRule}")
    private String topicCreateRule;

    @Resource
    TAmdbMqSubscribeMapper tAmdbMqSubscribeMapper;

    @Override
    public Response addMqSubscribe(MqSubscribeAddSubmitRequest addSubmitRequest) {
        String subscribeTarget = addSubmitRequest.getSubscribeTarget();
        String tenant = addSubmitRequest.getTenant();
        TAmdbMqSubscribeDO subscribeDO = getSubscribeDO(subscribeTarget, tenant);
        if (subscribeDO != null) {
            Date now = new Date();
            String topic = createTopic(subscribeTarget, tenant);
            subscribeDO = new TAmdbMqSubscribeDO();
            subscribeDO.setSubscribeTarget(addSubmitRequest.getSubscribeTarget());
            subscribeDO.setTenant(tenant);
            subscribeDO.setTopic(topic);
            subscribeDO.setFields(addSubmitRequest.getFields());
            subscribeDO.setParams(addSubmitRequest.getParams());
            subscribeDO.setCreator(addSubmitRequest.getUserId());
            subscribeDO.setCreatorName(addSubmitRequest.getUserName());
            subscribeDO.setModifier(addSubmitRequest.getUserId());
            subscribeDO.setModifierName(addSubmitRequest.getUserName());
            subscribeDO.setGmtCreate(now);
            subscribeDO.setGmtModify(now);
            tAmdbMqSubscribeMapper.insert(subscribeDO);
        }
        MqSubscribeResponse response = new MqSubscribeResponse();
        response.setTopic(subscribeDO.getTopic());
        return Response.success(response);
    }

    @Override
    public Response updateMqSubscribe(MqSubscribeUpdateSubmitRequest updateSubmitRequest) {
        TAmdbMqSubscribeDO subscribeDO = getSubscribeDO(updateSubmitRequest.getSubscribeTarget(), updateSubmitRequest.getTenant());
        if (subscribeDO != null) {
            subscribeDO.setFields(updateSubmitRequest.getFields());
            subscribeDO.setParams(updateSubmitRequest.getParams());
            subscribeDO.setModifier(updateSubmitRequest.getUserId());
            subscribeDO.setModifierName(updateSubmitRequest.getUserName());
            subscribeDO.setGmtModify(new Date());
            tAmdbMqSubscribeMapper.updateByPrimaryKeySelective(subscribeDO);
            return Response.emptySuccess();
        }
        return Response.fail("未找到订阅记录：" + updateSubmitRequest.getSubscribeTarget());
    }

    @Override
    public Response removeMqSubscribe(MqSubscribeDeleteRequest deleteRequest) {
        TAmdbMqSubscribeDO subscribeDO = getSubscribeDO(deleteRequest.getSubscribeTarget(), deleteRequest.getTenant());
        if (subscribeDO != null) {
            tAmdbMqSubscribeMapper.deleteByPrimaryKey(subscribeDO);
            return Response.emptySuccess();
        }
        return Response.fail("未找到订阅记录：" + deleteRequest.getSubscribeTarget());
    }

    private TAmdbMqSubscribeDO getSubscribeDO(String subscribeTarget, String tenant) {
        Example example = new Example(TAmdbMqSubscribeDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("subscribe_target", subscribeTarget);
        criteria.andEqualTo("tenant", tenant);
        return tAmdbMqSubscribeMapper.selectOneByExample(criteria);
    }

    private TAmdbMqSubscribeDO getSubscribeDO(String subscribeKey) {
        Example example = new Example(TAmdbMqSubscribeDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("subscribe_key", subscribeKey);
        return tAmdbMqSubscribeMapper.selectOneByExample(criteria);
    }

    private String createTopic(String subscribeTarget, String tenant) {
        return topicCreateRule.replace("#{target}", subscribeTarget).replace("#{tenant}", tenant);
    }
}
