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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.trace.EntryTraceAvgCostDTO;
import io.shulie.amdb.common.dto.trace.EntryTraceInfoDTO;
import io.shulie.amdb.common.enums.RpcType;
import io.shulie.amdb.common.request.trace.EntryTraceQueryParam;
import io.shulie.amdb.common.request.trace.TraceCompensateRequest;
import io.shulie.amdb.common.request.trace.TraceStackQueryParam;
import io.shulie.amdb.common.request.trace.TraceStatisticsQueryReq;
import io.shulie.amdb.common.request.trodata.LogCompensateCallbackRequest;
import io.shulie.amdb.common.request.trodata.LogCompensateCallbackRequest.LogCompensateCallbackData;
import io.shulie.amdb.constant.SqlConstants;
import io.shulie.amdb.dao.ITraceDao;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.queue.NoLengthBlockingQueue;
import io.shulie.amdb.service.TraceService;
import io.shulie.amdb.service.log.PushLogService;
import io.shulie.amdb.task.PressureTraceCompensateTask;
import io.shulie.amdb.utils.FileUtil;
import io.shulie.amdb.utils.StringUtil;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.link.model.TTrackClickhouseModel;
import io.shulie.surge.data.deploy.pradar.parser.MiddlewareType;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Service
public class TraceServiceImpl implements TraceService {
    private Logger logger = LoggerFactory.getLogger(TraceServiceImpl.class);

    // trace日志字段
    private static final String TRACE_SELECT_FILED
            = " appName,traceId,level,parentIndex,`index`,rpcId,rpcType,logType,traceAppName,upAppName,startTime,cost,"
            + "middlewareName,serviceName,methodName,remoteIp,port,resultCode,request,response,clusterTest,callbackMsg,"
            + "attributes,localAttributes,async,version,hostIp,agentId,parsedServiceName ";

    @Autowired
    @Qualifier("traceDaoImpl")
    ITraceDao traceDao;

    @Autowired
    private PushLogService pushLogService;

    @Value("${config.trace.limit}")
    private String traceQueryLimit;

    @Value("${config.trace.oldTask.queryDate}")
    private String queryDate;

    @Value("${config.trace.compensate.nfsdir}")
    private String nfsdir;

    @Value("${config.trace.compensate.defaultVersion}")
    private String defaultVersion;

    @Value("${config.trace.compensate.surgeAddress}")
    private String surgeAddress;

    public static final String TABLE_TRACE_AGENT = "t_trace_all";
    public static final String TABLE_TRACE_PRESSURE = "t_trace_pressure";

