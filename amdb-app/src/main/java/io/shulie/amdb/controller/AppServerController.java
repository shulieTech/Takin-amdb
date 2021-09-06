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
import io.shulie.amdb.entity.TAmdbAppServer;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.AppServerQueryRequest;
import io.shulie.amdb.service.AppServerService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(description = "应用服务管理")
@RestController
@RequestMapping("amdb/db/api/appServer")
/**
 * 应用服务接口信息
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class AppServerController {
    @Autowired
    private AppServerService appServerService;

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Response insert(@RequestBody TAmdbAppServer tAmdbApp) {
        return Response.success(appServerService.insert(tAmdbApp));
    }

    @RequestMapping(value = "/insertBatch", method = RequestMethod.POST)
    public Response insertBatch(@RequestBody List<TAmdbAppServer> tAmdbApp) {
        return Response.success(appServerService.insertBatch(tAmdbApp));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody TAmdbAppServer tAmdbApp) {
        return Response.success(appServerService.update(tAmdbApp));
    }

    @RequestMapping(value = "/updateBatch", method = RequestMethod.POST)
    public Response updateBatch(@RequestBody List<TAmdbAppServer> tAmdbApp) {
        return Response.success(appServerService.updateBatch(tAmdbApp));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Response delete(@RequestBody TAmdbAppServer tAmdbApp) {
        return Response.success(appServerService.delete(tAmdbApp));
    }

    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public Response select(@RequestBody TAmdbAppServer tAmdbApp) {
        return Response.success(appServerService.select(tAmdbApp));
    }

    @RequestMapping(value = "/selectBatch", method = RequestMethod.GET)
    public Response selectBatch(AppServerQueryRequest appServerQueryRequest) {
        if (appServerQueryRequest == null || StringUtils.isEmpty(appServerQueryRequest.getAppName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        return Response.success(appServerService.selectBatch(appServerQueryRequest));
    }

    @RequestMapping(value ="/deleteByParams", method = RequestMethod.POST)
    public Response deleteByParams(@RequestBody AppServerQueryRequest param) {
        if(param == null || StringUtils.isEmpty(param.getAppName())){
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        appServerService.deleteByParams(param);
        return Response.emptySuccess();
    }
}
