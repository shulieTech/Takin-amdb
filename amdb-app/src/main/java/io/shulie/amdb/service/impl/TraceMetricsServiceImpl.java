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
import io.shulie.amdb.constant.E2eConstants;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.E2EBaseRequest;
import io.shulie.amdb.request.query.E2ENodeMetricsRequest;
import io.shulie.amdb.request.query.MetricsQueryRequest;
import io.shulie.amdb.request.query.TraceMetricsRequest;
import io.shulie.amdb.response.e2e.E2ENodeErrorInfosResponse;
import io.shulie.amdb.response.e2e.E2ENodeMetricsResponse;
import io.shulie.amdb.response.e2e.E2EStatisticsResponse;
import io.shulie.amdb.response.metrics.MetricsResponse;
import io.shulie.amdb.service.MetricsService;
import io.shulie.amdb.service.TraceMetricsService;
import io.shulie.amdb.utils.InfluxDBManager;
import io.shulie.amdb.utils.StringUtil;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * @author sunsy
 */
public class TraceMetricsServiceImpl implements TraceMetricsService {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private InfluxDBManager influxDbManager;

    /**
     * 默认延迟2分钟
     */
    @Value("${config.e2e.delaySec}")
    private int delayTime;

    @Override
    public List<TraceMetrics> getSqlStatements(TraceMetricsRequest param) {
        //获取查询时间区间 默认延迟两分钟
        long startTime = timeDelay(param.getStartTime());
        long endTime = timeDelay(param.getEndTime());

        String edgeIds[] = param.getEdgeIds().split(",");
        int clusterTest = param.getClusterTest();
        StringBuffer buffer = new StringBuffer();
        buffer.append("select edgeId,sqlStatement,maxRt,avgRt,totalCount,traceId,appName,method,rpcType,service from trace_metrics where" +
                " time >= " + startTime + "000000 and time < " + endTime + "000000 and sqlStatement != 'null' and (");
        boolean isFirst = true;
        for (String edgeId : edgeIds) {
            if (StringUtils.isNoneBlank(edgeId)) {
                if (isFirst) {
                    buffer.append(" edgeId='" + edgeId + "' ");
                    isFirst = false;
                } else {
                    buffer.append(" or edgeId='" + edgeId + "' ");
                }
            }
        }
        buffer.append(")");
        switch (clusterTest) {
            case 0:
                buffer.append(" and clusterTest = 'false'");
                break;
            case 1:
                buffer.append(" and clusterTest = 'true'");
                break;
            default:
                break;
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            buffer.append(" and tenantAppKey='").append(param.getTenantAppKey()).append("'");
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            buffer.append(" and envCode='").append(param.getEnvCode()).append("'");
        }
        buffer.append(" TZ('Asia/Shanghai')");
        log.info("查询sql:{}", buffer);
        List<TraceMetrics> resultList = new ArrayList<>();
        List<QueryResult.Result> aggrerateResult = influxDbManager.query(buffer.toString());
        List<QueryResult.Series> list = aggrerateResult.get(0).getSeries();
        if (list != null) {
            for (QueryResult.Series result : list) {
                List columns = result.getColumns();
                List values = result.getValues();
                resultList = getQueryData(columns, values);
            }
        }
        return resultList;
    }

