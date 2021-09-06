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

package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.convert.AppServerMetricsConvert;
import io.shulie.amdb.dto.AppServerMetricsReportDTO;
import io.shulie.amdb.entity.AppServerMetricsReportDO;
import io.shulie.amdb.mapper.AppServerMetricsReportMapper;
import io.shulie.amdb.request.query.AppServerMetricsReportQueryRequest;
import io.shulie.amdb.request.submit.AppServerMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.AppServerMetricsReportResponse;
import io.shulie.amdb.service.AppServerMetricsReportService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xingchen
 * @ClassName: AppServerMetricsServiceImpl
 * @Package: io.shulie.amdb.service.impl
 * @Date: 2020/11/419:59
 * @Description:
 */
@Service
public class AppServerMetricsReportServiceImpl implements AppServerMetricsReportService {

    private static Logger logger = LoggerFactory.getLogger(AppServerMetricsReportServiceImpl.class);

    @Resource
    private AppServerMetricsReportMapper appServerMetricsReportMapper;

    /**
     * 批量新增
     *
     * @param appServerMetricsReportSubmitRequestList
     */
    @Override
    public Response batchInsert(List<AppServerMetricsReportSubmitRequest> appServerMetricsReportSubmitRequestList) {
        List<AppServerMetricsReportDO> appServerMetricsReportDOList = appServerMetricsReportSubmitRequestList.stream().map(metrics -> AppServerMetricsConvert.convertAppServerMetricsReportDO(metrics)).collect(Collectors.toList());
        try {
            if (CollectionUtils.isNotEmpty(appServerMetricsReportDOList)) {
                appServerMetricsReportDOList.forEach(appServerMetricsReportDO -> {
                    try {
                        Example example = new Example(AppServerMetricsReportDO.class);
                        Example.Criteria criteria = example.createCriteria();
                        criteria.andEqualTo("appName", appServerMetricsReportDO.getAppName());
                        criteria.andEqualTo("callType", appServerMetricsReportDO.getCallType());
                        criteria.andEqualTo("callEvent", appServerMetricsReportDO.getCallEvent());
                        criteria.andEqualTo("statisticalStart", appServerMetricsReportDO.getStatisticalStart());
                        if (appServerMetricsReportMapper.selectCountByExample(example) == 0) {
                            appServerMetricsReportMapper.insert(appServerMetricsReportDO);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("batchInsert error", e);
            return Response.fail(e.getMessage());
        }
        return Response.emptySuccess();
    }

    /**
     * @param request
     * @return
     */
    @Override
    public List<AppServerMetricsReportResponse> batchQuery(AppServerMetricsReportQueryRequest request) {
        List<AppServerMetricsReportDTO> reportDTOList = appServerMetricsReportMapper.getReportsBySamplingInterval(request.getSamplingInterval(), DateFormatUtils.format(request.getTimeScopeStart(), "yyyy-MM-dd HH:mm:ss"), DateFormatUtils.format(request.getTimeScopeEnd(), "yyyy-MM-dd HH:mm:ss"), request.getBelongApp(), request.getApiUrl(), request.getApiProtocol());
        if (CollectionUtils.isEmpty(reportDTOList)) {
            return new ArrayList<>();
        }
        Map<String, List<AppServerMetricsReportDTO>> dataMap = new HashMap<>();
        reportDTOList.forEach(reportDTO -> {
            String day = DateFormatUtils.format(new Date(reportDTO.getM() * request.getSamplingInterval() * 1000 + request.getTimeScopeEnd().getTime()), "yyyy-MM-dd");
            if (dataMap.get(day) == null) {
                dataMap.put(day, new ArrayList<>());
            }
            dataMap.get(day).add(reportDTO);
        });
        List<AppServerMetricsReportResponse> responseList = new ArrayList<>();
        for (String key : dataMap.keySet()) {
            AppServerMetricsReportResponse response = new AppServerMetricsReportResponse();
            List<AppServerMetricsReportDTO> metricsReportDtos = dataMap.get(key);
            response.setAvgP95(metricsReportDtos.stream().mapToDouble(AppServerMetricsReportDTO::getAvgP95).average().getAsDouble());
            response.setMaxP95(metricsReportDtos.stream().mapToDouble(AppServerMetricsReportDTO::getAvgP95).max().getAsDouble());
            response.setMinP95(metricsReportDtos.stream().mapToDouble(AppServerMetricsReportDTO::getAvgP95).min().getAsDouble());
            response.setAvgQps(metricsReportDtos.stream().mapToDouble(AppServerMetricsReportDTO::getAvgQps).average().getAsDouble());
            response.setDay(key);
            responseList.add(response);
        }
        return responseList;
    }
}
