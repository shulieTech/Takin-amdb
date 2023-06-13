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
import io.shulie.amdb.common.CalcType;
import io.shulie.amdb.common.IndicateMeasurementEnum;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.*;
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
    private MetricsService metricsService;

    /**
     * 查询应用线程状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryCommonMetrics", method = RequestMethod.POST)
    public Response queryAppStatThread(@RequestBody CommonMetricsQueryRequest request) {
        if (StringUtils.isBlank(request.getMetric())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        request.setCalcType(StringUtils.upperCase(request.getCalcType()));
        if (!StringUtils.equals(request.getCalcType(), CalcType.SUM.name())
                && !StringUtils.equals(request.getCalcType(), CalcType.AVG.name())) {
            request.setCalcType(CalcType.AVG.name());
        }
        return Response.success(metricsService.getCommonMetrics(request));
    }

    /**
     * 查询应用内部调用状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryAppStatIncall", method = RequestMethod.POST)
    public Response queryAppStatIncall(@RequestBody MetricsQueryRequest request) {
        request.setMeasurementName(IndicateMeasurementEnum.APP_STAT_INCALL.getMeasurementName());
        return queryMetrics(request);
    }

    /**
     * 指标数据查询
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryMetrics", method = RequestMethod.POST)
    public Response queryMetrics(@RequestBody MetricsQueryRequest request) {
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
            resultPair = metricsService.metricsDetails(request);
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