    private static ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(5, new DefaultThreadFactory("traceIds-query-pool"));
    }


    @Override
    public Response<List<EntryTraceInfoDTO>> getEntryTraceInfo(EntryTraceQueryParam param) {
        Boolean e2eFlag = false;
        if (StringUtils.isNotBlank(param.getQuerySource()) && "e2e".equals(param.getQuerySource())) {
            e2eFlag = true;
        }

        if (!e2eFlag && StringUtils.isBlank(param.getAppName()) && StringUtils.isBlank(param.getEntranceList())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "appName and entranceList are all empty.");
        }

        String queryTable;
        if (param.getQueryType() == 1) {
            queryTable = TABLE_TRACE_AGENT;
        } else if (param.getQueryType() == 2) {
            queryTable = TABLE_TRACE_PRESSURE;
        } else {
            queryTable = TABLE_TRACE_AGENT;
        }

        // 拼装过滤条件
        Pair<List<String>, List<String>> filters = getFilters(param, e2eFlag);
        List<String> andFilterList = filters.getFirst();
        List<String> orFilterList = filters.getSecond();
        if (isEmpty(andFilterList)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "查询条件为空");
        }
        StringBuilder sql = new StringBuilder("select " + TRACE_SELECT_FILED + " from " + queryTable + " where " + StringUtils.join(
                andFilterList, " and ")).append(SqlConstants.BLANK);
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            sql.append(" and (" + StringUtils.join(orFilterList, " or ") + ")").append(SqlConstants.BLANK);
        }
        if (StringUtils.isNotBlank(param.getSortField())) {
            sql.append("order by ").append(param.getSortField()).append(SqlConstants.BLANK);
            if (StringUtils.isNotBlank(param.getSortType())) {
                sql.append(" ").append(param.getSortType()).append(SqlConstants.BLANK);
            }
        }
        sql.append(getLimitInfo(param));
        // 入口trace列表
        List<TTrackClickhouseModel> traceModelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);

        if (e2eFlag) {
            Response result = Response.success(traceModelList.stream().map(model -> convert(model)).collect(Collectors.toList()));
            setResponseCount(andFilterList, orFilterList, result, queryTable);
            return result;
        }

        if ("dau".equals(param.getQuerySource())) {
            List<EntryTraceInfoDTO> entryTraceInfoDtos = new ArrayList<>();
            for (TTrackClickhouseModel traceModel : traceModelList) {
                EntryTraceInfoDTO entryTraceInfoDTO = new EntryTraceInfoDTO();
                entryTraceInfoDTO.setServiceName(traceModel.getParsedServiceName());
                entryTraceInfoDTO.setMethodName(traceModel.getMethodName());
                entryTraceInfoDTO.setAppName(traceModel.getAppName());
                entryTraceInfoDTO.setRemoteIp(traceModel.getRemoteIp());
                entryTraceInfoDTO.setPort(traceModel.getPort());
                entryTraceInfoDTO.setStartTime(traceModel.getStartTime());
                entryTraceInfoDTO.setRequest(traceModel.getRequest());
                entryTraceInfoDTO.setResultCode(traceModel.getResultCode());
                entryTraceInfoDTO.setCost(traceModel.getCost());
                entryTraceInfoDTO.setResponse(traceModel.getResponse());
                entryTraceInfoDTO.setAssertResult(traceModel.getCallbackMsg());
                entryTraceInfoDTO.setLocalAttributes(traceModel.getLocalAttributes());
                entryTraceInfoDTO.setTraceId(traceModel.getTraceId());
                entryTraceInfoDtos.add(entryTraceInfoDTO);
            }
            Response<List<EntryTraceInfoDTO>> result = Response.success(entryTraceInfoDtos);
            setResponseCount(andFilterList, orFilterList, result, queryTable);
            return result;
        }

        Map<String, TTrackClickhouseModel> traceId2TraceMap = traceModelList.stream().collect(
                Collectors.toMap(TTrackClickhouseModel::getTraceId, model -> model, (k1, k2) -> k1));

        // 流量引擎日志查询----流量引擎日志查询的时候加上appName和startDate，保证查询效率
        Map<String, TTrackClickhouseModel> traceId2EngineTraceMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(traceModelList) && !"dau".equals(param.getQuerySource())) {
            String engineSql = "select " + TRACE_SELECT_FILED
                    + " from " + queryTable + " where traceId in ('"
                    + StringUtils.join(traceId2TraceMap.keySet(), "','")
                    + "') and logType='5' and appName='pressure-engine'";

            if (param.getStartTime() != null && param.getStartTime() > 0) {
                engineSql += " and startDate >= '"
                        + DateFormatUtils.format(new Date(param.getStartTime() + 5000), "yyyy-MM-dd HH:mm:ss")
                        + "'";
            }

            if (param.getEndTime() != null && param.getEndTime() > 0) {
                engineSql += " and startDate <= '"
                        + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss")
                        + "'";
            }
            // 查询
            List<TTrackClickhouseModel> traceEngineModelList = traceDao.queryForList(engineSql,
                    TTrackClickhouseModel.class);
            traceId2EngineTraceMap = traceEngineModelList.stream().collect(
                    Collectors.toMap(TTrackClickhouseModel::getTraceId, model -> model, (k1, k2) -> k1));
        }

        // 合并结果
        List<EntryTraceInfoDTO> entryTraceInfoDtos = mergeEngineTraceAndTrace(traceId2EngineTraceMap, traceId2TraceMap);
        Response<List<EntryTraceInfoDTO>> result = Response.success(entryTraceInfoDtos);
        setResponseCount(andFilterList, orFilterList, result, queryTable);
        return result;
    }

    @Override
    public Response<List<EntryTraceInfoDTO>> getEntryTraceListByTaskId(EntryTraceQueryParam param) {
        Boolean e2eFlag = false;
        if (StringUtils.isNotBlank(param.getQuerySource()) && "e2e".equals(param.getQuerySource())) {
            e2eFlag = true;
        }
        String queryTable;

        //配置压测报告切换的日期,之前的压测报告还需要查询老表判断,默认是2023/01/01
        //todo 控制台如果能传一个是否是旧压测报告的标识是最好的
        long now = System.currentTimeMillis();
        Calendar instance = Calendar.getInstance();
        int year = 2023;
        int month = 0;
        int date = 1;
        if (StringUtils.isNotBlank(queryDate)) {
            year = Integer.parseInt(queryDate.split("-")[0].replaceAll("^(0+)", ""));
            month = Integer.parseInt(queryDate.split("-")[1].replaceAll("^(0+)", "")) - 1;
            //默认当天的压测报告保留4天
            date = Integer.parseInt(queryDate.split("-")[2].replaceAll("^(0+)", "")) + 4;
        }
        instance.set(year, month, date, 0, 0, 0);
        long splitTime = instance.getTime().getTime();
        Boolean isOldReport = false;
        if (now < splitTime) {
            logger.info("查询老压测报告数据");
            isOldReport = true;
        }

        if (param.getQueryType() == 1 && StringUtils.isBlank(param.getTaskId())) {
            queryTable = TABLE_TRACE_AGENT;
        } else if (param.getQueryType() == 2 || StringUtils.isNotBlank(param.getTaskId())) {
            queryTable = TABLE_TRACE_PRESSURE;
        } else {
            queryTable = TABLE_TRACE_AGENT;
        }

        // 拼装查询字段
        List<String> selectFields = getSelectFields(param.getFieldNames());
        if (isEmpty(selectFields)) {
            return Response.fail(AmdbExceptionEnums.TRACE_EMPTY_SELECT_FILED);
        }

        // 拼装过滤条件
        Pair<List<String>, List<String>> filters = getFilters(param, e2eFlag);
        List<String> andFilterList = filters.getFirst();
        List<String> orFilterList = filters.getSecond();
        if (isEmpty(andFilterList)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "查询条件为空");
        }

        // 流量引擎日志
        StringBuilder sql = new StringBuilder();
        sql.append("select " + TRACE_SELECT_FILED + " from " + queryTable + " where " + StringUtils.join(
                andFilterList, " and ")).append(SqlConstants.BLANK);
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            sql.append(" and (" + StringUtils.join(orFilterList, " or ") + ")").append(SqlConstants.BLANK);
        }
        if (StringUtils.isNotBlank(param.getSortField())) {
            sql.append("order by ").append(param.getSortField()).append(SqlConstants.BLANK);
            if (StringUtils.isNotBlank(param.getSortType())) {
                sql.append(" ").append(param.getSortType()).append(SqlConstants.BLANK);
            }
        }

        sql.append(getLimitInfo(param));
        List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);
        //老压测报告
        if (CollectionUtils.isEmpty(modelList) && isOldReport) {
            StringBuilder replace = sql.replace(sql.indexOf(TABLE_TRACE_PRESSURE), sql.indexOf(TABLE_TRACE_PRESSURE) + 16, TABLE_TRACE_AGENT);
            modelList = traceDao.queryForList(replace.toString(), TTrackClickhouseModel.class);
        }
        Response result = Response.success(modelList.stream().map(model -> convert(model)).collect(Collectors.toList()));
        setResponseCount(andFilterList, orFilterList, result, queryTable);
        return result;
    }


    public EntryTraceInfoDTO convert(TTrackClickhouseModel trackClickhouseModel) {
        EntryTraceInfoDTO entryTraceInfoDTO = new EntryTraceInfoDTO();
        entryTraceInfoDTO.setTraceId(trackClickhouseModel.getTraceId());
        entryTraceInfoDTO.setServiceName(trackClickhouseModel.getServiceName());
        entryTraceInfoDTO.setMethodName(trackClickhouseModel.getMethodName());
        entryTraceInfoDTO.setAppName(trackClickhouseModel.getAppName());
        entryTraceInfoDTO.setRemoteIp(trackClickhouseModel.getRemoteIp());
        entryTraceInfoDTO.setPort(trackClickhouseModel.getPort());
        entryTraceInfoDTO.setStartTime(trackClickhouseModel.getStartTime());
        entryTraceInfoDTO.setRequest(trackClickhouseModel.getRequest());
        entryTraceInfoDTO.setResultCode(trackClickhouseModel.getResultCode());
        entryTraceInfoDTO.setCost(trackClickhouseModel.getCost());
        entryTraceInfoDTO.setResponse(trackClickhouseModel.getResponse());
        entryTraceInfoDTO.setAssertResult(trackClickhouseModel.getCallbackMsg());
        entryTraceInfoDTO.setClusterTest(trackClickhouseModel.isClusterTest());
        return entryTraceInfoDTO;
    }


    /**
     * 流量引擎日志和入口日志合并
     *
     * @param traceId2EngineTraceMap 流量引擎日志
     * @param traceId2TraceMap       入口trace日志
     */
    private List<EntryTraceInfoDTO> mergeEngineTraceAndTrace
    (Map<String, TTrackClickhouseModel> traceId2EngineTraceMap,
     Map<String, TTrackClickhouseModel> traceId2TraceMap) {
        List<EntryTraceInfoDTO> entryTraceInfoDtos = new ArrayList<>();
        List<String> traceIdSet = new ArrayList<>();
        if (MapUtils.isNotEmpty(traceId2EngineTraceMap)) {
            traceIdSet.addAll(traceId2EngineTraceMap.keySet());
        }
        if (MapUtils.isNotEmpty(traceId2TraceMap)) {
            traceIdSet.addAll(traceId2TraceMap.keySet());
        }
        traceIdSet = traceIdSet.stream().distinct().collect(Collectors.toList());
        for (String traceId : traceIdSet) {
            EntryTraceInfoDTO entryTraceInfoDTO = new EntryTraceInfoDTO();
            TTrackClickhouseModel traceModel = traceId2TraceMap.get(traceId);
            TTrackClickhouseModel engineTraceModel = traceId2EngineTraceMap.get(traceId);
            if (traceModel != null) {
                entryTraceInfoDTO.setServiceName(traceModel.getParsedServiceName());
                entryTraceInfoDTO.setMethodName(traceModel.getMethodName());
                entryTraceInfoDTO.setAppName(traceModel.getAppName());
                entryTraceInfoDTO.setRemoteIp(traceModel.getRemoteIp());
                entryTraceInfoDTO.setPort(traceModel.getPort());
                entryTraceInfoDTO.setStartTime(traceModel.getStartTime());
                entryTraceInfoDTO.setRequest(traceModel.getRequest());
                entryTraceInfoDTO.setResultCode(traceModel.getResultCode());
                entryTraceInfoDTO.setCost(traceModel.getCost());
                entryTraceInfoDTO.setResponse(traceModel.getResponse());
                entryTraceInfoDTO.setAssertResult(traceModel.getCallbackMsg());
                entryTraceInfoDTO.setLocalAttributes(traceModel.getLocalAttributes());
            } else if (engineTraceModel != null) {
                entryTraceInfoDTO.setServiceName(engineTraceModel.getServiceName());
                entryTraceInfoDTO.setMethodName(engineTraceModel.getMethodName());
                entryTraceInfoDTO.setAppName(engineTraceModel.getAppName());
                entryTraceInfoDTO.setRemoteIp(engineTraceModel.getRemoteIp());
                entryTraceInfoDTO.setPort(engineTraceModel.getPort());
                entryTraceInfoDTO.setStartTime(engineTraceModel.getStartTime());
                entryTraceInfoDTO.setRequest(engineTraceModel.getRequest());
            }
            if (engineTraceModel != null) {
                entryTraceInfoDTO.setRequest(engineTraceModel.getRequest());
                entryTraceInfoDTO.setResultCode(engineTraceModel.getResultCode());
                entryTraceInfoDTO.setCost(engineTraceModel.getCost());
                entryTraceInfoDTO.setResponse(engineTraceModel.getResponse());
                entryTraceInfoDTO.setAssertResult(engineTraceModel.getCallbackMsg());
            }
            entryTraceInfoDTO.setTraceId(traceId);
            entryTraceInfoDtos.add(entryTraceInfoDTO);
        }
        return entryTraceInfoDtos;
    }

    /**
     * 解析查询字段
     *
     * @param fieldNameStr
     * @return
     */
    private List<String> getSelectFields(String fieldNameStr) {
        List<String> selectFields = new ArrayList<>();
        if (StringUtils.isBlank(fieldNameStr)) {
            return null;
        } else {
            List<String> fieldNames = Arrays.asList(fieldNameStr.split(","));
            if (fieldNames.contains("appName")) {
                selectFields.add("appName");
            }
            if (fieldNames.contains("remoteIp")) {
                selectFields.add("remoteIp");
            }
            if (fieldNames.contains("port")) {
                selectFields.add("port");
            }
            if (fieldNames.contains("resultCode")) {
                selectFields.add("resultCode");
            }
            if (fieldNames.contains("rpcType")) {
                selectFields.add("rpcType");
            }
            if (fieldNames.contains("cost")) {
                selectFields.add("cost");
            }
            if (fieldNames.contains("startTime")) {
                selectFields.add("startTime");
            }
            if (fieldNames.contains("traceId")) {
                selectFields.add("traceId");
            }
            if (fieldNames.contains("serviceName")) {
                selectFields.add("serviceName");
                selectFields.add("parsedServiceName");
            }
            if (fieldNames.contains("methodName")) {
                selectFields.add("methodName");
                selectFields.add("parsedMethod");
            }
        }
        return selectFields;
    }

    private Pair<List<String>, List<String>> getFilters(EntryTraceQueryParam param, Boolean e2eFlag) {
        List<String> andFilterList = new ArrayList<>();
        List<String> orFilterList = new ArrayList<>();

        //开始时间-结束时间
        /**
         *   1. 实况：默认最近5s~当前时间，每5s刷新一次
         *   2. 报告：默认压测开始时间~压测结束时间，无法调整时间到压测时间范围外
         *     提示：非压测时间范围内，请调整开始时间和结束时间
         *   3. 链路查询：默认最近24H~当前
         */
        //时间范围
        if (param.getStartTime() != null && param.getStartTime() > 0) {
            andFilterList.add(
                    "startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime() - 5000), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            andFilterList.add(
                    "startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        //如果开始时间/结束时间没传,默认查询最近一小时
        if (e2eFlag && param.getStartTime() == null && param.getEndTime() == null) {
            Date now = new Date();
            andFilterList.add(
                    "startDate >= '" + DateFormatUtils.format(now.getTime() - 3600 * 1000, "yyyy-MM-dd HH:mm:ss") + "'");
            andFilterList.add(
                    "startDate <= '" + DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss") + "'");
        }

        //租户信息(压力引擎没有租户信息,查询非压测报告时候再带上租户条件)
        if (StringUtils.isNotBlank(param.getTenantAppKey()) && param.getQueryType() != 2) {
            andFilterList.add("userAppKey='" + param.getTenantAppKey() + "'");
        }
        if (StringUtils.isNotBlank(param.getEnvCode()) && param.getQueryType() != 2) {
            andFilterList.add("envCode='" + param.getEnvCode() + "'");
        }

        //调用类型(中间件名称模糊匹配)
        if (StringUtils.isNotBlank(param.getMiddlewareName())) {
            andFilterList.add("middlewareName like '%" + param.getMiddlewareName().toLowerCase() + "%'");
        }


        //请求参数模糊匹配
        if (StringUtils.isNotBlank(param.getRequest())) {
            andFilterList.add("request like '%" + param.getRequest() + "%'");
        }

        // DAU特殊条件
        if ("dau".equals(param.getQuerySource())) {
            andFilterList.add("middlewareName='tomcat'");
            andFilterList.add("localAttributes like '%envCode%'");
        }

        //应用名称等值匹配
        if (StringUtils.isNotBlank(param.getAppName())) {
            if (param.getAppName().contains(",")) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append("appName in (");
                for (String app : param.getAppName().split(",")) {
                    sbuilder.append("'");
                    sbuilder.append(app);
                    sbuilder.append("'");
                    sbuilder.append(",");
                }
                sbuilder.deleteCharAt(sbuilder.lastIndexOf(","));
                sbuilder.append(")");
                andFilterList.add(sbuilder.toString());
            } else {
                andFilterList.add("appName='" + param.getAppName() + "'");
            }
        }

        //调用类型等值匹配
        if (StringUtils.isNotBlank(param.getRpcType())) {
            // web server
            if (param.getRpcType().equals(RpcType.TYPE_WEB_SERVER + "")) {
                andFilterList.add("rpcType='" + RpcType.TYPE_WEB_SERVER + "'");
            }
            // dubbo 或者 MQ 都取服务端日志
            else if (param.getRpcType().equals(RpcType.TYPE_RPC + "") || param.getRpcType().equals(
                    RpcType.TYPE_MQ + "")) {
                andFilterList.add("rpcType='" + RpcType.TYPE_WEB_SERVER + "'");
                andFilterList.add("logType='" + PradarLogType.LOG_TYPE_RPC_SERVER + "'");
            } else {
                andFilterList.add("1 = -1");
            }
        } else {
            //如果不传rpcType且查询来源是空或者tro(控制台),只查询服务端和入口日志,如果是e2e,则还需要查询压测数据
            if (StringUtils.isBlank(param.getTaskId())) {
                if (param.getQueryType() == 2) {
                    andFilterList.add("(logType = '5')");
                } else {
                    //链路查询
                    if (e2eFlag) {
                        andFilterList.add("(logType in ('1','3','5'))");
                    } else {
                        andFilterList.add("(logType='1' or logType='3')");
                    }
                }
            }
        }

        //耗时范围
        if (param.getMinCost() >= 0) {
            if (param.getMinCost() > 0 && param.getMaxCost() == 0) {
                andFilterList.add("cost >= " + param.getMinCost());
            } else if (param.getMaxCost() > 0 && param.getMaxCost() >= param.getMinCost()) {
                andFilterList.add("cost between " + param.getMinCost() + " and " + param.getMaxCost());
            }
        }

        //方法名等值匹配
        if (StringUtils.isNotBlank(param.getMethodName())) {
            andFilterList.add("parsedMethod='" + param.getMethodName() + "'");
        }

        //接口名模糊匹配
        if (StringUtils.isNotBlank(param.getServiceName())) {
            andFilterList.add("parsedServiceName like '%" + param.getServiceName() + "%'");
        }

        //入口等值匹配
        if (StringUtils.isNotBlank(param.getEntranceList())) {
            //如果是调试流量,根据业务活动筛选
            if ("debug".equals(param.getQuerySource())) {
                StringBuilder stringBuilder = new StringBuilder();
                List<String> entryList = Arrays.asList(param.getEntranceList().split(","));
                //如果为单独查询一个入口时,可能是两种情况,调试脚本查询指定业务活动数据/压测报告查看一个业务活动压测的流量明细
                String[] entranceInfo = entryList.get(0).split("#");
                if (entranceInfo.length == 4 && StringUtils.isNotBlank(entranceInfo[1])) {
                    //如果是入口规则,采取精确匹配,否则采用模糊匹配
                    if (entranceInfo[1].contains("{")) {
                        stringBuilder.append("parsedServiceName= '" + entranceInfo[1] + "'");
                    } else {
                        stringBuilder.append("parsedServiceName like '%" + entranceInfo[1] + "%'");
                    }
                    if (StringUtils.isNotBlank(entranceInfo[2])) {
                        stringBuilder.append(" and parsedMethod='" + entranceInfo[2] + "'");
                    }
                    if (StringUtils.isNotBlank(entranceInfo[3])) {
                        stringBuilder.append(" and rpcType='" + entranceInfo[3] + "'");
                    }
                    andFilterList.add(stringBuilder.toString());
                }
            } else {
                List<String> entryList = Arrays.asList(param.getEntranceList().split(","));
                entryList.forEach(entrance -> {
                    String[] entranceInfo = entrance.split("#");
                    if (param.getQueryType() == 2) {
                        if (entranceInfo.length == 4) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("(parsedServiceName like '%" + entranceInfo[1] + "%' ");
                            String methodName = entranceInfo[2];
                            if (StringUtils.isNotBlank(methodName)) {
                                builder.append(" and parsedMethod='" + methodName + "' ");
                            }
                            builder.append(" and rpcType='" + entranceInfo[3] + "') ");
                            orFilterList.add(builder.toString());
                        }
                    } else {
                        if (StringUtils.isNotBlank(entranceInfo[0]) && !"null".equals(entranceInfo[0])) {
                            orFilterList.add("(appName='" + entranceInfo[0] + "' and parsedServiceName like '%" + entranceInfo[1]
                                    + "%' and parsedMethod='" + entranceInfo[2] + "' and rpcType='" + entranceInfo[3] + "')");
                        }
                    }
                });
            }
        }

        //压测报告ID等值匹配
        if (StringUtils.isNotBlank(param.getTaskId())) {
            andFilterList.add("taskId='" + param.getTaskId() + "'");
        }

        //调用结果等值匹配
        if (StringUtils.isNotBlank(param.getResultType())) {
            if ("2".equals(param.getResultType())) {
                andFilterList.add("resultCode='05'");
            }
            if ("1".equals(param.getResultType())) {
                andFilterList.add("(resultCode='00' or resultCode='200')");
            }
            if ("0".equals(param.getResultType())) {
                andFilterList.add("(resultCode not in ('00','200','05'))");
            }
        }

        //是否压测流量等值匹配
        if (StringUtils.isNotBlank(param.getClusterTest())) {
            andFilterList.add("clusterTest='" + param.getClusterTest() + "'");
        }

        //traceId等值匹配
        if (CollectionUtils.isNotEmpty(param.getTraceIdList())) {
            andFilterList.add("traceId in ('" + StringUtils.join(param.getTraceIdList(), "','") + "')");
        }

        return new Pair<>(andFilterList, orFilterList);
    }

    private Pair<List<String>, List<String>> getFilters3(EntryTraceQueryParam param) {
        List<String> andFilterList = new ArrayList<>();
        List<String> orFilterList = new ArrayList<>();

        if (param.getStartTime() != null && param.getStartTime() > 0) {
            andFilterList.add(
                    "startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime() + 5000), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            andFilterList.add(
                    "startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "'");
        }

        //如果没有传应用参数,需要查询指定租户下的数据
        if (StringUtils.isBlank(param.getAppName())) {
            andFilterList.add("appName in ('" + StringUtils.join(param.getAppNames(), "','") + "')");
        }

        if (StringUtils.isNotBlank(param.getAppName())) {
            andFilterList.add("appName='" + param.getAppName() + "'");
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            andFilterList.add("parsedServiceName='" + param.getServiceName() + "'");
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            andFilterList.add("parsedMethod='" + param.getMethodName() + "'");
        }
        if (CollectionUtils.isNotEmpty(param.getTraceIdList())) {
            andFilterList.add("traceId in ('" + StringUtils.join(param.getTraceIdList(), "','") + "')");
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            andFilterList.add("userAppKey='" + param.getTenantAppKey() + "'");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            andFilterList.add("envCode='" + param.getEnvCode() + "'");
        }
        return new Pair<>(andFilterList, orFilterList);
    }

    private String getLimitInfo(EntryTraceQueryParam param) {
        String limit = "";
        if ((param.getPageSize() != null && param.getPageSize() > 0) || (param.getCurrentPage() != null
                && param.getCurrentPage() > 0)) {
            int pageSize = param.getPageSize();
            if (pageSize <= 0) {
                pageSize = 20;
            }
            int currentPage = param.getCurrentPage();
            if (currentPage <= 0) {
                currentPage = 1;
            }
            limit = "limit " + ((currentPage - 1) * pageSize) + "," + pageSize;
        }
        return limit;
    }

    private void setResponseCount(List<String> andFilterList, List<String> orFilterList, Response
            response, String queryTable) {
        String countSql = "select count(1) as total " + " from " + queryTable + " where " + StringUtils.join(andFilterList,
                " and ");
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            countSql += " and (" + StringUtils.join(orFilterList, " or ") + ")";
        }
        Map<String, Object> countInfo = traceDao.queryForMap(countSql);
        long total = NumberUtils.toLong("" + countInfo.get("total"), 0);
        response.setTotal(total);
    }

    private void setDistinctResponseCount(List<String> andFilterList, List<String> orFilterList, Response
            response, String queryTable) {
        String countSql = "select count(1) as total " + " from ( select distinct " + TRACE_SELECT_FILED + " from " + queryTable + " where " + StringUtils.join(andFilterList,
                " and ");
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            countSql += " and (" + StringUtils.join(orFilterList, " or ") + "))";
        } else {
            countSql += ") t";
        }
        Map<String, Object> countInfo = traceDao.queryForMap(countSql);
        long total = NumberUtils.toLong("" + countInfo.get("total"), 0);
        response.setTotal(total);
    }

    @Override
    public Response<Map<String, List<RpcBased>>> getTraceInfo(EntryTraceQueryParam param) {

        // 拼装过滤条件
        List<String> andFilterList = new ArrayList<>();
        List<String> orFilterList = new ArrayList<>();

        if (StringUtils.isNotBlank(param.getAppName())) {
            andFilterList.add("appName='" + param.getAppName() + "'");
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            andFilterList.add("parsedMethod='" + param.getMethodName() + "'");
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            andFilterList.add("parsedServiceName='" + param.getServiceName() + "'");
        }
        if (StringUtils.isNotBlank(param.getEntranceList())) {
            List<String> entryList = Arrays.asList(param.getEntranceList().split(","));
            entryList.forEach(entrance -> {
                String[] entranceInfo = entrance.split("#");
                if (StringUtils.isNotBlank(entranceInfo[0])) {
                    orFilterList.add("(appName='" + entranceInfo[0] + "' and parsedServiceName='" + entranceInfo[1]
                            + "' and parsedMethod='" + entranceInfo[2] + "' and rpcType='" + entranceInfo[3] + "')");
                }
            });
        }
        if (StringUtils.isNotBlank(param.getResultType())) {
            if ("1".equals(param.getResultType())) {
                andFilterList.add("(resultCode='00' or resultCode='200')");
            }
            if ("0".equals(param.getResultType())) {
                andFilterList.add("(resultCode<>'00' and resultCode<>'200')");
            }
        }
        if (StringUtils.isNotBlank(param.getClusterTest())) {
            andFilterList.add("clusterTest='" + param.getClusterTest() + "'");
        }
        if (param.getStartTime() != null && param.getStartTime() > 0) {
            andFilterList.add(
                    "startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime()), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            andFilterList.add(
                    "startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        // 分页
        String limit = "";
        if ((param.getPageSize() != null && param.getPageSize() > 0) || (param.getCurrentPage() != null
                && param.getCurrentPage() > 0)) {
            int pageSize = param.getPageSize();
            if (pageSize <= 0) {
                pageSize = 20;
            }
            int currentPage = param.getCurrentPage();
            if (currentPage <= 0) {
                currentPage = 1;
            }
            limit = "limit " + ((currentPage - 1) * pageSize) + "," + pageSize;
        } else {
            limit = "limit 10";
        }
        String sql = "select traceId from t_trace_all where " + StringUtils.join(
                andFilterList, " and ");
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            sql += " and (" + StringUtils.join(orFilterList, " or ") + ")";
        }
        String countSql = "select count(1) as total " + " from t_trace_all where " + StringUtils.join(andFilterList,
                " and ");
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            countSql += " and (" + StringUtils.join(orFilterList, " or ") + ")";
        }
        sql += " order by traceId desc " + limit;
        List<Map<String, Object>> modelList = traceDao.queryForList(sql);
        Map<String, List<RpcBased>> traceMap = new HashMap<>();
        for (Map<String, Object> map : modelList) {
            String traceId = String.valueOf(map.get("traceId"));
            TraceStackQueryParam tmp = new TraceStackQueryParam();
            tmp.setTraceId(traceId);
            traceMap.put(traceId, getTraceDetail(tmp));
        }
        Map<String, Object> countInfo = traceDao.queryForMap(countSql);
        long total = NumberUtils.toLong("" + countInfo.get("total"), 0);
        Response result = Response.success(traceMap);
        result.setTotal(total);
        return result;
    }

    @Override
    public List<Map<String, Object>> queryInterfaceParam(TraceStackQueryParam param) {
        //分页条数
        int pageSize = 1000;

        StringBuilder sql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();

        //先统计数量,再分页进行查询
        countSql.append("select count(1) as total from (select distinct request from t_trace_all where");
        if (StringUtil.isNotBlank(param.getStartTime()) && StringUtil.isNotBlank(param.getEndTime())) {
            countSql.append(" startDate between '" + param.getStartTime() + "' and '" + param.getEndTime() + "' and ");
        }
        countSql.append(" appName = '" + param.getAppName() + "' and parsedServiceName = '" + param.getServiceName() + "' and parsedMethod = '" + param.getMethodName() + "' and rpcType = '" + param.getRpcType() + "' and request not in ('','{}')) ");
        Map<String, Object> countInfo = traceDao.queryForMap(countSql.toString());
        long total = NumberUtils.toLong("" + countInfo.get("total"), 0);
        int page = (total / pageSize == 0) ? 1 : (int) (total % pageSize == 0 ? (total % pageSize) : total / pageSize + 1);

        for (int i = 1; i <= page; i++) {
            sql.append("select distinct request from t_trace_all where ");
            if (StringUtil.isNotBlank(param.getStartTime()) && StringUtil.isNotBlank(param.getEndTime())) {
                sql.append(" startDate between '" + param.getStartTime() + "' and '" + param.getEndTime() + "' and ");
            }
            sql.append(" appName = '" + param.getAppName() + "' and parsedServiceName = '" + param.getServiceName() + "' and parsedMethod = '" + param.getMethodName() + "' and rpcType = '" + param.getRpcType() + "' and request not in ('','{}') ");
            sql.append("limit " + ((i - 1) * pageSize) + "," + pageSize);
            List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);

            List<Map<String, Object>> resultList = new ArrayList<>();
            if (!modelList.isEmpty()) {
                modelList.stream().forEach(model -> {
                    String request = model.getRequest().trim();
                    //如果request是以{开头,}结尾,去除首尾的{}
                    if (request.startsWith("{") && request.endsWith("}")) {
                        request = request.substring(1, request.length() - 1);
                    }
                    String temp = request;
                    try {
                        Map<String, Object> resultMap = JSONObject.parseObject(request);

                        temp = temp.substring(1, temp.length() - 1);
                        String[] entryArr = temp.split(",");
                        List<String> keyList = Lists.newArrayList();
                        for (String entry : entryArr) {
                            String key = entry.split(":")[0];
                            keyList.add(key.substring(1, key.length() - 1));
                        }
                        if (!keyList.isEmpty()) {
                            LinkedHashMap linkeResult = new LinkedHashMap();
                            keyList.forEach(key -> {
                                linkeResult.put(key, resultMap.get(key));
                            });
                            resultList.add(linkeResult);
                        }
                    } catch (Exception e) {
                        logger.warn("非json格式入参:{}", request);

                        //非json格式
                        //判断是否是
                    }
                });
            }

            return resultList;
        }

        //request
        return new ArrayList<>();
    }

    @Override
    public Response<List<TTrackClickhouseModel>> getAllTraceList(EntryTraceQueryParam param) {
        // 拼装过滤条件
        Pair<List<String>, List<String>> filters = getFilters3(param);
        List<String> andFilterList = filters.getFirst();
        List<String> orFilterList = filters.getSecond();
        // 分页
        String limit = getLimitInfo(param);
        // 流量引擎日志
        StringBuilder sql = new StringBuilder("select * from t_trace_all where " + StringUtils.join(
                andFilterList, " and "));
        if (CollectionUtils.isNotEmpty(orFilterList)) {
            sql.append(" and (" + StringUtils.join(orFilterList, " or ") + ")");
        }
        //1027 三变让改成接口耗时降序排序
        sql.append(limit);

        List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);
        Response result = null;
        if (!modelList.isEmpty()) {
            result = Response.success(modelList);
            StringBuilder countSql = new StringBuilder("select count(1) as total from t_trace_all where " + StringUtils.join(andFilterList, " and "));
            if (CollectionUtils.isNotEmpty(orFilterList)) {
                sql.append(" and (" + StringUtils.join(orFilterList, " or ") + ")");
            }
            Map<String, Object> countInfo = traceDao.queryForMap(countSql.toString());
            result.setTotal(NumberUtils.toLong("" + countInfo.get("total"), 0));
            return result;
        }

        result = Response.success(new ArrayList<>());
        result.setTotal(0);
        return result;
    }

    @Override
    public Response<String> getAppNameByUrl(EntryTraceQueryParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("select appName from t_trace_all where 1=1 and logType != 5 ");
        if (param.getStartTime() != null && param.getStartTime() > 0) {
            sql.append("and startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            sql.append("and startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }

        if (StringUtils.isNotBlank(param.getMethodName())) {
            sql.append("and methodName='" + param.getMethodName() + "' ");
        }

        if (StringUtils.isNotBlank(param.getServiceName())) {
            sql.append("and serviceName='" + param.getServiceName() + "' ");
        }
        sql.append(" limit 1");

        List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);

        if (CollectionUtils.isNotEmpty(modelList)) {
            return Response.success(modelList.get(0).getAppName());
        }
        return Response.success("");
    }

    /**
     * todo 1.优化查询速度 2.增加接口开关 3.增加接口合法性校验 4.增加代码稳定性校验
     *
     * @param traceIdList
     * @return
     */
    @Override
    public List<RpcBased> getTraceListByTraceIdList(List<String> traceIdList) {
        String traceIds = StringUtils.join(traceIdList, "','");
        StringBuilder countSql = new StringBuilder();
        countSql.append("select count(1) as total from t_trace_all where 1=1 and logType != 5");
        countSql.append(" and traceId global in ");
        countSql.append("('");
        countSql.append(traceIds);
        countSql.append("')");
        Map<String, Object> countMap = traceDao.queryForMap(countSql.toString());
        long count = NumberUtils.toLong("" + countMap.get("total"), 0);
        if (count > 10000) {
            return Lists.newArrayList();
        }

        int pageSize = 1000;
        long pageNum = (count % pageSize == 0 ? count / pageSize : count / pageSize + 1);

        final CountDownLatch latch = new CountDownLatch(Integer.parseInt(pageNum + ""));
        List<RpcBased> rpcBasedList = Lists.newArrayList();

        AtomicReference<Boolean> isHappenException = new AtomicReference<>(false);
        for (int i = 0; i < pageNum; i++) {
            executorService.submit(() -> {
                StringBuilder sql = new StringBuilder();
                sql.append("select " + TRACE_SELECT_FILED + " from t_trace_all where 1=1 and logType != 5");
                sql.append(" and traceId global in ");
                sql.append("('");
                sql.append(traceIds);
                sql.append("')");
                sql.append(" limit ");
                sql.append((pageNum - 1) * pageSize);
                sql.append(",");
                sql.append(pageSize);

                try {
                    List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);
                    for (TTrackClickhouseModel model : modelList) {
                        // 所有客户端都要重新计算耗时
                        if (model.getLogType() == 2) {
                            calculateCost(model, modelList);
                        }
                    }
                    rpcBasedList.addAll(modelList.stream().map(model -> model.getRpcBased()).collect(Collectors.toList()));
                    latch.countDown();
                } catch (Exception e) {
                    isHappenException.set(true);
                    logger.error("分页查询clickhouse获取traceId列表失败{},{},查询sql如下:{}", e, e.getStackTrace(), sql);
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (isHappenException.get()) {
            throw new IllegalStateException("分页查询clickhouse获取traceId列表失败");
        }
        return rpcBasedList;
    }

    //是否回调
    private static Cache<String, Boolean> taskStautsCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build();

    private final static ExecutorService THREAD_POOL = new ThreadPoolExecutor(100, 200,
            300L, TimeUnit.SECONDS,
            new NoLengthBlockingQueue<>(), new ThreadFactoryBuilder()
            .setNameFormat("ptl-log-push-%d").build(), new ThreadPoolExecutor.AbortPolicy());
    private final ExecutorService executor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
            .setNameFormat("tarce-compensate-commit-%d").build());

    @Override
    public void startCompensate(TraceCompensateRequest request) {
        StringBuilder builder = new StringBuilder(nfsdir);
        builder.append("/").append(request.getResourceId());
        builder.append("/").append(request.getJobId());
        logger.info("current check path is {}", builder);
        String checkDirectory = builder.toString();

        Boolean isChecked = taskStautsCache.getIfPresent(checkDirectory);
        //10分钟内已经触发过校准任务
        if (isChecked != null && isChecked) {
            logger.warn("current task:{} has checked finished!!!", checkDirectory);
            return;
        }

        LogCompensateCallbackData data = new LogCompensateCallbackData();
        LogCompensateCallbackRequest callbackTakinRequest = new LogCompensateCallbackRequest(data);
        data.setPressureId(request.getJobId());
        data.setResourceId(String.valueOf(request.getResourceId()));

        //启动校准任务
        //如果不包含err文件,代表所有ptl都已经上传成功
        if (!checkIsNeedCompensate(checkDirectory)) {
            data.setCompleted(true);
            data.setContent("当前目录下无err文件,校准自动完成");
            //开始回调
            pushLogService.callbackTakin(request.getCallbackUrl(), callbackTakinRequest);
            return;
        } else {
            //启动补偿
            List<File> fileList = FileUtil.getFileList(checkDirectory, ".err");
            if (CollectionUtils.isEmpty(fileList)) {
                //校准通过
                data.setCompleted(true);
                data.setContent("当前目录下无err文件,校准自动完成");
                //开始回调
                pushLogService.callbackTakin(request.getCallbackUrl(), callbackTakinRequest);
                //开始回调
                return;
            } else {
                //异步提交,不阻塞主线程
                executor.submit(() -> compensate(request, checkDirectory, callbackTakinRequest, fileList));
            }
        }
        //写入缓存
        taskStautsCache.put(checkDirectory, true);
    }

    @Override
    public List<EntryTraceAvgCostDTO> getStatisticsTraceList(List<TraceStatisticsQueryReq> traceStatisticsQueryReqList) {
        //获取当前入口所有的traceId列表
        String traceSQL = getTraceIdsSQL(traceStatisticsQueryReqList);
        List<TTrackClickhouseModel> TraceModelList = traceDao.queryForList(traceSQL, TTrackClickhouseModel.class);
        if (CollectionUtils.isEmpty(TraceModelList)) {
            return Collections.emptyList();
        }
        List<String> traceList = TraceModelList.stream().map(TTrackClickhouseModel::getTraceId).collect(Collectors.toList());

        //获取每个入口的平均耗时数据
        String traceAvgCostSQL = getTraceAvgCost(traceList);
        List<EntryTraceAvgCostDTO> modelList = traceDao.queryForList(traceAvgCostSQL, EntryTraceAvgCostDTO.class);

        //找到每个入口耗时最大的traceId
        String getMaxCostTraceIdSQL = getMaxCostTraceIdsSQL(traceStatisticsQueryReqList);
        List<EntryTraceAvgCostDTO> maxCostList = traceDao.queryForList(getMaxCostTraceIdSQL, EntryTraceAvgCostDTO.class);
        Map<String, String> traceIdMap = maxCostList.stream()
                .collect(Collectors.toMap(traceInfo -> traceInfo.getServiceName() + traceInfo.getAppName() + traceInfo.getMethodName(),
                        EntryTraceAvgCostDTO::getTraceId, (existingValue, newValue) -> existingValue));

        //将最大的traceId放到每个入口上面去
        for (EntryTraceAvgCostDTO avgCostDTO : modelList) {
            String traceId = traceIdMap.get(avgCostDTO.getServiceName() + avgCostDTO.getAppName() + avgCostDTO.getMethodName());
            if (StringUtils.isNotBlank(traceId)) {
                avgCostDTO.setTraceId(traceId);
            }
        }

        // 压测引擎日志
        if (modelList.size() > 1) {
            modelList = modelList.stream().filter(model -> model.getLogType() != 5).collect(Collectors.toList());
        }
        return modelList;
    }

    private static String getTraceIdsSQL(List<TraceStatisticsQueryReq> traceStatisticsQueryReqList) {
        if (traceStatisticsQueryReqList.isEmpty()) {
            return ""; // 返回一个空字符串或适当的错误消息
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select traceId from t_trace_all where (");

        for (int i = 0; i < traceStatisticsQueryReqList.size(); i++) {
            TraceStatisticsQueryReq queryReq = traceStatisticsQueryReqList.get(i);
            stringBuilder.append("(serviceName = '").append(queryReq.getServiceName()).append("'")
                    .append(" and appName = '").append(queryReq.getAppName()).append("'")
                    .append(" and methodName = '").append(queryReq.getMethodName()).append("'")
                    .append(" and startDate >= '").append(queryReq.getStartTime()).append("'")
                    .append(" and startDate <= '").append(queryReq.getEndTime()).append("'")
                    .append(")");

            if (i < traceStatisticsQueryReqList.size() - 1) {
                stringBuilder.append(" or "); // 使用OR连接多个条件，假设你想要满足任何一个条件的结果
            }
        }

        stringBuilder.append(") limit 50000");
        return stringBuilder.toString();
    }

    private static String getMaxCostTraceIdsSQL(List<TraceStatisticsQueryReq> traceStatisticsQueryReqList) {
        if (traceStatisticsQueryReqList.isEmpty()) {
            return ""; // 返回一个空字符串或适当的错误消息
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select  serviceName,appName,methodName,samplingInterval,argMax(traceId, cost) AS traceId  from t_trace_all where (");

        for (int i = 0; i < traceStatisticsQueryReqList.size(); i++) {
            TraceStatisticsQueryReq queryReq = traceStatisticsQueryReqList.get(i);
            stringBuilder.append("(serviceName = '").append(queryReq.getServiceName()).append("'")
                    .append(" and appName = '").append(queryReq.getAppName()).append("'")
                    .append(" and methodName = '").append(queryReq.getMethodName()).append("'")
                    .append(" and startDate >= '").append(queryReq.getStartTime()).append("'")
                    .append(" and startDate <= '").append(queryReq.getEndTime()).append("'").append(")");
            if (i < traceStatisticsQueryReqList.size() - 1) {
                stringBuilder.append(" or "); // 使用OR连接多个条件，假设你想要满足任何一个条件的结果
            }
        }
        stringBuilder.append(") GROUP BY serviceName,appName,methodName,samplingInterval");
        stringBuilder.append(" limit 50000");
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        TraceStatisticsQueryReq req = new TraceStatisticsQueryReq();
        req.setServiceName("/order/policy/v4/policyInfoList");
        req.setMethodName("POST");
        req.setAppName("wechat-order-service");
        req.setStartTime("2024-03-19 10:56:16");
        req.setEndTime("2024-03-19 11:17:16");
        TraceStatisticsQueryReq req1 = new TraceStatisticsQueryReq();
        req1.setServiceName("t_wp_risk_information");
        req1.setMethodName("jdbc:oracle:thin:@9.23.28.15:1521/zghdb171");
        req1.setAppName("wechat-order-service");
        req1.setStartTime("2024-03-19 10:56:16");
        req1.setEndTime("2024-03-19 11:17:16");
        System.out.println(getMaxCostTraceIdsSQL(Arrays.asList(req, req1)));
    }

    private static String getTraceAvgCost(List<String> traceList) {
        if (traceList.isEmpty()) {
            return ""; // 返回一个空字符串或适当的错误消息
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select appName,serviceName,methodName,rpcId,logType,rpcType,avg(cost) as avgCost,");
        stringBuilder.append("SUM(CASE WHEN resultCode NOT IN ('200', '00') THEN 1 ELSE 0 END) AS failureCount,");
        stringBuilder.append("SUM(CASE WHEN resultCode IN ('200', '00') THEN 1 ELSE 0 END) AS successCount, COUNT(*) AS totalCount, ");
        stringBuilder.append("(SUM(CASE WHEN resultCode IN ('200', '00') THEN 1 ELSE 0 END) * 100.0) / COUNT(*) AS successRate from t_trace_all  where traceId in (");

        for (int i = 0; i < traceList.size(); i++) {
            stringBuilder.append("'").append(traceList.get(i)).append("'");
            if (i < traceList.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(") group by appName,serviceName,methodName,rpcId,logType,rpcType");
        return stringBuilder.toString();
    }

    private void compensate(TraceCompensateRequest request, String checkDirectory, LogCompensateCallbackRequest callbackTakinRequest, List<File> fileList) {
        LogCompensateCallbackData data = callbackTakinRequest.getData();
        final CountDownLatch latch = new CountDownLatch(fileList.size());
        StringBuilder fileNameBuilder = new StringBuilder();
        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);
            if (file.length() == 0) {
                latch.countDown();
                continue;
            }
            fileNameBuilder.append("[" + file.getAbsolutePath() + "(" + file.length() + ")]");
            String[] split = file.getName().split("-");

            String version = defaultVersion;
            if (split.length == 3) {
                version = split[1];
            }
            String finalVersion = version;

            //每个文件开启一个线程去上传
            THREAD_POOL.submit(new PressureTraceCompensateTask(file, pushLogService, finalVersion, surgeAddress, latch));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //校准通过
        data.setCompleted(true);
        data.setContent(checkDirectory + "目录下存在以下文件需要校准:" + fileNameBuilder + ",已校准完成");
        //开始回调
        pushLogService.callbackTakin(request.getCallbackUrl(), callbackTakinRequest);
    }


    private boolean checkIsNeedCompensate(String nfsdir) {
        return FileUtil.checkFileKeywordExists(nfsdir, ".err");
    }

    @Override
    public Response<List<EntryTraceInfoDTO>> getAppAndReqByUrl(EntryTraceQueryParam param) {
        List<Map<String, Object>> appNameList = traceDao.queryForList(buildAppNameSql(param).toString());
        List<EntryTraceInfoDTO> response = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(appNameList)) {
            for (int i = 0; i < appNameList.size(); i++) {
                String appName = StringUtil.parseStr(appNameList.get(i).get("appName"));
                param.setAppName(appName);
                List<Map<String, Object>> resultSets = traceDao.queryForList(buildSql(param).toString());
                EntryTraceInfoDTO trace = new EntryTraceInfoDTO();
                if (CollectionUtils.isNotEmpty(resultSets)) {
                    trace.setAppName(appName);
                    trace.setRequest(StringUtil.parseStr(resultSets.get(0).get("request")));
                    response.add(trace);
                }
            }
        }
        Response<List<EntryTraceInfoDTO>> success = Response.success(response);
        success.setTotal(response.size());
        return success;
    }

    private StringBuilder buildAppNameSql(EntryTraceQueryParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("select appName from t_trace_all where 1=1 and logType in ('1','3') ");
        //设置为当前时间往前10分钟
        if (param.getStartTime() == null) {
            param.setStartTime(System.currentTimeMillis() - (10 * 60 * 1000));
        }
        //设置为当前时间往后5分钟
        if (param.getEndTime() == null) {
            param.setEndTime(System.currentTimeMillis() + (5 * 60 * 1000));
        }
        if (param.getStartTime() != null && param.getStartTime() > 0) {
            sql.append(" and startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            sql.append(" and startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            sql.append(" and parsedServiceName='" + param.getServiceName() + "'");
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            sql.append(" and parsedMethod='" + param.getMethodName() + "'");
        }
        //只取http相关调用
        sql.append(" and rpcType in ('0','1')");
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            sql.append(" and userAppKey='").append(param.getTenantAppKey()).append("' ");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            sql.append(" and envCode='").append(param.getEnvCode()).append("' ");
        }
        sql.append("group by appName");
        return sql;
    }

    private StringBuilder buildSql(EntryTraceQueryParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("select request from t_trace_all where 1=1");
        //设置为当前时间往前10分钟
        if (param.getStartTime() == null) {
            param.setStartTime(System.currentTimeMillis() - (10 * 60 * 1000));
        }
        //设置为当前时间往后5分钟
        if (param.getEndTime() == null) {
            param.setEndTime(System.currentTimeMillis() + (5 * 60 * 1000));
        }
        if (param.getStartTime() != null && param.getStartTime() > 0) {
            sql.append(" and startDate >= '" + DateFormatUtils.format(new Date(param.getStartTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }
        if (param.getEndTime() != null && param.getEndTime() > 0) {
            sql.append(" and startDate <= '" + DateFormatUtils.format(new Date(param.getEndTime()), "yyyy-MM-dd HH:mm:ss") + "' ");
        }

        if (StringUtils.isNotBlank(param.getAppName())) {
            sql.append(" and appName='" + param.getAppName() + "' ");
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            sql.append(" and parsedServiceName='" + param.getServiceName() + "'");
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            sql.append(" and parsedMethod='" + param.getMethodName() + "'");
        }
        //只取http相关调用
        sql.append(" and rpcType in ('0','1')");
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            sql.append(" and userAppKey='").append(param.getTenantAppKey()).append("' ");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            sql.append(" and envCode='").append(param.getEnvCode()).append("' ");
        }
        sql.append(" order by startDate desc limit 1");
        return sql;
    }

    @Override
    public List<RpcBased> getTraceDetail(TraceStackQueryParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("select " + TRACE_SELECT_FILED + " from t_trace_all where 1=1 ");
        if (StringUtil.isNotBlank(param.getStartTime()) && StringUtil.isNotBlank(param.getEndTime())) {
            sql.append(" and startDate between '" + param.getStartTime() + "' and '" + param.getEndTime() + "' ");
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            sql.append(" and userAppKey='").append(param.getTenantAppKey()).append("' ");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            sql.append(" and envCode='").append(param.getEnvCode()).append("' ");
        }
        sql.append(" and traceId='" + param.getTraceId()
                + "' order by rpcId limit " + traceQueryLimit);
        List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);
            /*List<TTrackClickhouseModel> engineModelList = modelList.stream().filter(model -> 5 == model.getLogType())
                    .collect(Collectors.toList());*/
            /*TTrackClickhouseModel engineModel = null;
            if (CollectionUtils.isNotEmpty(engineModelList)) {
                modelList = subtractToList(modelList, engineModelList);
                engineModel = engineModelList.get(0);
            }*/

        // 压测引擎日志
        if (modelList.size() > 1) {
            modelList = modelList.stream().filter(model -> model.getLogType() != 5).collect(Collectors.toList());
        }
        for (TTrackClickhouseModel model : modelList) {
            // 所有客户端都要重新计算耗时
            if (model.getLogType() == 2) {
                calculateCost(model, modelList);
            }
                /*// 流量引擎日志和入口日志匹配，如果匹配上，替换耗时、response、request
                if (engineModel != null && (model.getLogType() == 1 || model.getLogType() == 3) && model
                        .getParsedServiceName().equals(
                                engineModel.getServiceName()) && model.getRpcType() == engineModel.getRpcType()) {
                    model.setStartTime(engineModel.getStartTime());
                    model.setCost(engineModel.getCost());
                    model.setRequest(engineModel.getRequest());
                    model.setResponse(engineModel.getResponse());
                }*/
        }
        List<RpcBased> rpcBasedList = modelList.stream().map(model -> model.getRpcBased()).collect(Collectors.toList());
        return rpcBasedList;
    }

    /**
     * 先查询出所有有问题的节点
     * 切割rpcId获取有问题的节点的父类节点，包括根节点
     * 然后返回以上所有trace
     *
     * @param param
     * @return
     */
    @Override
    public List<RpcBased> getReduceTraceDetail(TraceStackQueryParam param) {
        String rpcSql = spliceReduceRpcSql(param);
        List<TTrackClickhouseModel> modelRpcIdList = traceDao.queryForList(rpcSql, TTrackClickhouseModel.class);
        //如果判断条件不满足，就展示所有
        if (CollectionUtils.isEmpty(modelRpcIdList)) {
            return getTraceDetail(param);
        }

        List<String> rpcList = modelRpcIdList.stream().filter(a -> Objects.nonNull(a) && StringUtils.isNotBlank(a.getRpcId()))
                .map(TTrackClickhouseModel::getRpcId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(rpcList)){
            return getTraceDetail(param);
        }

        Set<String> allRpcSet = new HashSet<>();
        for (String rpcId : rpcList) {
            allRpcSet.addAll(getAllRootRpcIdSet(rpcId));
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select " + TRACE_SELECT_FILED + " from t_trace_all where 1=1 ");
        if (StringUtil.isNotBlank(param.getStartTime()) && StringUtil.isNotBlank(param.getEndTime())) {
            sql.append(" and startDate between '" + param.getStartTime() + "' and '" + param.getEndTime() + "' ");
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            sql.append(" and userAppKey='").append(param.getTenantAppKey()).append("' ");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            sql.append(" and envCode='").append(param.getEnvCode()).append("' ");
        }
        if (CollectionUtils.isNotEmpty(allRpcSet)) {
            sql.append(" and rpcId in (");
            allRpcSet.forEach(rpcId -> sql.append("'").append(rpcId).append("',"));
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        }
        sql.append(" and traceId='" + param.getTraceId()
                + "' order by rpcId asc,logType asc limit " + traceQueryLimit);

        List<TTrackClickhouseModel> modelList = traceDao.queryForList(sql.toString(), TTrackClickhouseModel.class);

        // 压测引擎日志
        if (modelList.size() > 1) {
            modelList = modelList.stream().filter(model -> model.getLogType() != 5).collect(Collectors.toList());
        }
        for (TTrackClickhouseModel model : modelList) {
            // 所有客户端都要重新计算耗时
            if (model.getLogType() == 2) {
                calculateCost(model, modelList);
            }
        }
        List<RpcBased> rpcBasedList = modelList.stream().map(model -> model.getRpcBased()).collect(Collectors.toList());
        return rpcBasedList;
    }

    private static String spliceReduceRpcSql(TraceStackQueryParam param) {
        StringBuilder sql = new StringBuilder();
        sql.append("select rpcId" + " from t_trace_all where 1=1 ");
        if (StringUtil.isNotBlank(param.getStartTime()) && StringUtil.isNotBlank(param.getEndTime())) {
            sql.append(" and startDate between '" + param.getStartTime() + "' and '" + param.getEndTime() + "' ");
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            sql.append(" and userAppKey='").append(param.getTenantAppKey()).append("' ");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            sql.append(" and envCode='").append(param.getEnvCode()).append("' ");
        }
        if (Objects.nonNull(param.getCost())) {
            sql.append(" and (cost >= ").append(param.getCost()).append(" or (resultCode!='200' and resultCode !='00'))");
        }
        sql.append(" and traceId='" + param.getTraceId() + "' order by rpcId");

        return sql.toString();
    }

    /**
     * 根据rpcId获取所有父类的节点
     * @param rpcId
     * @return
     */
    private static Set<String> getAllRootRpcIdSet(String rpcId) {
        if (StringUtils.isBlank(rpcId)) {
            return Collections.emptySet();
        }
        List<String> rpcIdParts = Arrays.asList(rpcId.split("\\."));
        Set<String> rpcIdSet = new HashSet<>();
        for (int i = rpcIdParts.size(); i > 0; i--) {
            String currentRpcId = String.join(".", rpcIdParts.subList(0, i));
            rpcIdSet.add(currentRpcId);
        }
        return rpcIdSet;
    }

    private void calculateCost(TTrackClickhouseModel clientModel, List<TTrackClickhouseModel> modelList) {
        TTrackClickhouseModel serverModel = modelList.stream().filter(
                        m -> m.getLogType() == 3 && m.getRpcId().startsWith(clientModel.getRpcId()) && m.getServiceName()
                                .equals(clientModel.getServiceName()) && m.getMethodName().equals(clientModel.getMethodName()))
                .findFirst().orElse(null);
        if (serverModel != null) {
            // 非MQ的中间件，如果客户端耗时小于服务端耗时，则客户端耗时=客户端耗时+服务端耗时
            if (clientModel.getRpcType() != MiddlewareType.TYPE_MQ) {
                if (clientModel.getCost() < serverModel.getCost()) {
                    clientModel.setCost(clientModel.getCost() + serverModel.getCost());
                }
            }
        }
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
     *
     * <pre>
     *     subtractToList([1,2,3,4],[2,3,4,5]) -》 [1]
     * </pre>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   元素类型
     * @return 单差集
     * @since 5.3.5
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2) {

        if (isEmpty(coll1)) {
            return Collections.emptyList();
        }
        if (isEmpty(coll2)) {
            return new LinkedList<>(coll1);
        }

        //将被交数用链表储存，防止因为频繁扩容影响性能
        final List<T> result = new LinkedList<>();
        Set<T> set = new HashSet<>(coll2);
        for (T t : coll1) {
            if (false == set.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }
}
