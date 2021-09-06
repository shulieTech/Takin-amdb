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

import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.trace.EntryTraceInfoDTO;
import io.shulie.amdb.common.request.trace.EntryTraceQueryParam;
import io.shulie.amdb.dto.LogResultDTO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.LogResultRequest;
import io.shulie.amdb.service.TraceService;
import io.shulie.amdb.utils.StringUtil;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Api(description = "trace查询")
@RequestMapping(value = "/amdb/trace")
/**
 * 调用链查询
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class TraceController {
    private Logger logger = LoggerFactory.getLogger(TraceController.class);
    @Autowired
    private TraceService traceService;

    /**
     * 流量明细
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getEntryTraceList", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getEntryTraceList(EntryTraceQueryParam param) {
        logger.info("流量明细查询 请求参数:{}", param);
        //&& "2".equals(param.getResultType())
        if (StringUtils.isNotBlank(param.getTaskId())) {
            return traceService.getEntryTraceListByTaskId(param);
        }
        try {
            return traceService.getEntryTraceInfo(param);
        } catch (Exception e) {
            logger.error("流量明细查询失败", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * 流量明细
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getEntryTraceListByTaskId", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getEntryTraceListByTaskId(EntryTraceQueryParam param) {
        logger.info("流量明细查询 请求参数:{}", param);
        if (StringUtils.isBlank(param.getTaskId())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "taskId");
        }
        try {
            return traceService.getEntryTraceListByTaskId(param);
        } catch (Exception e) {
            logger.error("流量明细查询失败", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * 链路详情
     *
     * @param traceId
     * @return
     */
    @RequestMapping(value = "/getTraceDetail", method = RequestMethod.GET)
    public Response<List<RpcBased>> getTraceDetail(String traceId) {
        logger.info("链路详情查询 traceId:{}", traceId);
        if (StringUtil.isBlank(traceId)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "taskId");
        }
        try {
            List<RpcBased> rpcBasedList = traceService.getTraceDetail(traceId);
            Response<List<RpcBased>> response = Response.success(rpcBasedList);
            response.setTotal(rpcBasedList == null ? 0 : rpcBasedList.size());
            return response;
        } catch (Exception e) {
            logger.error("链路详情查询失败", e);
            return Response.fail(AmdbExceptionEnums.TRACE_DETAIL_QUERY);
        }
    }

    /**
     * trace日志查询
     *
     * @param logResultRequest
     * @return
     */
    @RequestMapping(value = "/log/query", method = RequestMethod.GET)
    public Response<List<LogResultDTO>> logQuery(LogResultRequest logResultRequest) {
        return Response.success(new ArrayList<>());
    }
}
