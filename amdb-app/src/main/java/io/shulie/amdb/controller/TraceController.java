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
import io.shulie.amdb.common.request.trace.TraceCompensateRequest;
import io.shulie.amdb.common.request.trace.TraceStackQueryParam;
import io.shulie.amdb.dto.LogResultDTO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.LogResultRequest;
import io.shulie.amdb.service.TraceService;
import io.shulie.amdb.utils.StringUtil;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Api(description = "trace??????")
@RequestMapping(value = "/amdb/trace")
/**
 * ???????????????
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class TraceController {
    private Logger logger = LoggerFactory.getLogger(TraceController.class);
    @Autowired
    private TraceService traceService;

    /**
     * T3????????????Trace????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getAllTraceList", method = RequestMethod.POST)
    public Response<List<TTrackClickhouseModel>> getAllTraceList(@RequestBody EntryTraceQueryParam param) {
        logger.info("??????????????????(????????????) ????????????:{}", param);
        if (param.getStartTime() == null || param.getEndTime() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "startTime | endTime");
        }
        try {
            return traceService.getAllTraceList(param);
        } catch (Exception e) {
            logger.error("??????????????????(????????????) ??????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * ??????????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getDebugTraceList", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getDebugTraceList(EntryTraceQueryParam param) {
        logger.info("??????????????????(????????????)????????????:{}", param);
        try {
            //?????????????????????????????????
            param.setQuerySource("debug");
            //?????????????????????????????????
            param.setQueryType(2);
            return traceService.getEntryTraceListByTaskId(param);
        } catch (Exception e) {
            logger.error("????????????(????????????)????????????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * ????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getEntryTraceList", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getEntryTraceList(EntryTraceQueryParam param) {
        logger.info("?????????????????? ????????????:{}", param);
        //&& "2".equals(param.getResultType())
        if (StringUtils.isNotBlank(param.getTaskId())) {
            return traceService.getEntryTraceListByTaskId(param);
        }
        try {
            return traceService.getEntryTraceInfo(param);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * ????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getAppNameByUrl", method = RequestMethod.GET)
    public Response<String> getAppNameByUrl(EntryTraceQueryParam param) {
        logger.info("?????????????????????????????????????????? ????????????:{}", param);
        if (StringUtils.isBlank(param.getServiceName()) || StringUtils.isBlank(param.getMethodName()) || param.getStartTime() == null || param.getEndTime() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "serviceName or methodName or startTime or endTime");
        }
        return traceService.getAppNameByUrl(param);
    }

    /**
     * ??????url???????????????????????????request
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getAppAndReqByUrl", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getAppAndReqByUrl(EntryTraceQueryParam param) {
        logger.info("????????????????????????????????????????????????????????? ????????????:{}", param);
        if (StringUtils.isBlank(param.getServiceName()) || StringUtils.isBlank(param.getMethodName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "serviceName or methodName ");
        }
        return traceService.getAppAndReqByUrl(param);
    }

    /**
     * ????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getEntryTraceListByTaskId", method = RequestMethod.GET)
    public Response<List<EntryTraceInfoDTO>> getEntryTraceListByTaskId(EntryTraceQueryParam param) {
        logger.info("??????????????????(????????????ID??????)????????????:{}", param);
        if (StringUtils.isBlank(param.getTaskId())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "taskId");
        }
        try {
            return traceService.getEntryTraceListByTaskId(param);
        } catch (Exception e) {
            logger.error("??????????????????(????????????ID??????)??????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/getTraceListByTraceIdList", method = RequestMethod.POST)
    public Response<List<RpcBased>> getTraceListByTraceIdList(@RequestBody List<String> traceIdList) {
        if (CollectionUtils.isEmpty(traceIdList)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "taskId");
        }
        try {
            List<RpcBased> rpcBasedList = traceService.getTraceListByTraceIdList(traceIdList);
            Response<List<RpcBased>> response = Response.success(rpcBasedList);
            response.setTotal(rpcBasedList == null ? 0 : rpcBasedList.size());
            return response;
        } catch (Exception e) {
            logger.error("??????????????????(??????traceIdList)??????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_QUERY);
        }
    }


    /**
     * ???????????????
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getTraceDetail", method = RequestMethod.GET)
    public Response<List<RpcBased>> getTraceDetail(TraceStackQueryParam param) {
        logger.info("???????????????:{}", param);
        if (StringUtil.isBlank(param.getTraceId())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "traceId");
        }
        try {
            List<RpcBased> rpcBasedList = traceService.getTraceDetail(param);
            //??????????????????????????????????????????????????????
            //RpcStack rpcStack = ProtocolParserFactory.getFactory().parseRpcStackByRpcBase(param.getTraceId(), rpcBasedList);

            Response<List<RpcBased>> response = Response.success(rpcBasedList);
            response.setTotal(rpcBasedList == null ? 0 : rpcBasedList.size());
            return response;
        } catch (Exception e) {
            logger.error("?????????????????????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_DETAIL_QUERY);
        }
    }

    /**
     * trace????????????
     *
     * @param logResultRequest
     * @return
     */
    @RequestMapping(value = "/log/query", method = RequestMethod.GET)
    public Response<List<LogResultDTO>> logQuery(LogResultRequest logResultRequest) {
        return Response.success(new ArrayList<>());
    }

    /**
     * trace????????????
     *
     * @return
     */
    @RequestMapping(value = "/compensate", method = RequestMethod.POST)
    public Response<String> compensate(@RequestBody TraceCompensateRequest request) {
        if (request.getResourceId() == null || request.getJobId() == null || StringUtils.isBlank(request.getCallbackUrl())) {
            return new Response<>("????????????");
        }

        try {
            traceService.startCompensate(request);
        } catch (Exception e) {
            logger.error("??????trace????????????", e);
            return Response.fail(AmdbExceptionEnums.TRACE_COMPENSATE_ERROR, "??????trace????????????");
        }
        return new Response<>("200");
    }


    @RequestMapping(value = "/scriptCsvDown", method = RequestMethod.GET)
    public void scriptCsvDown(HttpServletRequest request, HttpServletResponse response) {
        //select distinct request from t_trace_all where appName = 't3_release_operation-center' and parsedServiceName = 'com.t3.ts.operation.center.service.recommend.RecommendTakePointGuideService:0.0.0' and parsedMethod = 'queryPointGuideInfoByPointId(int)' and request != ''
        //select appName,parsedServiceName,parsedMethod from t_trace_all where request != '' group by appName,parsedServiceName,parsedMethod
        //select distinct  request from t_trace_all where startDate >= '2021-10-25 00:00:00' and  rpcType = 0 and  request != '' group by request limit 1000;
        //??????????????????????????????????????????????????????????????????
        String rpcType = (String) request.getParameter("rpcType");
        String serviceName = (String) request.getParameter("serviceName");
        String methodName = (String) request.getParameter("methodName");
        String appName = (String) request.getParameter("appName");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String filter = (String) request.getParameter("filter");
        String replace = (String) request.getParameter("replace");
        List<String> filterArr = null;
        List<String> replaceArr = null;
        if (StringUtils.isNotBlank(filter)) {
            filterArr = Arrays.asList(filter.split(","));
        }
        if (StringUtils.isNotBlank(replace)) {
            replaceArr = Arrays.asList(replace.split(","));
        }

        Map<String, String> fieldStrategy = new HashMap<>();
        if (CollectionUtils.isNotEmpty(replaceArr)) {
            replaceArr.forEach(value -> {
                fieldStrategy.put(value.split("@@")[0], value.split("@@")[1]);
            });
        }

        TraceStackQueryParam param = new TraceStackQueryParam();
        param.setRpcType(rpcType);
        param.setServiceName(serviceName);
        param.setMethodName(methodName);
        param.setAppName(appName);
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        //????????????
        if (StringUtils.isBlank(param.getAppName()) || StringUtils.isBlank(param.getServiceName()) ||
                StringUtils.isBlank(param.getMethodName()) || StringUtils.isBlank(param.getRpcType())) {
            //return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "appName || serviceName || method || rpcType");
        }

        //?????????HTTP?????????,???????????????
        if (!"0".equals(param.getRpcType())) {
            //return Response.fail(AmdbExceptionEnums.TRACE_QUERY_WARN, "????????????,?????????HTTP??????");
        }

        List<Map<String, Object>> resultList = traceService.queryInterfaceParam(param);
        if (!resultList.isEmpty()) {

            File directory = new File("/tmp/amdb");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = param.getAppName() + "_" + param.getServiceName().replaceAll("/", "%") + "_" + param.getMethodName() + "_" + System.currentTimeMillis() + ".csv";
            File csvFile = new File(directory, fileName);
            if (!csvFile.exists()) {
                try {
                    csvFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            BufferedOutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(csvFile));
                BufferedOutputStream finalOutputStream = outputStream;
                AtomicInteger i = new AtomicInteger();
                List<String> finalFilterArr = filterArr;
                resultList.stream().forEach(result -> {
                    i.getAndIncrement();
                    StringBuilder stringBuilder = new StringBuilder();
                    result.forEach((k, v) -> {
                        if (CollectionUtils.isNotEmpty(finalFilterArr)) {
                            if (!finalFilterArr.contains(k)) {
                                replaceValue(fieldStrategy, stringBuilder, k, v);
                            }
                        } else {
                            //?????????????????????
                            replaceValue(fieldStrategy, stringBuilder, k, v);
                        }
                    });
                    //???????????????
                    if (stringBuilder.length() > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
                        if (i.get() != resultList.size()) {
                            stringBuilder.append("\n");
                        }
                        try {
                            finalOutputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //????????????
            if (csvFile.exists()) {
                response.setContentType("application/force-download");// ???????????????????????????
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(csvFile);
                    bis = new BufferedInputStream(fis);
                    OutputStream responseOutputStream = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        responseOutputStream.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    //??????????????????????????????
                    csvFile.delete();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void replaceValue(Map<String, String> fieldStrategy, StringBuilder stringBuilder, String k, Object v) {
        //?????????????????????
        if (!fieldStrategy.isEmpty() && fieldStrategy.containsKey(k)) {
            String strategy = fieldStrategy.get(k);
            switch (strategy) {
                case "md5":
                    stringBuilder.append(md5(v)).append(",");
                    break;
                default:
                    stringBuilder.append(v).append(",");
            }
        } else {
            //?????????????????????,????????????
            if ("password".equals(k)) {
                stringBuilder.append("********").append(",");
                //??????????????????,??????4???????????????
            } else if ("mobile".equals(k)) {
                stringBuilder.append(v.toString().substring(0, 3) + "****" + v.toString().substring(7)).append(",");
            } else {
                stringBuilder.append(v).append(",");
            }
        }
    }

    private Object md5(Object param) {
        return Md5Utils.md5((String) param);
    }
}
