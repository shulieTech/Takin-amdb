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
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.entity.AppShadowBizTableDO;
import io.shulie.amdb.entity.AppShadowDatabaseDO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.AppShadowBizTableRequest;
import io.shulie.amdb.request.query.AppShadowDatabaseRequest;
import io.shulie.amdb.request.query.TAmdbAppBatchAppQueryRequest;
import io.shulie.amdb.response.app.AmdbAppResponse;
import io.shulie.amdb.service.AppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(description = "应用管理")
@RestController
@RequestMapping("amdb/db/api/app")
/**
 * 控制台应用管理
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class AppController {
    @Autowired
    private AppService appService;

    @ApiOperation(value = "新增")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Response insert(@RequestBody AppDO app) {
        try {
            appService.insert(app);
            return Response.success(app.getId());
        } catch (Exception e) {
            log.error("新增应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @ApiOperation(value = "新增")
    @RequestMapping(value = "/insertAsync", method = RequestMethod.POST)
    public Response insertAsync(@RequestBody AppDO app) {
        try {
            appService.insertAsync(app);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("新增应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @RequestMapping(value = "/insertBatch", method = RequestMethod.POST)
    public Response insertBatch(@RequestBody List<AppDO> app) {
        try {
            return Response.success(appService.insertBatch(app));
        } catch (Exception e) {
            log.error("新增应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody AppDO app) {
        try {
            return Response.success(appService.update(app));
        } catch (Exception e) {
            log.error("更新应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @RequestMapping(value = "/updateBatch", method = RequestMethod.POST)
    public Response updateBatch(@RequestBody List<AppDO> app) {
        try {
            return Response.success(appService.updateBatch(app));
        } catch (Exception e) {
            log.error("更新应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Response delete(@RequestBody AppDO app) {
        try {
            return Response.success(appService.delete(app));
        } catch (Exception e) {
            log.error("更新应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_UPDATE);
        }
    }

    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Response select(AppDO app) {
        if (app == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return Response.success(appService.selectOneByParam(app));
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @RequestMapping(value = "/selectByFilter", method = RequestMethod.GET)
    public Response selectByFilter(String filter) {
        try {
            return Response.success(appService.selectByFilter(filter));
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @RequestMapping(value = "/selectByBatchAppParams", method = RequestMethod.GET)
    public Response<List<AmdbAppResponse>> selectByBatchAppParams(TAmdbAppBatchAppQueryRequest param) {
        try {
            if (CollectionUtils.isEmpty(param.getAppIds()) && CollectionUtils.isEmpty(param.getAppNames())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appService.selectByBatchAppParams(param));
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @RequestMapping(value = "/selectByBatchAppParamsOnPostMetd", method = RequestMethod.POST)
    public Response<List<AmdbAppResponse>> selectByBatchAppParamsOnPostMetd(@RequestBody TAmdbAppBatchAppQueryRequest param) {
        try {
            if (CollectionUtils.isEmpty(param.getAppIds()) && CollectionUtils.isEmpty(param.getAppNames())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appService.selectByBatchAppParams(param));
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @RequestMapping(value = "/selectAllAppName", method = RequestMethod.GET)
    public Response selectAllAppName(TAmdbAppBatchAppQueryRequest param) {
        try {
            return Response.success(appService.selectAllAppName(param));
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @ApiOperation(value = "应用影子库/表查询")
    @GetMapping(value = "/selectShadowDatabases")
    public Response<List<AppShadowDatabaseDO>> selectShadowDatabases(AppShadowDatabaseRequest request) {
        if (StringUtils.isBlank(request.getAppName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "appName");
        }
        try {
            return Response.success(appService.selectShadowDatabase(request));
        } catch (Exception e) {
            log.error("查询应用影子库/表失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }

    @ApiOperation(value = "应用业务表查询")
    @GetMapping(value = "/selectShadowBizTables")
    public Response<List<AppShadowBizTableDO>> selectShadowBizTables(AppShadowBizTableRequest request) {
        if (StringUtils.isAnyBlank(request.getAppName(), request.getDataSource(), request.getTableUser())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "appName/dataSource/tableUser");
        }
        try {
            return Response.success(appService.selectShadowBizTables(request));
        } catch (Exception e) {
            log.error("查询应用业务表失败", e);
            return Response.fail(AmdbExceptionEnums.APP_SELECT);
        }
    }
}
