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
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.E2EAssertRequest;
import io.shulie.amdb.request.query.E2ENodeRequest;
import io.shulie.amdb.service.E2EService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api("e2e接口")
@RestController
@RequestMapping("amdb/db/api/e2e")
/**
 * 巡检指标查询 --e2e
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class E2EController {

    @Autowired
    E2EService e2eService;

//    @RequestMapping(value = "/nodeMetrics", method = RequestMethod.POST)
//    public Response<List<E2ENodeMetricsResponse>> getNodeMetrics(@RequestBody List<E2ENodeMetricsRequest> param) {
//        try {
//            return e2eService.getNodeMetrics(param);
//        } catch (Exception e) {
//            log.error("节点指标数据查询失败", e);
//            return Response.fail(AmdbExceptionEnums.E2E_NODE_QUERY);
//        }
//    }

//    @RequestMapping(value = "/nodeErrorInfos", method = RequestMethod.POST)
//    public Response<List<E2ENodeErrorInfosResponse>> getNodeErrorInfos(@RequestBody List<E2ENodeErrorInfosRequest> param) {
//        try {
//            return e2eService.getNodeErrorInfos(param);
//        } catch (Exception e) {
//            log.error("节点异常信息查询失败", e);
//            return Response.fail(AmdbExceptionEnums.E2E_NODE_QUERY);
//        }
//    }
//
//    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
//    public Response<List<E2EStatisticsResponse>> getStatistics(@RequestBody List<E2EStatisticsRequest> param) {
//        try {
//            return e2eService.getStatistics(param);
//        } catch (Exception e) {
//            log.error("统计信息查询失败", e);
//            return Response.fail(AmdbExceptionEnums.E2E_NODE_QUERY);
//        }
//    }

    @RequestMapping(value = "/addNode", method = RequestMethod.POST)
    public Response addNode(@RequestBody E2ENodeRequest request) {
        try {
            e2eService.addNode(request);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_NODE_UPDATE);
        }
    }

    @RequestMapping(value = "/removeNode", method = RequestMethod.POST)
    public Response removeNode(@RequestBody E2ENodeRequest request) {
        try {
            e2eService.removeNode(request);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_NODE_UPDATE);
        }
    }

    /**
     * 添加断言
     *
     * @param request
     */
    @RequestMapping(value = "/addAssert", method = RequestMethod.POST)
    public Response addAssert(@RequestBody E2EAssertRequest request) {
        try {
            e2eService.addAssert(request);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("添加断言失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_ASSERT_QUERY);
        }
    }

    /**
     * 删除断言
     *
     * @param request
     */
    @RequestMapping(value = "/removeAssert", method = RequestMethod.POST)
    public Response removeAssert(@RequestBody E2EAssertRequest request) {
        try {
            e2eService.removeAssert(request);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("删除断言失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_ASSERT_UPDATE);
        }
    }

    /**
     * 调整断言
     *
     * @param request
     */
    @RequestMapping(value = "/modifyAssert", method = RequestMethod.POST)
    public Response modifyAssert(@RequestBody E2EAssertRequest request) {
        try {
            e2eService.modifyAssert(request);
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("调整断言失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_ASSERT_UPDATE);
        }
    }
}
