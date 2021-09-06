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
import io.shulie.amdb.request.query.MachineMetricsReportQueryRequest;
import io.shulie.amdb.request.submit.MachineMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.MachineMetricsReportResponse;
import io.shulie.amdb.service.MachineMetricsReportService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("机器性能指标查询")
@RestController
@RequestMapping("amdb/db/api/machineMetrics")

/**
 * 机器指标查询
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class MachineMetricsReportController {

    @Autowired
    MachineMetricsReportService machineMetricsReportService;

    //批量写入指标数据
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Response batchInsert(@RequestBody List<MachineMetricsReportSubmitRequest> requestList) {
        return machineMetricsReportService.batchInsert(requestList);
    }

    //指标数据查询
    @RequestMapping(value = "/batchQuery", method = RequestMethod.POST)
    public Response<MachineMetricsReportResponse> batchQuery(@RequestBody MachineMetricsReportQueryRequest request) {
        if (request == null || StringUtils.isBlank(request.getServerName()) || StringUtils.isBlank(request.getServerType())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "serverName,serverType");
        }
        return machineMetricsReportService.batchQuery(request);
    }
}
