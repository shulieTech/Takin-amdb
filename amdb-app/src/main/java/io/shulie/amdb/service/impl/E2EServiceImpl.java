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
import io.shulie.amdb.entity.TAMDBPradarE2EAssertConfigDO;
import io.shulie.amdb.entity.TAMDBPradarE2EConfigDO;
import io.shulie.amdb.mapper.PradarE2EAssertMapper;
import io.shulie.amdb.mapper.PradarE2EConfigMapper;
import io.shulie.amdb.request.query.*;
import io.shulie.amdb.response.e2e.E2EBaseResponse;
import io.shulie.amdb.response.e2e.E2ENodeErrorInfosResponse;
import io.shulie.amdb.response.e2e.E2ENodeErrorInfosResponse.ErrorInfo;
import io.shulie.amdb.response.e2e.E2ENodeMetricsResponse;
import io.shulie.amdb.response.e2e.E2EStatisticsResponse;
import io.shulie.amdb.response.metrics.MetricsResponse;
import io.shulie.amdb.service.E2EService;
import io.shulie.amdb.service.MetricsService;
import io.shulie.surge.data.common.utils.Pair;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class E2EServiceImpl implements E2EService {

    @Autowired
    private MetricsService metricsService;

    @Resource
    private PradarE2EConfigMapper pradarE2eConfigMapper;

    @Resource
    private PradarE2EAssertMapper pradarE2eAssertMapper;

    /**
     * 默认延迟2分钟
     */
    @Value("${config.e2e.delaySec}")
    private int delayTime;

    @Override
    public Response<List<E2ENodeMetricsResponse>> getNodeMetrics(List<E2ENodeMetricsRequest> param) {
        long startTime = param.get(0).getStartTime();
        long endTime = param.get(0).getEndTime();
        startTime = timeDelay(startTime);
        endTime = timeDelay(endTime);
        // 查询业务流量和巡检流量
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> pair = getMetricsResponses(param,
                "trace_e2e_metrics", null,
                startTime, endTime);
        // 巡检流量
        Map<String, MetricsResponse> responseListOfE2E = pair.getFirst();
        // 业务流量
        Map<String, MetricsResponse> responseListOfBusiness = pair.getSecond();
        // 合并
        // Set<String> keySet = new HashSet<>(
        //    CollectionUtil.union(responseListOfE2E.keySet(), responseListOfBusiness.keySet()));
        Set<String> keySet = param.stream().map(
                request -> request.getAppName() + "|" + request.getServiceName() + "|" + request.getMethodName() + "|"
                        + request.getRpcType()).collect(Collectors.toSet());
        // 构造返回对象
        Map<String, E2EBaseResponse> responseMap = getResponseListByMetricsResultKetSet(keySet,
                E2ENodeMetricsResponse.class);
        // 合并计算结果
        List<E2ENodeMetricsResponse> responseList = new ArrayList<>();
        for (String key : keySet) {
            E2ENodeMetricsResponse response = (E2ENodeMetricsResponse) responseMap.get(key);
            MetricsModel metricsModel = MetricsModel.getMetricsModel(responseListOfE2E.get(key),
                    responseListOfBusiness.get(key));
            long range = (endTime - startTime) / 1000;
            response.setQps(range == 0 ? 0 : metricsModel.totalCount / range);
            response.setRt(metricsModel.totalCount == 0 ? 0 : metricsModel.totalRt / metricsModel.totalCount);
            response.setSuccessRate(
                    metricsModel.totalCount == 0 ? 0 : metricsModel.totalSuccessCount / metricsModel.totalCount);
            responseList.add(response);

//            if (response.getRt() == 0 && response.getSuccessRate() == 0) {
//                log.info("rt和成功率为0,请求参数为:{}", param);
//            }
        }
        return Response.success(responseList);
    }

    @Override
    public Response<List<E2ENodeErrorInfosResponse>> getNodeErrorInfos(List<E2ENodeErrorInfosRequest> param) {
        long startTime = param.get(0).getStartTime();
        long endTime = param.get(0).getEndTime();
        startTime = timeDelay(startTime);
        endTime = timeDelay(endTime);
        // 查询业务流量和巡检流量
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> statisticsPair = getMetricsResponses(param,
                "trace_e2e_metrics", null,
                startTime, endTime);
        // 巡检流量
        Map<String, MetricsResponse> responseStatisticsListOfE2E = statisticsPair.getFirst();
        // 业务流量
        Map<String, MetricsResponse> responseStatisticsListOfBusiness = statisticsPair.getSecond();
        // 查询业务流量和巡检流量对应的断言失败指标（断言）
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> pair = getMetricsResponses(param,
                "trace_e2e_assert_metrics", "exceptionType",
                startTime, endTime);
        // 巡检流量(断言)
        Map<String, MetricsResponse> responseListOfE2E = pair.getFirst();
        // 业务流量(断言)
        Map<String, MetricsResponse> responseListOfBusiness = pair.getSecond();

        // 合并
        Set<String> requestKeySet = param.stream().map(
                request -> request.getAppName() + "|" + request.getServiceName() + "|" + request.getMethodName() + "|"
                        + request.getRpcType() + "|-1").collect(Collectors.toSet());
        Set<String> keySet = new HashSet<String>();
        keySet.addAll(responseListOfE2E.keySet());
        keySet.addAll(responseListOfBusiness.keySet());
        keySet.addAll(requestKeySet);

        // 构造返回对象
        Map<String, E2EBaseResponse> e2eBaseResponseMap = getResponseListByMetricsResultKetSet(keySet,
                E2ENodeErrorInfosResponse.class);
        // 合并计算结果
        Map<String, E2ENodeErrorInfosResponse> responseMap = new HashMap<>();
        for (String key : keySet) {
            String[] params = key.split("\\|");
            String nodeId = getNodeId(params[0], params[1], params[2], params[3]);
            // 这里重新封装响应对象，因为key里包括【异常类型】字段，去掉之后对相同nodeId的数据进行合并
            E2ENodeErrorInfosResponse response = responseMap.get(nodeId);
            if (response == null) {
                response = (E2ENodeErrorInfosResponse) e2eBaseResponseMap.get(key);
                responseMap.put(nodeId, response);
            }
            // 聚合指标
            MetricsModel metricsStatisticsModel = MetricsModel.getMetricsModel(responseStatisticsListOfE2E.get(key.substring(0, key.lastIndexOf("|"))),
                    responseStatisticsListOfBusiness.get(key.substring(0, key.lastIndexOf("|"))));
            // 聚合指标（断言）
            MetricsModel metricsModel = MetricsModel.getMetricsModel(responseListOfE2E.get(key),
                    responseListOfBusiness.get(key));
            // 异常类型：params[4]
            String exceptionType = params[4];
            // 总的错误次数、成功次数、总请求次数，和是否断言错误没有关系
            response.setE2eRequestCount(response.getE2eRequestCount() + metricsStatisticsModel.getE2eTotalCount());
            response.setBusinessRequestCount(response.getBusinessRequestCount() + metricsStatisticsModel.getBusinessTotalCount());
            response.setRequestCount(response.getRequestCount() + metricsStatisticsModel.getTotalCount());
            if (!"-1".equals(exceptionType)) {
                ErrorInfo errorInfo = new ErrorInfo();
                errorInfo.setE2eErrorCount(metricsModel.getE2eErrorCount());
                errorInfo.setBusinessErrorCount(metricsModel.getBusinessErrorCount());
                errorInfo.setErrorCount(metricsModel.getTotalErrorCount());
                errorInfo.setErrorType(exceptionType);
                response.getErrorInfoList().add(errorInfo);
            }
        }
        return Response.success(responseMap.values().stream().collect(Collectors.toList()));
    }

    @Override
    public Response<List<E2EStatisticsResponse>> getStatistics(List<E2EStatisticsRequest> param) {
        long startTime = param.get(0).getStartTime();
        long endTime = param.get(0).getEndTime();
        startTime = timeDelay(startTime);
        endTime = timeDelay(endTime);
        // 查询业务流量和巡检流量
        Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> pair = getMetricsResponses(param,
                "trace_e2e_metrics", null,
                startTime, endTime);
        // 巡检流量
        Map<String, MetricsResponse> responseListOfE2E = pair.getFirst();
        // 业务流量
        Map<String, MetricsResponse> responseListOfBusiness = pair.getSecond();
        // 合并
        //Set<String> keySet = new HashSet<>(
        //CollectionUtil.union(responseListOfE2E.keySet(), responseListOfBusiness.keySet()));
        Set<String> keySet = param.stream().map(
                request -> request.getAppName() + "|" + request.getServiceName() + "|" + request.getMethodName() + "|"
                        + request.getRpcType()).collect(Collectors.toSet());
        // 构造返回对象
        Map<String, E2EBaseResponse> responseMap = getResponseListByMetricsResultKetSet(keySet,
                E2EStatisticsResponse.class);
        // 合并计算结果
        List<E2EStatisticsResponse> responseList = new ArrayList<>();
        for (String key : keySet) {
            E2EStatisticsResponse response = (E2EStatisticsResponse) responseMap.get(key);
            MetricsModel metricsModel = MetricsModel.getMetricsModel(responseListOfE2E.get(key),
                    responseListOfBusiness.get(key));
            response.setE2eRequestCount(metricsModel.getE2eTotalCount());
            response.setBusinessRequestCount(metricsModel.getBusinessTotalCount());
            responseList.add(response);
        }
        return Response.success(responseList);
    }

    /**
     * 添加节点
     *
     * @param request
     */
    @Override
    public void addNode(E2ENodeRequest request) {
        TAMDBPradarE2EConfigDO tamdbPradarE2eConfigDo = new TAMDBPradarE2EConfigDO();
        String nodeId = getNodeId(request);
        tamdbPradarE2eConfigDo.setAppName(request.getAppName());
        tamdbPradarE2eConfigDo.setService(request.getServiceName());
        tamdbPradarE2eConfigDo.setMethod(request.getMethodName());
        tamdbPradarE2eConfigDo.setRpcType(request.getRpcType());
        tamdbPradarE2eConfigDo.setNodeId(nodeId);
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            tamdbPradarE2eConfigDo.setUserAppKey(request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            tamdbPradarE2eConfigDo.setEnvCode(request.getEnvCode());
        }
        pradarE2eConfigMapper.insert(tamdbPradarE2eConfigDo);
    }

    /**
     * 删除节点
     *
     * @param request
     */
    @Override
    public void removeNode(E2ENodeRequest request) {
        TAMDBPradarE2EConfigDO tamdbPradarE2eConfigDo = new TAMDBPradarE2EConfigDO();
        String nodeId = getNodeId(request);
        tamdbPradarE2eConfigDo.setNodeId(nodeId);
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            tamdbPradarE2eConfigDo.setUserAppKey(request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            tamdbPradarE2eConfigDo.setEnvCode(request.getEnvCode());
        }
        pradarE2eConfigMapper.delete(tamdbPradarE2eConfigDo);
    }

    /**
     * 新增断言
     *
     * @param request
     */
    @Override
    public void addAssert(E2EAssertRequest request) {
        TAMDBPradarE2EAssertConfigDO assertConfigDO = new TAMDBPradarE2EAssertConfigDO();
        String nodeId = getNodeId(request);
        assertConfigDO.setNodeId(nodeId);
        assertConfigDO.setAssertCode(request.getAssertCode());
        assertConfigDO.setAssertCondition(request.getAssertCondition());
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            assertConfigDO.setUserAppKey(request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            assertConfigDO.setEnvCode(request.getEnvCode());
        }
        pradarE2eAssertMapper.insert(assertConfigDO);
    }

    /**
     * 删除断言
     *
     * @param request
     */
    @Override
    public void removeAssert(E2EAssertRequest request) {
        TAMDBPradarE2EAssertConfigDO assertConfigDO = new TAMDBPradarE2EAssertConfigDO();
        String nodeId = getNodeId(request);
        assertConfigDO.setNodeId(nodeId);
        assertConfigDO.setAssertCode(request.getAssertCode());
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            assertConfigDO.setUserAppKey(request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            assertConfigDO.setEnvCode(request.getEnvCode());
        }
        pradarE2eAssertMapper.delete(assertConfigDO);
    }

    /**
     * 调整断言
     *
     * @param request
     */
    @Override
    public void modifyAssert(E2EAssertRequest request) {
        TAMDBPradarE2EAssertConfigDO assertConfigDO = new TAMDBPradarE2EAssertConfigDO();
        String nodeId = getNodeId(request);
        assertConfigDO.setNodeId(nodeId);
        assertConfigDO.setAssertCode(request.getAssertCode());
        assertConfigDO.setAssertCondition(request.getAssertCondition());
        Example example = new Example(TAMDBPradarE2EAssertConfigDO.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("nodeId", nodeId);
        criteria.andEqualTo("assertCode", request.getAssertCode());
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            criteria.andEqualTo("envCode", request.getEnvCode());
        }
        pradarE2eAssertMapper.updateByExampleSelective(assertConfigDO, example);
    }

    /**
     * 执行指标查询，并返回巡检查询结果和业务查询结果
     *
     * @param requestList
     * @param group
     * @param startTime
     * @param endTime
     * @return
     */
    private Pair<Map<String, MetricsResponse>, Map<String, MetricsResponse>> getMetricsResponses(
            List<? extends E2EBaseRequest> requestList, String measurement,
            String group, long startTime, long endTime) {
        // 巡检指标查询
        MetricsQueryRequest queryRequest = getMetricsQueryRequest(requestList, measurement, true, group, startTime,
                endTime);
        Map<String, MetricsResponse> e2eMetricsResponseMap = getMetricsResponseMap(queryRequest);
        // 业务指标查询
        queryRequest = getMetricsQueryRequest(requestList, measurement, false, group, startTime, endTime);
        Map<String, MetricsResponse> businessMetricsResponseMap = getMetricsResponseMap(queryRequest);
        return new Pair<>(e2eMetricsResponseMap, businessMetricsResponseMap);
    }

    /**
     * 指标结果预处理，去掉Key中的是否巡检标识，方便对巡检和业务的结果数据进行合并
     * 是否巡检：keys[4]
     *
     * @param queryRequest
     * @return
     */
    private Map<String, MetricsResponse> getMetricsResponseMap(MetricsQueryRequest queryRequest) {
        Map<String, MetricsResponse> metricsResponseMap = metricsService.getMetrics(queryRequest);
        metricsResponseMap = formatKey(metricsResponseMap, key -> {
            String[] keys = key.split("\\|");
            key = keys[0] + "|" + keys[1] + "|" + keys[2] + "|" + keys[3];
            // 分组列要保留
            if (StringUtils.isNotBlank(queryRequest.getGroups())) {
                key = key + "|" + keys[5];
            }
            return key;
        });
        return metricsResponseMap;
    }

    /**
     * 封装指标查询请求参数
     *
     * @param requestList
     * @param isE2e
     * @param group
     * @param startTime
     * @param endTime
     * @return
     */
    private MetricsQueryRequest getMetricsQueryRequest(List<? extends E2EBaseRequest> requestList, String measurement,
                                                       boolean isE2e,
                                                       String group, long startTime, long endTime) {
        MetricsQueryRequest queryRequest = new MetricsQueryRequest();
        queryRequest.setMeasurementName(measurement);
        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("totalCount", "sum(totalCount)");
        fieldsMap.put("errorCount", "sum(errorCount)");
        fieldsMap.put("successCount", "sum(successCount)");
        fieldsMap.put("totalRt", "sum(totalRt)");
        queryRequest.setFieldMap(fieldsMap);
        List<LinkedHashMap<String, String>> tagMapList = new ArrayList<>();
        for (E2EBaseRequest request : requestList) {
            LinkedHashMap<String, String> tagMap = new LinkedHashMap<>();
            tagMap.put("parsedAppName", request.getAppName());
            tagMap.put("parsedServiceName", request.getServiceName());
            tagMap.put("parsedMethod", request.getMethodName());
            tagMap.put("rpcType", request.getRpcType());
            tagMap.put("clusterTest", isE2e ? "1" : "0");
            if (StringUtils.isNotBlank(request.getTenantAppKey())) {
                tagMap.put("userAppKey", request.getTenantAppKey());
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

    /**
     * 根据keySet构造返回结果集
     *
     * @param keySet
     * @param cls
     * @return
     */
    @SneakyThrows
    private Map<String, E2EBaseResponse> getResponseListByMetricsResultKetSet(Set<String> keySet,
                                                                              Class<? extends E2EBaseResponse> cls) {
        Map<String, E2EBaseResponse> responseMap = new HashMap<>();
        for (String key : keySet) {
            E2EBaseResponse response = cls.newInstance();
            String[] params = key.split("\\|");
            response.setAppName(params[0]);
            response.setServiceName(params[1]);
            response.setMethodName(params[2]);
            response.setRpcType(params[3]);
            responseMap.put(key, response);
        }
        return responseMap;
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
            return getNodeId(request.getAppName(), request.getServiceName(), request.getMethodName(), request.getRpcType());
        } else {
            return edgeId;
        }
    }

    /**
     * 生成nodeId
     *
     * @param parsedAppName
     * @param parsedServiceName
     * @param parsedMethod
     * @param rpcType
     * @return
     */
    private String getNodeId(String parsedAppName, String parsedServiceName, String parsedMethod, String rpcType) {
        return Md5Utils.md5(parsedAppName + "|" + parsedServiceName + "|" + parsedMethod + "|" + rpcType);
    }

    /**
     * Map key转换
     *
     * @param oldData
     * @param function
     * @param <T>
     * @return
     */
    private <T> Map<String, T> formatKey(Map<String, T> oldData, Function<String, String> function) {
        Map<String, T> newData = new HashMap<>();
        for (String key : oldData.keySet()) {
            String newKey = function.apply(key);
            newData.put(newKey, oldData.get(key));
        }
        return newData;
    }

    private long timeDelay(long oldTime) {
        long time = oldTime - delayTime * 1000;
        return time < 0 ? 0 : time;
    }
}

@Data
class MetricsModel {
    double e2eTotalCount = 0;
    double e2eSuccessCount = 0;
    double e2eErrorCount = 0;
    double e2eTotalRt = 0;
    double businessTotalCount = 0;
    double businessSuccessCount = 0;
    double businessErrorCount = 0;
    double businessTotalRt = 0;
    double totalCount = 0;
    double totalRt = 0;
    double totalErrorCount = 0;
    double totalSuccessCount = 0;

    /**
     * 合并巡检和业务查询结果
     *
     * @param clusterResponse
     * @param bussinessResponse
     * @return
     */
    public static MetricsModel getMetricsModel(MetricsResponse clusterResponse, MetricsResponse bussinessResponse) {
        MetricsModel metricsModel = new MetricsModel();
        if (clusterResponse != null && CollectionUtils.isNotEmpty(clusterResponse.getValue())) {
            metricsModel.setE2eTotalCount(((Double) clusterResponse.getValue().get(0).get("totalCount")));
            metricsModel.setE2eSuccessCount(((Double) clusterResponse.getValue().get(0).get("successCount")));
            metricsModel.setE2eErrorCount(((Double) clusterResponse.getValue().get(0).get("errorCount")));
            metricsModel.setE2eTotalRt((Double) clusterResponse.getValue().get(0).get("totalRt"));
        }
        if (bussinessResponse != null && CollectionUtils.isNotEmpty(bussinessResponse.getValue())) {
            metricsModel.setBusinessTotalCount(((Double) bussinessResponse.getValue().get(0).get("totalCount"))
            );
            metricsModel.setBusinessSuccessCount(((Double) bussinessResponse.getValue().get(0).get("successCount"))
            );
            metricsModel.setBusinessErrorCount(((Double) bussinessResponse.getValue().get(0).get("errorCount"))
            );
            metricsModel.setBusinessTotalRt((Double) bussinessResponse.getValue().get(0).get("totalRt"));
        }
        metricsModel.totalCount = metricsModel.e2eTotalCount + metricsModel.businessTotalCount;
        metricsModel.totalRt = metricsModel.e2eTotalRt + metricsModel.businessTotalRt;
        metricsModel.totalErrorCount = metricsModel.e2eErrorCount + metricsModel.businessErrorCount;
        metricsModel.totalSuccessCount = metricsModel.e2eSuccessCount + metricsModel.businessSuccessCount;
        return metricsModel;
    }
}
