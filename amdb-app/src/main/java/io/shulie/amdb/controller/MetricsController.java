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

import com.google.common.collect.Lists;
import io.shulie.amdb.adaptors.common.Pair;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.MetricsDetailQueryRequest;
import io.shulie.amdb.request.query.MetricsFromInfluxdbQueryRequest;
import io.shulie.amdb.request.query.MetricsFromInfluxdbRequest;
import io.shulie.amdb.request.query.MetricsQueryRequest;
import io.shulie.amdb.response.metrics.MetricsDetailResponse;
import io.shulie.amdb.service.MetricsService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api("指标信息查询")
@RestController
@RequestMapping("amdb/db/api/metrics")
/**
 * 指标信息查询（influxdb通用查询）
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class MetricsController {

    @Autowired
    MetricsService metricsService;

    //指标数据查询
    @RequestMapping(value = "/queryMetrics", method = RequestMethod.POST)
    public Response queryIpList(@RequestBody MetricsQueryRequest request) {
        if (StringUtils.isBlank(request.getMeasurementName()) || CollectionUtils.isEmpty(request.getTagMapList())
                || request.getFieldMap() == null || request.getFieldMap().size() == 0
                || request.getStartTime() == 0 || request.getEndTime() == 0) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        return Response.success(metricsService.getMetrics(request));
    }

    @RequestMapping(value = "/metricsDetailes", method = RequestMethod.POST)
    public Response metricsDetailes(@RequestBody MetricsDetailQueryRequest request) {
        if (StringUtils.isBlank(request.getAppName()) || StringUtils.isBlank(request.getStartTime()) || StringUtils.isBlank(request.getEndTime())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        Pair<List<MetricsDetailResponse>, Integer> resultPair;
        try {
            resultPair = metricsService.metricsDetailes(request);
        } catch (Exception e) {
            return new Response(Lists.newArrayList());
        }
        Response response = Response.success(resultPair.getKey());
        response.setTotal(resultPair.getValue());
        return response;
    }

    @RequestMapping(value = "/entranceFromChickHouse", method = RequestMethod.POST)
    public Response entranceFromChickHouse(@RequestBody MetricsFromInfluxdbQueryRequest request) {
        if (!request.isNotEmptyForEntrance()) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        String entrance = metricsService.entranceFromChickHouse(request);
        Response response = Response.success(entrance);
        response.setTotal(1);
        return response;
    }

    @RequestMapping(value = "/metricFromChickHouse", method = RequestMethod.POST)
    public Response metricFromChickHouse(@RequestBody MetricsFromInfluxdbQueryRequest request) {
        if (!request.isNotEmptyForMetric()) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        Map<String, Object> resultList = metricsService.metricsFromChickHouse(request);
        Response response = Response.success(resultList);
        response.setTotal(resultList.size());
        return response;
    }

    @RequestMapping(value = "/metricFromInfluxdb", method = RequestMethod.POST)
    public Response metricFromInfluxdb(@RequestBody MetricsFromInfluxdbRequest request) {
        if ((StringUtils.isBlank(request.getEagleId()) && CollectionUtils.isEmpty(request.getEagleIds())) || request.getStartMilli() == 0 || request.getEndMilli() == 0) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        List<Map<String, Object>> resultList = metricsService.metricFromInfluxdb(request);
        Response response = Response.success(resultList);
        response.setTotal(resultList.size());
        return response;
    }

}
