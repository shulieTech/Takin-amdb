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
import io.shulie.amdb.common.dto.agent.AgentInfoDTO;
import io.shulie.amdb.common.dto.instance.AppInfo;
import io.shulie.amdb.common.request.agent.AmdbAgentInfoQueryRequest;
import io.shulie.amdb.common.request.app.AppInfoQueryRequest;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.TAmdbAppInstanceBatchAppQueryRequest;
import io.shulie.amdb.request.query.TAmdbAppInstanceErrorInfoByQueryRequest;
import io.shulie.amdb.request.query.TAmdbAppInstanceQueryRequest;
import io.shulie.amdb.service.AppInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("amdb/db/api/appInstance")
/**
 * 控制台应用管理
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class AppInstanceController {
    @Autowired
    private AppInstanceService appInstanceService;

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Response insert(@RequestBody TAmdbAppInstanceDO tAmdbApp) {
        return appInstanceService.insert(tAmdbApp);
    }

    @RequestMapping(value = "/insertBatch", method = RequestMethod.POST)
    public Response insertBatch(@RequestBody List<TAmdbAppInstanceDO> tAmdbApp) {
        return appInstanceService.insertBatch(tAmdbApp);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody TAmdbAppInstanceDO tAmdbApp) {
        try {
            return Response.success(appInstanceService.update(tAmdbApp));
        } catch (Exception e) {
            log.error("更新应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_UPDATE);
        }
    }

    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Response<TAmdbAppInstanceDO> select(TAmdbAppInstanceDO tAmdbApp) {
        try {
            return Response.success(appInstanceService.selectOneByParam(tAmdbApp));
        } catch (Exception e) {
            log.error("查询应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_SELECT);
        }
    }

    @RequestMapping(value = "/selectByParams", method = RequestMethod.GET)
    public Response selectByParams(TAmdbAppInstanceQueryRequest param) {
        try {
            if (param.getAppName() == null) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appInstanceService.selectByParams(param));
        } catch (Exception e) {
            log.error("查询应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_SELECT);
        }
    }

    @RequestMapping(value = "/selectByBatchAppParams", method = RequestMethod.POST)
    public Response selectByBatchAppParamsByPostMethod(@RequestBody TAmdbAppInstanceBatchAppQueryRequest param) {
        try {
            if (CollectionUtils.isEmpty(param.getAppIds()) && StringUtils.isBlank(param.getAppNames()) && CollectionUtils.isEmpty(param.getAgentIds()) && CollectionUtils.isEmpty(param.getIpAddress())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appInstanceService.selectByBatchAppParams(param));
        } catch (Exception e) {
            log.error("查询应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_SELECT);
        }
    }

    @RequestMapping(value = "/selectByBatchAppParams", method = RequestMethod.GET)
    public Response selectByBatchAppParams(TAmdbAppInstanceBatchAppQueryRequest param) {
        try {
            if (CollectionUtils.isEmpty(param.getAppIds()) && StringUtils.isBlank(param.getAppNames()) && CollectionUtils.isEmpty(param.getAgentIds()) && CollectionUtils.isEmpty(param.getIpAddress())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appInstanceService.selectByBatchAppParams(param));
        } catch (Exception e) {
            log.error("查询应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_SELECT);
        }
    }

    @RequestMapping(value = "/selectErrorInfoByParams", method = RequestMethod.POST)
    public Response selectErrorInfoByParams(@RequestBody TAmdbAppInstanceErrorInfoByQueryRequest param) {
        try {
            if (StringUtils.isBlank(param.getAppId()) && StringUtils.isBlank(param.getAppName())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(appInstanceService.selectErrorInfoByParams(param));
        } catch (Exception e) {
            log.error("查询应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_INFO_SELECT);
        }
    }

    @RequestMapping("/initOnlineStatus")
    public Response initOnlineStatus() {
        try {
            appInstanceService.initOnlineStatus();
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("更新应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_UPDATE);
        }
    }

    @RequestMapping(value = "/deleteByParams", method = RequestMethod.POST)
    public Response deleteByParams(@RequestBody TAmdbAppInstanceQueryRequest param) {
        try {
            if (StringUtils.isBlank(param.getAppName())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            appInstanceService.deleteByParams(param);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("更新应用实例失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_UPDATE);
        }
    }

    /**
     * 查询agent日志
     *
     * @return
     */
    @RequestMapping(value = "/queryAgentInfo", method = RequestMethod.POST)
    public Response<List<AgentInfoDTO>> queryAgentInfo(@RequestBody AmdbAgentInfoQueryRequest request) {
        log.info("查询agent日志 ：{}", request);
        if (request.getPageSize() == Integer.MAX_VALUE || request.getStartDate() == null || request.getEndDate() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return Response.success(appInstanceService.queryAgentInfo(request));
        } catch (Exception e) {
            log.error("查询agent日志失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    /**
     * 查询应用信息
     *
     * @return
     */
    @RequestMapping(value = "/queryAppInfo", method = RequestMethod.POST)
    public Response<List<AppInfo>> queryAppInfo(@RequestBody AppInfoQueryRequest request) {
        log.info("查询应用信息：{}", request);
        if (request.getPageSize() == Integer.MAX_VALUE || StringUtils.isBlank(request.getTenantAppKey()) || StringUtils.isBlank(request.getEnvCode())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return appInstanceService.queryAppInfo(request);
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INFO_SELECT);
        }
    }
}