    @Override
    public Response<List<E2EStatisticsResponse>> getStatistics(TraceMetricsRequest param) {
        //获取查询时间区间 默认延迟两分钟
        long startTime = timeDelay(param.getStartTime());
        long endTime = timeDelay(param.getEndTime());
        //获取查询来源,分为tro/e2e
        String querySource = StringUtil.parseStr(param.getQuerySource());
        List<E2ENodeMetricsRequest> requestList = param.getE2eNodeRequestList();

        if (requestList.isEmpty()) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "e2eNodeRquestList查询条件为空");
        }
        String groupFileds = "clusterTest";
        String[] groupArr = groupFileds.split(",", -1);
        // 根据流量来源分组统计巡检和业务流量
        Map<String, MetricsResponse> result = getMetricsResponses(requestList,
                E2eConstants.MEARSUREMENT_TRACE_METRICS, groupFileds,
                startTime, endTime, -1, E2eConstants.INTERFACE_STATISTICS);

        //去重结果,判断一共需要返回几个Response
        Set<String> keySet = new HashSet<>();
        result.forEach((key, value) -> {
            String[] keys = key.split("\\|");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < keys.length - groupArr.length; i++) {
                stringBuilder.append(keys[i]).append("|");
            }
            keySet.add(stringBuilder.toString());
        });

        List<E2EStatisticsResponse> responseList = new ArrayList<>();
        //查询到结果的requestKey
        Set<String> resultKeySet = new HashSet<>();
        for (String key : keySet) {
            E2EStatisticsResponse response = new E2EStatisticsResponse();
            // 生成结果
            for (Map.Entry entry : result.entrySet()) {
                //匹配上key
                if (StringUtil.parseStr(entry.getKey()).startsWith(key)) {
                    MetricsResponse metricsResponse = (MetricsResponse) entry.getValue();
                    Map<String, Object> metricsValue = metricsResponse.getValue().get(0);
                    String edgeId = StringUtil.parseStr(metricsResponse.getTags().get("edgeId"));
                    String appName = StringUtil.parseStr(metricsResponse.getTags().get("appName"));
                    String method = StringUtil.parseStr(metricsResponse.getTags().get("method"));
                    String service = StringUtil.parseStr(metricsResponse.getTags().get("service"));
                    String rpcType = StringUtil.parseStr(metricsResponse.getTags().get("rpcType"));

                    response.setAppName(appName);
                    response.setMethodName(method);
                    response.setServiceName(service);
                    response.setRpcType(rpcType);
                    response.setEdgeId(edgeId);
                    //压测流量
                    if ("true".equals(metricsResponse.getTags().get("clusterTest"))) {
                        response.setE2eRequestCount((Double) metricsValue.get("totalCount"));
                    } else if ("false".equals(metricsResponse.getTags().get("clusterTest"))) {
                        response.setBusinessRequestCount((Double) metricsValue.get("totalCount"));
                    }
                    resultKeySet.add(generateE2eKey(edgeId, appName, method, service, rpcType));
                }

            }
            responseList.add(response);
        }

        //如果是e2e,还需要补全数据
        if ("e2e".equals(querySource)) {
            requestList.forEach(request -> {
                String edgeId = StringUtil.parseStr(request.getEdgeId());
                String appName = StringUtil.parseStr(request.getAppName());
                String methodName = StringUtil.parseStr(request.getMethodName());
                String serviceName = StringUtil.parseStr(request.getServiceName());
                String rpcType = StringUtil.parseStr(request.getRpcType());
                if (!resultKeySet.contains(generateE2eKey(edgeId, appName, methodName, serviceName, rpcType))) {
                    E2EStatisticsResponse response = new E2EStatisticsResponse();
                    response.setEdgeId(edgeId);
                    response.setAppName(appName);
                    response.setMethodName(methodName);
                    response.setServiceName(serviceName);
                    response.setRpcType(rpcType);
                    response.setE2eRequestCount(0);
                    response.setBusinessRequestCount(0);
                    responseList.add(response);
                }
            });
        }

        return Response.success(responseList);
    }

    @Override
    public Response<List<E2ENodeMetricsResponse>> getNodeMetrics(TraceMetricsRequest param) {
        //获取查询时间区间 默认延迟两分钟
        long startTime = timeDelay(param.getStartTime());
        long endTime = timeDelay(param.getEndTime());
        //计算总秒数
        long range = (endTime - startTime) / 1000;
        //获取查询来源,分为tro/e2e
        String querySource = StringUtil.parseStr(param.getQuerySource());
        //获取流量来源
        int clusterTest = param.getClusterTest();
        List<E2ENodeMetricsRequest> requestList = param.getE2eNodeRequestList();

        if (requestList.isEmpty()) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "e2eNodeRquestList查询条件为空");
        }

        // 根据流量来源统计指标数据
        Map<String, MetricsResponse> result = getMetricsResponses(requestList,
                E2eConstants.MEARSUREMENT_TRACE_METRICS, null,
                startTime, endTime, clusterTest, E2eConstants.INTERFACE_NODEMETRICS);
        // 生成结果
        List<E2ENodeMetricsResponse> responseList = new ArrayList<>();

        //查询到结果的requestKey
        Set<String> resultKeySet = new HashSet<>();
        for (Map.Entry entry : result.entrySet()) {
            MetricsResponse metricsResponse = (MetricsResponse) entry.getValue();
            Map<String, Object> metricsResponseMap = metricsResponse.getValue().get(0);

            Double totalCount = (Double) metricsResponseMap.get("totalCount");
            //e2e的成功数包含断言判定
            String key = "e2e".equals(querySource) ? "e2eSuccessCount" : "successCount";
            Double totalSuccessCount = (Double) metricsResponseMap.get(key);
            if (totalSuccessCount == null) {
                totalSuccessCount = 0.0;
            }
            Double totalRt = (Double) metricsResponseMap.get("totalRt");

            E2ENodeMetricsResponse response = new E2ENodeMetricsResponse();
            String edgeId = StringUtil.parseStr(metricsResponse.getTags().get("edgeId"));
            String appName = StringUtil.parseStr(metricsResponse.getTags().get("appName"));
            String method = StringUtil.parseStr(metricsResponse.getTags().get("method"));
            String service = StringUtil.parseStr(metricsResponse.getTags().get("service"));
            String rpcType = StringUtil.parseStr(metricsResponse.getTags().get("rpcType"));
            response.setEdgeId(edgeId);
            response.setAppName(appName);
            response.setMethodName(method);
            response.setServiceName(service);
            response.setRpcType(rpcType);
            response.setSqlStatement((String) metricsResponseMap.get("sqlStatement"));
            Double avgRt = totalCount == 0 ? 0 : totalRt / totalCount;
            response.setRt(avgRt);

            //如果查询老的数据,maxRt为null,此时放入平均rt
            Double maxRt = (Double) metricsResponseMap.get("maxRt");
            response.setMaxRt(maxRt == null ? avgRt : maxRt);
            response.setQps(range == 0 ? 0 : totalCount / range);
            response.setSuccessCount(totalSuccessCount);
            response.setTotalCount(totalCount);
            response.setSuccessRate(
                    totalCount == 0 ? 0 : totalSuccessCount / totalCount);
            response.setTraceId((String) metricsResponseMap.get("traceId"));
            responseList.add(response);
            resultKeySet.add(generateE2eKey(edgeId, appName, method, service, rpcType));
        }

        //如果是e2e,还需要补全数据
        if ("e2e".equals(querySource)) {
            requestList.forEach(request -> {
                String edgeId = StringUtil.parseStr(request.getEdgeId());
                String appName = StringUtil.parseStr(request.getAppName());
                String methodName = StringUtil.parseStr(request.getMethodName());
                String serviceName = StringUtil.parseStr(request.getServiceName());
                String rpcType = StringUtil.parseStr(request.getRpcType());
                if (!resultKeySet.contains(generateE2eKey(edgeId, appName, methodName, serviceName, rpcType))) {
                    E2ENodeMetricsResponse response = new E2ENodeMetricsResponse();
                    response.setEdgeId(edgeId);
                    response.setAppName(appName);
                    response.setMethodName(methodName);
                    response.setServiceName(serviceName);
                    response.setRpcType(rpcType);
                    response.setSqlStatement("null");
                    response.setRt(0);
                    response.setMaxRt(0);
                    response.setQps(0);
                    response.setSuccessRate(0);
                    response.setTotalCount(0);
                    response.setSuccessCount(0);
                    response.setTraceId("");
                    responseList.add(response);
                }
            });
        }
        return Response.success(responseList);
    }


    @Override
    public Response<List<E2ENodeErrorInfosResponse>> getNodeErrorInfos(TraceMetricsRequest param) {
        //获取查询时间区间 默认延迟两分钟
        long startTime = timeDelay(param.getStartTime());
        long endTime = timeDelay(param.getEndTime());
        //获取查询来源,分为tro/e2e
        String querySource = StringUtil.parseStr(param.getQuerySource());
        List<E2ENodeMetricsRequest> requestList = param.getE2eNodeRequestList();

        if (requestList.isEmpty()) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "e2eNodeRquestList查询条件为空");
        }

        // 查询业务流量和巡检流量
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> traceMetrics = getErrorMetricsResponses(requestList,
                E2eConstants.MEARSUREMENT_TRACE_METRICS, null,
                startTime, endTime, querySource);
        // 巡检流量
        Map<String, MetricsResponse> clusterTraceMetrics = traceMetrics.getFirst();
        // 业务流量
        Map<String, MetricsResponse> businessTraceMetrics = traceMetrics.getSecond();

        // 查询业务流量和巡检流量对应的断言失败指标（断言）
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> assertMetrics = getErrorMetricsResponses(requestList,
                E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS, "exceptionType",
                startTime, endTime, querySource);
        // 巡检流量(断言)
        Map<String, MetricsResponse> clusterAssertMetrics = assertMetrics.getFirst();
        // 业务流量(断言)
        Map<String, MetricsResponse> businessAssertMetrics = assertMetrics.getSecond();

        Set<String> keySet = new HashSet<>();
        keySet.addAll(clusterTraceMetrics.keySet());
        keySet.addAll(businessTraceMetrics.keySet());
        keySet.addAll(clusterAssertMetrics.keySet());
        keySet.addAll(businessAssertMetrics.keySet());

        // 合并计算结果
        Map<String, E2ENodeErrorInfosResponse> responseMap = new HashMap<>();
        for (String key : keySet) {
            String[] params = key.split("\\|");
            int length = params.length;

            String nodeId = null;
            Boolean hasEdgeId = true;
            if ((params[length - 1].equals(E2eConstants.MEARSUREMENT_TRACE_METRICS) && params.length == 8) || (params[length - 1].equals(E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS) && params.length == 9)) {
                hasEdgeId = false;
                nodeId = getOldNodeId(params[0], params[1], params[2], params[3]);
                //使用edgeId查询,查询新数据
            } else if ((params[length - 1].equals(E2eConstants.MEARSUREMENT_TRACE_METRICS) && params.length == 9) || (params[length - 1].equals(E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS) && params.length == 10)) {
                nodeId = params[4];
            }

            // 这里重新封装响应对象，因为key里包括【异常类型】字段，去掉之后对相同nodeId的数据进行合并
            E2ENodeErrorInfosResponse response = responseMap.get(nodeId);
            if (response == null) {
                response = new E2ENodeErrorInfosResponse();
                response.setAppName(params[0]);
                response.setServiceName(params[1]);
                response.setMethodName(params[2]);
                response.setRpcType(params[3]);
                if (hasEdgeId) {
                    response.setEdgeId(nodeId);
                } else {
                    response.setEdgeId("");
                }
                responseMap.put(nodeId, response);
            }

            // 聚合指标
            MetricsModel traceMetricsModel = MetricsModel.getMetricsModel(clusterTraceMetrics.get(key),
                    businessTraceMetrics.get(key));
            response.setE2eRequestCount(response.getE2eRequestCount() + traceMetricsModel.getE2eTotalCount());
            response.setBusinessRequestCount(response.getBusinessRequestCount() + traceMetricsModel.getBusinessTotalCount());
            response.setRequestCount(response.getRequestCount() + traceMetricsModel.getTotalCount());

            if (params[length - 1].equals(E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS)) {
                // 聚合指标（断言）
                MetricsModel assertMetricsModel = MetricsModel.getMetricsModel(clusterAssertMetrics.get(key),
                        businessAssertMetrics.get(key));
                String exceptionType = params[length - 2];

                E2ENodeErrorInfosResponse.ErrorInfo errorInfo = null;
                if (CollectionUtils.isEmpty(response.getErrorInfoList())) {
                    errorInfo = new E2ENodeErrorInfosResponse.ErrorInfo();
                    errorInfo.setE2eErrorCount(assertMetricsModel.getE2eErrorCount());
                    errorInfo.setBusinessErrorCount(assertMetricsModel.getBusinessErrorCount());
                    errorInfo.setErrorCount(assertMetricsModel.getTotalErrorCount());
                    errorInfo.setErrorType(exceptionType);
                } else {
                    AtomicReference<Boolean> isMerged = new AtomicReference<>(false);
                    //如果不为空,则遍历已有的errorInfo,并合并相同错误类型的
                    response.getErrorInfoList().forEach(v -> {
                        if (exceptionType.equals(v.getErrorType())) {
                            v.setE2eErrorCount(v.getE2eErrorCount() + assertMetricsModel.getE2eErrorCount());
                            v.setBusinessErrorCount(v.getBusinessErrorCount() + assertMetricsModel.getBusinessErrorCount());
                            v.setErrorCount(v.getErrorCount() + assertMetricsModel.getTotalErrorCount());
                            isMerged.set(true);
                        }
                    });
                    if (!isMerged.get()) {
                        errorInfo = new E2ENodeErrorInfosResponse.ErrorInfo();
                        errorInfo.setE2eErrorCount(assertMetricsModel.getE2eErrorCount());
                        errorInfo.setBusinessErrorCount(assertMetricsModel.getBusinessErrorCount());
                        errorInfo.setErrorCount(assertMetricsModel.getTotalErrorCount());
                        errorInfo.setErrorType(exceptionType);
                    }
                }

                //根据异常类型查最近一次的traceId
                List<QueryResult.Result> tmpResult = null;
                if (Objects.nonNull(errorInfo)) {
                    if (hasEdgeId) {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (nodeId.contains(",")) {
                            stringBuilder.append("(");
                            for (String single : nodeId.split(",")) {
                                stringBuilder.append("nodeId='" + single + "'").append(" or ");
                            }
                            stringBuilder.delete(stringBuilder.lastIndexOf(" or "), stringBuilder.length());
                            stringBuilder.append(")");
                        } else {
                            stringBuilder.append("nodeId = '" + nodeId + "'");
                        }

                        tmpResult = influxDbManager.query("select traceId from " + E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS + " where time >= " + startTime + "000000 and time < " + endTime + "000000 and exceptionType='" + exceptionType + "' and " + stringBuilder + " order by time desc limit 1");
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("parsedAppName = '" + params[0] + "'");
                        stringBuilder.append(" and parsedServiceName = '" + params[1] + "'");
                        stringBuilder.append(" and parsedMethod = '" + params[2] + "'");
                        stringBuilder.append(" and rpcType = '" + params[3] + "' ");
                        tmpResult = influxDbManager.query("select traceId from " + E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS + " where time >= " + startTime + "000000 and time < " + endTime + "000000 and exceptionType='" + exceptionType + "' and " + stringBuilder + " order by time desc limit 1");
                    }

                    if (CollectionUtils.isNotEmpty(tmpResult)) {
                        List<QueryResult.Series> tmpList = tmpResult.get(0).getSeries();
                        if (CollectionUtils.isNotEmpty(tmpList)) {
                            errorInfo.setTraceId(StringUtil.parseStr(tmpList.get(0).getValues().get(0).get(1)));
                        }
                    } else {
                        errorInfo.setTraceId("");
                    }
                    response.getErrorInfoList().add(errorInfo);
                }
            }
        }
        return Response.success(responseMap.values().stream().collect(Collectors.toList()));
    }


    /**
     * 生成nodeId
     *
     * @param request
     * @return
     */
    private String getNodeId(E2EBaseRequest request) {
        String edgeId = request.getEdgeId();
        if (StringUtils.isBlank(edgeId)) {
            return getOldNodeId(request.getAppName(), request.getServiceName(), request.getMethodName(), request.getRpcType());
        } else {
            return edgeId;
        }
    }

    private String getOldNodeId(String parsedAppName, String parsedServiceName, String parsedMethod, String rpcType) {
        return Md5Utils.md5(parsedAppName + "|" + parsedServiceName + "|" + parsedMethod + "|" + rpcType);
    }

    private String generateE2eKey(String edgeId, String appName, String method, String service, String rpcType) {
        return appName + "|" + method + "|" + service + "|" + rpcType + "|" + edgeId;
    }

    private Map<String, MetricsResponse> getMetricsResponses(List<E2ENodeMetricsRequest> requestList, String measurement, String group, long startTime, long endTime, int clusterTest, String interfaceName) {
        //构造查询语句
        MetricsQueryRequest queryRequest = buildQueryRequest(requestList, measurement, group, startTime,
                endTime, clusterTest, interfaceName);
        return metricsService.getMetrics(queryRequest);
    }

    private Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> getErrorMetricsResponses(
            List<E2ENodeMetricsRequest> requestList, String measurement,
            String group, long startTime, long endTime, String querySource) {
        // 巡检指标查询
        MetricsQueryRequest e2eQueryRequest = buildErrorInfoQueryRequest(requestList, measurement, group, startTime,
                endTime, 1, querySource);
        Map<String, MetricsResponse> e2eMetricsResponseMap = metricsService.getMetrics(e2eQueryRequest);
        e2eMetricsResponseMap = formatKey(e2eMetricsResponseMap, key -> {
            key = key + "|" + measurement;
            return key;
        });

        // 业务指标查询
        MetricsQueryRequest businessQueryRequest = buildErrorInfoQueryRequest(requestList, measurement, group, startTime,
                endTime, 0, querySource);
        Map<String, MetricsResponse> businessMetricsResponseMap = metricsService.getMetrics(businessQueryRequest);
        businessMetricsResponseMap = formatKey(businessMetricsResponseMap, key -> {
            key = key + "|" + measurement;
            return key;
        });
        return new Pair<>(e2eMetricsResponseMap, businessMetricsResponseMap);
    }

    private MetricsQueryRequest buildErrorInfoQueryRequest(List<E2ENodeMetricsRequest> requestList, String measurement, String group, long startTime, long endTime, int clusterTest, String querySource) {
        MetricsQueryRequest queryRequest = new MetricsQueryRequest();
        queryRequest.setMeasurementName(measurement);

        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("totalRt", "sum(totalRt)");
        fieldsMap.put("totalCount", "sum(totalCount)");
        if (E2eConstants.MEARSUREMENT_TRACE_METRICS.equals(measurement)) {
            //e2e认为断言命中的200响应也算作失败,要根据流量来源区分
            if ("tro".equals(querySource)) {
                fieldsMap.put("successCount", "sum(successCount)");
                fieldsMap.put("errorCount", "sum(errorCount)");
            } else if ("e2e".equals(querySource)) {
                fieldsMap.put("successCount", "sum(e2eSuccessCount)");
                fieldsMap.put("errorCount", "sum(e2eErrorCount)");
            }
        } else if (E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS.equals(measurement)) {
            fieldsMap.put("successCount", "sum(successCount)");
            fieldsMap.put("errorCount", "sum(errorCount)");
        }

        queryRequest.setFieldMap(fieldsMap);
        List<LinkedHashMap<String, String>> tagMapList = new ArrayList<>();
        for (E2EBaseRequest request : requestList) {
            LinkedHashMap<String, String> tagMap = new LinkedHashMap<>();
            //查询执行流量来源的数据
            if (E2eConstants.MEARSUREMENT_TRACE_METRICS.equals(measurement)) {
                //指标存在顺序,注意不要轻易调整
                tagMap.put("appName", request.getAppName());
                tagMap.put("service", request.getServiceName());
                tagMap.put("method", request.getMethodName());
                tagMap.put("rpcType", request.getRpcType());
                //兼容历史数据查询
                if (StringUtils.isNotBlank(request.getEdgeId())) {
                    tagMap.put("edgeId", request.getEdgeId());
                }
                if (clusterTest == 0) {
                    tagMap.put("clusterTest", "false");
                } else if (clusterTest == 1) {
                    tagMap.put("clusterTest", "true");
                }
                if (StringUtils.isNotBlank(request.getTenantAppKey())) {
                    tagMap.put("tenantAppKey", request.getTenantAppKey());
                }
                if (StringUtils.isNotBlank(request.getEnvCode())) {
                    tagMap.put("envCode", request.getEnvCode());
                }
            } else if (E2eConstants.MEARSUREMENT_TRACE_E2E_ASSERT_METRICS.equals(measurement)) {
                tagMap.put("parsedAppName", request.getAppName());
                tagMap.put("parsedServiceName", request.getServiceName());
                tagMap.put("parsedMethod", request.getMethodName());
                tagMap.put("rpcType", request.getRpcType());
                //兼容历史数据查询
                if (StringUtils.isNotBlank(request.getEdgeId())) {
                    tagMap.put("nodeId", request.getEdgeId());
                }
                //e2e的断言指标表里clusterTest使用1/0区分的
                tagMap.put("clusterTest", clusterTest + "");
                if (StringUtils.isNotBlank(request.getTenantAppKey())) {
                    tagMap.put("tenantAppKey", request.getTenantAppKey());
                }
                if (StringUtils.isNotBlank(request.getEnvCode())) {
                    tagMap.put("envCode", request.getEnvCode());
                }
            }
            tagMapList.add(tagMap);
        }
        queryRequest.setTagMapList(tagMapList);
        if (StringUtils.isNotBlank(group)) {
            queryRequest.setGroups(group);
        }
        queryRequest.setStartTime(startTime);
        queryRequest.setEndTime(endTime);
        return queryRequest;
    }

    private <T> Map<String, T> formatKey(Map<String, T> oldData, Function<String, String> function) {
        Map<String, T> newData = new HashMap<>();
        for (String key : oldData.keySet()) {
            String newKey = function.apply(key);
            newData.put(newKey, oldData.get(key));
        }
        return newData;
    }

    private MetricsQueryRequest buildQueryRequest(List<E2ENodeMetricsRequest> requestList, String measurement, String group, long startTime, long endTime, int clusterTest, String interfaceName) {
        MetricsQueryRequest queryRequest = new MetricsQueryRequest();
        queryRequest.setMeasurementName(measurement);

        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("totalCount", "sum(totalCount)");
        fieldsMap.put("errorCount", "sum(errorCount)");
        fieldsMap.put("successCount", "sum(successCount)");
        fieldsMap.put("e2eSuccessCount", "sum(e2eSuccessCount)");
        fieldsMap.put("totalRt", "sum(totalRt)");
        queryRequest.setFieldMap(fieldsMap);

        //只有取节点指标时候才需要取非聚合属性
        if (E2eConstants.INTERFACE_NODEMETRICS.equals(interfaceName)) {
            Map<String, String> nonAggrerateFieldsMap = new HashMap<>();
            nonAggrerateFieldsMap.put("sqlStatement", "sqlStatement");
            nonAggrerateFieldsMap.put("traceId", "traceId");
            nonAggrerateFieldsMap.put("edgeId", "edgeId");
            nonAggrerateFieldsMap.put("appName", "appName");
            nonAggrerateFieldsMap.put("service", "service");
            nonAggrerateFieldsMap.put("method", "method");
            nonAggrerateFieldsMap.put("rpcType", "rpcType");
            nonAggrerateFieldsMap.put("maxRt", "max(maxRt)");
//            nonAggrerateFieldsMap.put("maxRt", "max(totalRt)");
            queryRequest.setNonAggrerateFieldMap(nonAggrerateFieldsMap);
        }
        List<LinkedHashMap<String, String>> tagMapList = new ArrayList<>();
        for (E2ENodeMetricsRequest request : requestList) {
            LinkedHashMap<String, String> tagMap = new LinkedHashMap<>();
            //查询执行流量来源的数据
            if (clusterTest == 0) {
                tagMap.put("clusterTest", "false");
            } else if (clusterTest == 1) {
                tagMap.put("clusterTest", "true");
            }
            //向下兼容老数据查询
            if (StringUtils.isNotBlank(request.getEdgeId())) {
                tagMap.put("edgeId", request.getEdgeId());
//                tagMapList.add(tagMap);
//                continue;
            }
            tagMap.put("appName", request.getAppName());
            tagMap.put("service", request.getServiceName());
            tagMap.put("method", request.getMethodName());
            tagMap.put("rpcType", request.getRpcType());
            if (StringUtils.isNotBlank(request.getTenantAppKey())) {
                tagMap.put("tenantAppKey", request.getTenantAppKey());
            }
            if (StringUtils.isNotBlank(request.getEnvCode())) {
                tagMap.put("envCode", request.getEnvCode());
            }
            tagMapList.add(tagMap);
        }
        queryRequest.setTagMapList(tagMapList);

        if (StringUtils.isNotBlank(group)) {
            queryRequest.setGroups(group);
        }
        queryRequest.setStartTime(startTime);
        queryRequest.setEndTime(endTime);
        return queryRequest;
    }

    private long timeDelay(long oldTime) {
        long time = oldTime - delayTime * 1000;
        return time < 0 ? 0 : time;
    }

    /***整理列名、行数据***/
    private List<TraceMetrics> getQueryData(List<String> columns, List<List<Object>> values) {
        List<TraceMetrics> lists = new ArrayList<>();
        for (List<Object> list : values) {
            TraceMetrics info = new TraceMetrics();
            BeanWrapperImpl bean = new BeanWrapperImpl(info);
            for (int i = 0; i < list.size(); i++) {
                String propertyName = columns.get(i);//字段名
                Object value = list.get(i);//相应字段值
                bean.setPropertyValue(propertyName, value);
            }
            lists.add(info);
        }
        return lists;
    }

}
