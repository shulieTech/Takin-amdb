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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.request.query.ConfigQueryRequest;
import io.shulie.amdb.request.submit.ConfigAddOrUpdateSubmitRequest;
import io.shulie.amdb.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("配置管理")
@RestController
@RequestMapping("amdb/db/api/config")
/**
 * 全局配置
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class ConfigController {

    @Autowired
    ConfigService configService;

    @ApiOperation(value = "新增或修改配置")
    @RequestMapping("/updateConfig")
    public Response updateConfig(ConfigAddOrUpdateSubmitRequest submitRequest) {
        return configService.updateOrInsertConfig(submitRequest);
    }

    @ApiOperation(value = "获取配置信息")
    @RequestMapping("/getConfig")
    public Response getConfig(ConfigQueryRequest queryRequest) {
        return Response.success(configService.getConfigValue(queryRequest));
    }
}
