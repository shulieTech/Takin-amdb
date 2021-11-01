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
import io.shulie.amdb.request.query.TraceMetricsRequest;
import io.shulie.amdb.response.e2e.E2ENodeErrorInfosResponse;
import io.shulie.amdb.response.e2e.E2ENodeMetricsResponse;
import io.shulie.amdb.response.e2e.E2EStatisticsResponse;
import io.shulie.amdb.service.TraceMetricsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("指标信息查询")
@RestController
@RequestMapping("amdb/db/api/traceMetric")
@Slf4j
public class TraceMetricsController {

    @Autowired
    TraceMetricsService traceMetricsService;

    //指标数据查询
    @RequestMapping(value = "/getSqlStatements", method = RequestMethod.POST)
    public Response getSqlStatements(@RequestBody TraceMetricsRequest request) {
        if (request.getStartTime() == 0 || request.getEndTime() == 0 || StringUtils.isBlank(request.getEdgeIds())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        return Response.success(traceMetricsService.getSqlStatements(request));
    }

    @RequestMapping(value = "/nodeMetrics", method = RequestMethod.POST)
    public Response<List<E2ENodeMetricsResponse>> getNodeMetrics(@RequestBody TraceMetricsRequest param) {
        try {
            return traceMetricsService.getNodeMetrics(param);
        } catch (Exception e) {
            log.error("metrics指标数据查询失败", e);
            return Response.fail(AmdbExceptionEnums.METRICS_NODE_QUERY);
        }
    }

    @RequestMapping(value = "/nodeErrorInfos", method = RequestMethod.POST)
    public Response<List<E2ENodeErrorInfosResponse>> getNodeErrorInfos(@RequestBody TraceMetricsRequest param) {
        try {
            return traceMetricsService.getNodeErrorInfos(param);
        } catch (Exception e) {
            log.error("节点异常信息查询失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_NODE_QUERY);
        }
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public Response<List<E2EStatisticsResponse>> getStatistics(@RequestBody TraceMetricsRequest param) {
        try {
            return traceMetricsService.getStatistics(param);
        } catch (Exception e) {
            log.error("统计信息查询失败", e);
            return Response.fail(AmdbExceptionEnums.E2E_NODE_QUERY);
        }
    }

}
