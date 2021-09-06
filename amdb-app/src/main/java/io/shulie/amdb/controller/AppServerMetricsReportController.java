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
import io.shulie.amdb.constant.ApiConst;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.AppServerMetricsReportQueryRequest;
import io.shulie.amdb.request.submit.AppServerMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.AppServerMetricsReportResponse;
import io.shulie.amdb.service.AppServerMetricsReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: xingchen
 * @ClassName: AppServerMetricsReportController
 * @Package: io.shulie.amdb.controller
 * @Date: 2020/11/419:49
 * @Description:
 */
@RestController
@RequestMapping(value = ApiConst.BASE_API + "/apiPerformanceMetricsReport")
@Api("API性能指标报表")
/**
 * 服务性能指标信息
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class AppServerMetricsReportController {

    @Autowired
    private AppServerMetricsReportService appServerMetricsReportService;

    /**
     * 批量新增
     *
     * @param appServerMetricsReportSubmitRequestList
     * @return
     */
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Response<String> addList(@ApiParam(value = "API性能指标报表数据批量插入", required = true)@RequestBody List<AppServerMetricsReportSubmitRequest> appServerMetricsReportSubmitRequestList) {
        return appServerMetricsReportService.batchInsert(appServerMetricsReportSubmitRequestList);
    }

    /**
     * 批量查询
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/batchQuery", method = RequestMethod.POST)
    public Response<List<AppServerMetricsReportResponse>> list(@RequestBody AppServerMetricsReportQueryRequest request) {
        if(request.getTimeScopeStart()==null&&request.getTimeScopeEnd()==null){
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        return Response.success(appServerMetricsReportService.batchQuery(request));
    }
}
