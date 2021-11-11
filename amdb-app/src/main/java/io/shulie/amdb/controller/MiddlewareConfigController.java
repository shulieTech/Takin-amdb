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
import io.shulie.amdb.request.query.ClickhouseCreateTableRequest;
import io.shulie.amdb.request.query.MiddlewareQueryRequest;
import io.shulie.amdb.response.app.model.Configuration;
import io.shulie.amdb.service.MiddlewareConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.shulie.amdb.exception.AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC;

@RestController
@RequestMapping("amdb/db/api/middlware")
@Slf4j
public class MiddlewareConfigController {

    @Resource
    private MiddlewareConfigService middlewareConfigService;

    @GetMapping("clickhouse/query")
    public Response<List<Configuration>> queryClickhouseClusterConfig(MiddlewareQueryRequest request) {
        List<Configuration> configDOList = middlewareConfigService.queryClusterConfig(request);
        return Response.success(configDOList, configDOList.size());
    }

    @PostMapping("clickhouse/createTable")
    public Response<Object> autoCreateClickhouseTable(@RequestBody ClickhouseCreateTableRequest request) throws Exception {
        if (StringUtils.isAnyBlank(request.getClusterAddress(), request.getPassword())) {
            return Response.fail(COMMON_EMPTY_PARAM_STRING_DESC, "clusterAddress/password");
        }
        middlewareConfigService.createClickhouseClusterTable(request);
        return Response.emptySuccess();
    }
}
