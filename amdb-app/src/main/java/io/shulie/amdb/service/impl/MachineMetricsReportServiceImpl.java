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
import io.shulie.amdb.convert.MachineMetricsReportConvert;
import io.shulie.amdb.dto.MachineMetricsReportDTO;
import io.shulie.amdb.entity.MachineMetricsReportDO;
import io.shulie.amdb.mapper.AppInstanceMapper;
import io.shulie.amdb.mapper.MachineMetricsReportMapper;
import io.shulie.amdb.mapper.MiddleWareInstanceMapper;
import io.shulie.amdb.request.query.MachineMetricsReportQueryRequest;
import io.shulie.amdb.request.submit.MachineMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.MachineMetricsReportResponse;
import io.shulie.amdb.response.metrics.model.MachineMetricsReportModel;
import io.shulie.amdb.service.AppInstanceService;
import io.shulie.amdb.service.MachineMetricsReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MachineMetricsReportServiceImpl implements MachineMetricsReportService {

    @Resource
    MachineMetricsReportMapper machineMetricsReportMapper;

    @Resource
    MiddleWareInstanceMapper middleWareInstanceMapper;

    @Resource
    AppInstanceMapper appInstanceMapper;

    @Autowired
    AppInstanceService appInstanceService;

    @Override
    public Response batchInsert(List<MachineMetricsReportSubmitRequest> requestList) {
        List<MachineMetricsReportDO> machineMetricsReportDos = requestList.stream().map(
                request -> MachineMetricsReportConvert.convertRelationDO(request)
        ).collect(Collectors.toList());
        try {
            machineMetricsReportDos.forEach(machineMetricsReportDO -> {
                machineMetricsReportMapper.insert(machineMetricsReportDO);
            });
        } catch (Exception e) {
            log.error("批量新增异常", e);
            return Response.fail();
        }
        return Response.emptySuccess();
    }

    @Override
    public Response<MachineMetricsReportResponse> batchQuery(MachineMetricsReportQueryRequest request) {
        final List<String> ipList;
        if (StringUtils.isNotBlank(request.getIpList())) {
            ipList = Arrays.asList(request.getIpList().split(","));
        } else if ("app".equals(request.getServerType().toLowerCase())) {
            ipList = getAppIpList(request.getServerName());
        } else {
            ipList = getMiddleWareIpList(request.getServerType(), request.getServerName());
        }
        List<MachineMetricsReportDTO> machineMetricsReportDtos = machineMetricsReportMapper.getReportsBySamplingInterval(request.getSamplingInterval(), DateFormatUtils.format(request.getStartTime(), "yyyy-MM-dd HH:mm:ss"), DateFormatUtils.format(request.getEndTime(), "yyyy-MM-dd HH:mm:ss"), "'" + StringUtils.join(ipList, "','") + "'");
        Map<Long, MachineMetricsReportDTO> mGather = new HashMap<>();
        machineMetricsReportDtos.forEach(machineMetricsReportDTO -> {
            Long time = machineMetricsReportDTO.getM() * request.getSamplingInterval() * 1000 + request.getStartTime().getTime();
            machineMetricsReportDTO.setTime(new Date(time));
            mGather.put(time, machineMetricsReportDTO);
        });
        Set<Long> timeSet = mGather.keySet().stream().sorted().collect(Collectors.toSet());
        Map<String, List<MachineMetricsReportDTO>> mMetricsReportResponseDTOList = new HashMap<>();
        timeSet.forEach(time -> {
            String day = DateFormatUtils.format(new Date(time), "yyyy-MM-dd");
            if (mMetricsReportResponseDTOList.get(day) == null) {
                mMetricsReportResponseDTOList.put(day, new ArrayList<>());
            }
            mMetricsReportResponseDTOList.get(day).add(mGather.get(time));
        });
        List<MachineMetricsReportModel> machineMetricsReportModelList = new ArrayList();
        Set<String> days = mMetricsReportResponseDTOList.keySet();
        days.forEach(day -> {
            MachineMetricsReportModel model = new MachineMetricsReportModel();
            model.setAvgCpuUs(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgCpuUs).average().getAsDouble());
            model.setAvgIoRt(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgIoRt).average().getAsDouble());
            model.setAvgIoUs(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgIoUs).average().getAsDouble());
            model.setAvgMemUs(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgMemUs).average().getAsDouble());
            model.setMaxCpuUs(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgCpuUs).max().getAsDouble());
            model.setMaxMemUs(mMetricsReportResponseDTOList.get(day).stream().mapToDouble(MachineMetricsReportDTO::getAvgMemUs).max().getAsDouble());
            model.setDay(day);
            machineMetricsReportModelList.add(model);
        });
        MachineMetricsReportResponse machineMetricsReportResponse = new MachineMetricsReportResponse();
        machineMetricsReportResponse.setServerType(request.getServerType());
        machineMetricsReportResponse.setServerName(request.getServerName());
        machineMetricsReportResponse.setIps(ipList);
        machineMetricsReportResponse.setMetricsList(machineMetricsReportModelList);
        return Response.success(machineMetricsReportResponse);
    }

    private List<String> getAppIpList(String appName) {
        return appInstanceMapper.selectIpListByAppName(appName);
    }

    private List<String> getMiddleWareIpList(String type, String name) {
        return middleWareInstanceMapper.selectIpListByTypeAndName(type, name);
    }
}
