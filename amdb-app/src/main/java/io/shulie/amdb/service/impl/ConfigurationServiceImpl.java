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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.ConfigurationDO;
import io.shulie.amdb.entity.ConfigurationItemDO;
import io.shulie.amdb.entity.ConfigurationItemKeyDO;
import io.shulie.amdb.mapper.ConfigurationMapper;
import io.shulie.amdb.request.submit.ConfigurationSubmitRequest;
import io.shulie.amdb.request.submit.ConfigurationSubmitRequest.ConfigurationItem;
import io.shulie.amdb.response.app.model.Configuration;
import io.shulie.amdb.response.app.model.ConfigurationKey;
import io.shulie.amdb.service.ConfigurationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Resource
    private ConfigurationMapper configurationMapper;

    @Override
    @Transactional
    public Response<String> addOrUpdateConfiguration(ConfigurationSubmitRequest request) {
        String type = request.getType();
        List<ConfigurationItemKeyDO> itemKeyList = findItemKeyByType(type);
        if (CollectionUtils.isEmpty(itemKeyList)) {
            throw new RuntimeException("可配置key：[]，自动忽略无效key");
        }
        // 系统预置可配置项
        Map<String, ConfigurationItemKeyDO> systemKeys = itemKeyList.stream().collect(
            Collectors.toMap(ConfigurationItemKeyDO::getName, Function.identity()));

        String message = String.format("可配置key：%s，自动忽略无效key", itemKeyList);
        // 有效配置项
        Collection<ConfigurationItem> validKeys = request.getItems().stream().filter(
            itemKey -> systemKeys.containsKey(itemKey.getKey()))
            .collect(Collectors.toMap(ConfigurationItem::getKey, Function.identity(), (oldValue, newValue) -> oldValue))
            .values();
        if (CollectionUtils.isEmpty(validKeys)) {
            throw new RuntimeException(message);
        }
        request.setItems(new ArrayList<>(validKeys));
        // save
        saveConfiguration(request);
        return Response.success(message);
    }

    private void saveConfiguration(ConfigurationSubmitRequest request) {
        String number = request.getNumber();

        Example example = new Example(ConfigurationDO.class);
        example.createCriteria().andEqualTo("number", number);
        ConfigurationDO existsConfiguration = configurationMapper.selectOneByExample(example);
        if (existsConfiguration == null) {
            addConfiguration(request);
        } else {
            existsConfiguration.setName(request.getName());
            existsConfiguration.setGmtModify(new Date());
            existsConfiguration.setDesc(request.getDesc());
            configurationMapper.updateByPrimaryKeySelective(existsConfiguration);

            // 配置项先删除后新增
            configurationMapper.deleteItemByConfigurationId(existsConfiguration.getId());
            addConfigurationItem(existsConfiguration.getId(), request);
        }
    }

    private void addConfiguration(ConfigurationSubmitRequest request) {
        ConfigurationDO configuration = request.convertAdd();
        configurationMapper.insertUseGeneratedKeys(configuration);
        Long configurationId = configuration.getId();
        addConfigurationItem(configurationId, request);
    }

    private void addConfigurationItem(Long configurationId, ConfigurationSubmitRequest request) {
        List<ConfigurationItemDO> itemDoList = request.getItems().stream().map(item -> {
            ConfigurationItemDO itemDO = new ConfigurationItemDO();
            itemDO.setConfigurationId(configurationId);
            itemDO.setKey(item.getKey());
            itemDO.setValue(item.getValue());
            return itemDO;
        }).collect(Collectors.toList());
        configurationMapper.insertItemList(itemDoList);
    }

    @Override
    public void deleteConfiguration(String number) {
        Example example = new Example(ConfigurationDO.class);
        example.createCriteria().andEqualTo("number", number);
        ConfigurationDO configurationDO = new ConfigurationDO();
        configurationDO.setDeleted(1);
        configurationDO.setGmtModify(new Date());
        configurationMapper.updateByExampleSelective(configurationDO, example);
    }

    @Override
    public void enableConfiguration(String number) {
        enableOrderDisableConfiguration(number, true);
    }

    @Override
    public void disableConfiguration(String number) {
        enableOrderDisableConfiguration(number, false);
    }

    @Override
    public List<Configuration> query(ConfigurationDO record) {
        return query(record, true);
    }

    @Override
    public List<Configuration> query(ConfigurationDO record, boolean returnItems) {
        List<ConfigurationDO> configurationList = configurationMapper.query(record);
        if (CollectionUtils.isEmpty(configurationList)) {
            return new ArrayList<>(0);
        }
        return configurationList.stream().map(configuration -> configuration.convert(returnItems)).collect(
            Collectors.toList());
    }

    @Override
    public List<ConfigurationKey> queryKeys(String type) {
        List<ConfigurationItemKeyDO> itemKeyDOS = findItemKeyByType(type);
        if (CollectionUtils.isEmpty(itemKeyDOS)) {
            return new ArrayList<>(0);
        }
        return itemKeyDOS.stream().map(ConfigurationItemKeyDO::convert).collect(Collectors.toList());
    }

    private void enableOrderDisableConfiguration(String number, boolean enable) {
        Example example = new Example(ConfigurationDO.class);
        example.createCriteria().andEqualTo("number", number).andEqualTo("deleted", 0);
        ConfigurationDO configurationDO = new ConfigurationDO();
        configurationDO.setStatus(enable ? 1 : 0);
        configurationDO.setGmtModify(new Date());
        configurationMapper.updateByExampleSelective(configurationDO, example);
    }

    private List<ConfigurationItemKeyDO> findItemKeyByType(String type) {
        return configurationMapper.findItemKeyByType(type);
    }
}
