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
import io.shulie.amdb.common.dto.instance.AgentStatusStatInfo;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.AppInstanceStatusQueryRequest;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsResponse;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsSumResponse;
import io.shulie.amdb.service.AppInstanceStatusService;
import io.shulie.amdb.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("amdb/db/api/appInstanceStatus")
public class AppInstanceStatusController {
    @Autowired
    private AppInstanceStatusService appInstanceStatusService;

    @RequestMapping(value = "/queryInstanceStatus", method = RequestMethod.GET)
    public Response<List<AmdbAppInstanceStautsResponse>> queryInstanceStatus(AppInstanceStatusQueryRequest param) {
        try {
            return Response.success(appInstanceStatusService.selectByParams(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    @RequestMapping(value = "/queryInstanceStatusV2", method = RequestMethod.GET)
    public Response<List<AmdbAppInstanceStautsResponse>> queryInstanceStatusV2(AppInstanceStatusQueryRequest param) {
        try {
            if (StringUtil.isBlank(param.getAppNames()) ) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM, "appNames");
            }
            return Response.success(appInstanceStatusService.selectByParams(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    @PostMapping(value = "/queryInstanceStatusV3")
    public Response<List<AmdbAppInstanceStautsResponse>> queryInstanceStatusV3(@RequestBody AppInstanceStatusQueryRequest param) {
        try {
            return Response.success(appInstanceStatusService.selectByParams(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    @RequestMapping(value = "/queryInstanceSumInfo", method = RequestMethod.GET)
    public Response<AmdbAppInstanceStautsSumResponse> queryInstanceSumInfo(AppInstanceStatusQueryRequest param) {
        try {
            if (StringUtil.isBlank(param.getAppName())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appInstanceStatusService.queryInstanceSumInfo(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    @PostMapping(value = "/countStatus")
    public Response<AgentStatusStatInfo> queryInstanceStatusCount(@RequestBody AppInstanceStatusQueryRequest param) {
        try {
            return Response.success(appInstanceStatusService.countStatus(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

}
