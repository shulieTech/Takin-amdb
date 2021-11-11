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

package io.shulie.amdb.controller;

import java.util.List;

import javax.annotation.Resource;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.ConfigurationDO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.submit.ConfigurationSubmitRequest;
import io.shulie.amdb.response.app.model.Configuration;
import io.shulie.amdb.response.app.model.ConfigurationKey;
import io.shulie.amdb.service.ConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("amdb/db/api/configuration")
@Slf4j
@Api("amdb配置")
public class ConfigurationController {

    @Resource
    private ConfigurationService configurationService;

    // 新增配置
    @ApiOperation(value = "新增配置")
    @PostMapping("/add")
    public Response<String> addConfiguration(@RequestBody ConfigurationSubmitRequest request) {
        if (StringUtils.isAnyBlank(request.getName(), request.getNumber(), request.getType())
            || request.getAvailableEnv() == null || CollectionUtils.isEmpty(request.getItems())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "number/name/type/availableEnv/items");
        }
        return configurationService.addOrUpdateConfiguration(request);
    }

    @PutMapping("/update")
    @ApiOperation(value = "更新配置")
    public Response<String> updateConfiguration(@RequestBody ConfigurationSubmitRequest request) {
        if (StringUtils.isAnyBlank(request.getNumber(), request.getName(), request.getType())
            || CollectionUtils.isEmpty(request.getItems())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "number/name/type/items");
        }
        return configurationService.addOrUpdateConfiguration(request);
    }

    @PutMapping("/enable/{number}")
    @ApiOperation(value = "启用配置")
    public Response<Object> enable(@PathVariable("number") String number) {
        if (StringUtils.isBlank(number)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "number");
        }
        configurationService.enableConfiguration(number);
        return Response.emptySuccess();
    }

    @PutMapping("/disable/{number}")
    @ApiOperation(value = "禁用配置")
    public Response<Object> disable(@PathVariable("number") String number) {
        if (StringUtils.isBlank(number)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "number");
        }
        configurationService.disableConfiguration(number);
        return Response.emptySuccess();
    }

    @DeleteMapping("/delete/{number}")
    @ApiOperation(value = "删除配置")
    public Response<Object> delete(@PathVariable("number") String number) {
        if (StringUtils.isBlank(number)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "number");
        }
        configurationService.deleteConfiguration(number);
        return Response.emptySuccess();
    }

    @GetMapping("query")
    @ApiOperation(value = "查询配置")
    public Response<List<Configuration>> queryConfiguration(ConfigurationDO record) {
        List<Configuration> configurationList = configurationService.query(record);
        return Response.success(configurationList, configurationList.size());
    }

    @GetMapping("/{type}/keys")
    @ApiOperation(value = "查询可配置项")
    public Response<List<ConfigurationKey>> queryKeys(@PathVariable("type") String type) {
        List<ConfigurationKey> availableKeys = configurationService.queryKeys(type);
        return Response.success(availableKeys, availableKeys.size());
    }
}
