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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.shulie.amdb.adaptors.common.Pair;
import io.shulie.amdb.dao.ITraceDao;
import io.shulie.amdb.entity.TAmdbPradarLinkEdgeDO;
import io.shulie.amdb.mapper.PradarLinkEdgeMapper;
import io.shulie.amdb.request.query.MetricsDetailQueryRequest;
import io.shulie.amdb.request.query.MetricsFromInfluxdbQueryRequest;
import io.shulie.amdb.request.query.MetricsQueryRequest;
import io.shulie.amdb.response.metrics.MetricsDetailResponse;
import io.shulie.amdb.response.metrics.MetricsResponse;
import io.shulie.amdb.service.MetricsService;
import io.shulie.amdb.utils.InfluxDBManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private InfluxDBManager influxDbManager;

    @Resource
    PradarLinkEdgeMapper pradarLinkEdgeMapper;

    @Autowired
    @Qualifier("traceDaoImpl")
    ITraceDao traceDao;

    @Override
    public Map<String, MetricsResponse> getMetrics(MetricsQueryRequest request) {
        Map<String, MetricsResponse> responseList = new HashMap<>();

        //遍历每一个tag进行查询
        request.getTagMapList().forEach(tagMap -> {
            List<Map<String, Object>> resultList = new ArrayList<>();

            //构造聚合指标查询sql
            String aggrerateSql = "select " + parseAliasFields(request.getFieldMap()) +
                    " from  " + request.getMeasurementName() + " where " + parseWhereFilter(tagMap) + " and time >= " +
                    formatTimestamp(request.getStartTime()) + " and time < " + formatTimestamp(request.getEndTime()) + " " +
                    parseGroupBy(request.getGroups());
            //log.info("聚合指标查询sql:{}", aggrerateSql);
            List<QueryResult.Result> aggrerateResult = influxDbManager.query(aggrerateSql);

            //构造非聚合指标查询sql
            List<QueryResult.Result> nonAggrerateResult = null;
            if (request.getNonAggrerateFieldMap() != null) {
                String nonAggrerateSql = "select " + parseAliasFields(request.getNonAggrerateFieldMap()) +
                        " from  " + request.getMeasurementName() + " where " + parseWhereFilter(tagMap) + " and time >= " +
                        formatTimestamp(request.getStartTime()) + " and time < " + formatTimestamp(request.getEndTime());
                //log.info("非聚合指标查询sql:{}", nonAggrerateSql);
                nonAggrerateResult = influxDbManager.query(nonAggrerateSql);
            }

            //处理聚合指标
            aggrerateResult.stream().filter((internalResult) -> {
                return Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries());
            }).forEach((internalResult) -> {
                internalResult.getSeries().stream().filter((series) -> {
                    return series.getName().equals(request.getMeasurementName());
                }).forEachOrdered((series) -> {
                    List<Map<String, Object>> tmpResult = new ArrayList<>();
                    MetricsResponse response = new MetricsResponse();
                    LinkedHashMap<String, String> resultTagMap = new LinkedHashMap<>();
                    resultTagMap.putAll(tagMap);
                    if (MapUtils.isNotEmpty(series.getTags())) {
                        String groupFields[] = request.getGroups().split(",");
                        for (String groupField : groupFields) {
                            resultTagMap.put(groupField, series.getTags().get(groupField));
                        }
                    }

                    Iterator iterable = series.getValues().iterator();
                    while (iterable.hasNext()) {
                        Map<String, Object> result = new HashMap<>();
                        List<Object> row = (List) iterable.next();
                        for (int i = 0; i < series.getColumns().size(); i++) {
                            String column = series.getColumns().get(i);
                            result.put(column, row.get(i));
                        }
                        tmpResult.add(result);
                    }
                    response.setTags(resultTagMap);
                    response.setTimes(request.getEndTime() - request.getStartTime());
                    if (!tmpResult.isEmpty()) {
                        resultList.addAll(tmpResult);
                        response.setValue(tmpResult);
                        responseList.put(StringUtils.join(response.getTags().values(), "|"), response);
                    }
                });
            });

            //如果要合并结果集,上面的聚合查询不能带group,可能会造成数据匹配错乱
            if (!resultList.isEmpty() && nonAggrerateResult != null && request.getGroups() == null) {
                nonAggrerateResult.stream().filter((internalResult) -> {
                    return Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries());
                }).forEach((internalResult) -> {
                    internalResult.getSeries().stream().filter((series) -> {
                        return series.getName().equals(request.getMeasurementName());
                    }).forEachOrdered((series) -> {

                        Iterator iterable = series.getValues().iterator();
                        while (iterable.hasNext()) {
                            Map<String, Object> result = new HashMap<>();
                            List<Object> row = (List) iterable.next();
                            for (int i = 0; i < series.getColumns().size(); i++) {
                                String column = series.getColumns().get(i);
                                result.put(column, row.get(i));
                            }
                            //引用传递
                            Map<String, Object> aggrerateMap = resultList.get(0);
                            aggrerateMap.putAll(result);
                        }
                    });
                });
            }
        });
        //log.info("指标查询合并结果:{}", responseList);
        return responseList;
    }

    @Override
    public Pair<List<MetricsDetailResponse>, Integer> metricsDetailes(MetricsDetailQueryRequest request) {
        List<MetricsDetailResponse> resultList2 = new ArrayList<>();
        if (this.cache1.size() == 0 || this.cache2.size() == 0) {
            refreshCache();
        }
        StringBuffer sb = new StringBuffer();
        String service_interface, method_interface;
        String service_active, method_active;
        String appName = request.getAppName();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();

        //拼接SQL-必填字段
        sb.append("select appName,service,method,middlewareName,total,rpcType from trace_metrics " +
                "where time >'" + startTime + "' and time <'" + endTime + "' and appName = '" + appName + "'");
        //拼接SQL-流量类型
        if (request.getClusterTest() != -1) {
            sb.append(" and clusterTest = '" + (0 == request.getClusterTest() ? "false" : "true") + "'");
        }
        //拼接SQL-服务名称
        String serviceName = request.getServiceName();
        if (StringUtils.isNotBlank(serviceName)) {
            String info[] = serviceName.split("#");
            if (info.length == 2) {
                service_interface = info[0];
                method_interface = info[1];
                sb.append(" and service = '" + service_interface + "' and method='" + method_interface + "'");
            } else {
                service_interface = info[0];
                sb.append(" and service = '" + service_interface + "'");
            }
        }
        //拼接SQL-活动名称
        if (StringUtils.isNotBlank(request.getActivityName())) {
            String info[] = request.getActivityName().split("#");
            service_active = info[0];
            method_active = info[1];
            String linkId = pradarLinkEdgeMapper.getLinkId(request.getAppName(), service_active, method_active);
            List<String> list = null;
            if (StringUtils.isNotBlank(linkId)) {
                list = this.cache1.getIfPresent(linkId);
            }
            if (list != null && list.size() > 0) {
                sb.append(" and (");
                for (int i = 0; i < list.size(); i++) {
                    String s2m[] = list.get(i).split("#");
                    if (i != 0) {
                        sb.append(" or ");
                    }
                    sb.append(" (service = '" + s2m[1] + "' and method='" + s2m[2] + "')");
                }
                sb.append(" )");
            }
        }
        //sb.append(" limit "+request.getPageSize()+" OFFSET  "+request.getOffset());
        sb.append(" order by time desc TZ('Asia/Shanghai')");
        log.info("查询sql01:{}", sb);
        List<QueryResult.Result> influxResult = influxDbManager.query(sb.toString());
        List<QueryResult.Series> list = influxResult.get(0).getSeries();
        List<TraceMetrics> resultList = new ArrayList<>();
        if (list != null) {
            for (QueryResult.Series result : list) {
                List columns = result.getColumns();
                List values = result.getValues();
                resultList = getQueryData(columns, values);
            }
        }
        // 提前分页总户数判断
        int size = resultList.size();
        Integer current = request.getCurrentPage();
        Integer pageSize = request.getPageSize();
        if (size <= current * pageSize) {
            return new Pair<>(new ArrayList<>(0), size);
        }

        //计算数据间隔描述，为计算tps准备
        long diffInMillis = 300;
        String maxTime = resultList.get(0).getTime();
        String minTime = resultList.get(size - 1).getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date firstDate = sdf.parse(minTime.replace("T", " ").replace("+08:00", ""));
            Date secondDate = sdf.parse(maxTime.replace("T", " ").replace("+08:00", ""));
            diffInMillis = Math.abs(secondDate.getTime() - firstDate.getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //去重(因为influxDB的group by支持不够),计算指标,计算业务活动入口
        String sql;
        Set<String> allActiveList = new HashSet<>();
        for (TraceMetrics temp : resultList) {
            MetricsDetailResponse response = new MetricsDetailResponse();
            response.setAppName(temp.getAppName());
            response.setMiddlewareName(temp.getMiddlewareName());
            response.setService(temp.getService());
            response.setMethod(temp.getMethod());
            response.setServiceAndMethod(temp.getService() + "#" + temp.getMethod());
            response.setRpcType(temp.getRpcType());
            String key;
            if (!resultList2.contains(response)) {
                key = request.getAppName() + "#" + response.getServiceAndMethod();
                List<String> value = this.cache2.getIfPresent(key);
                if (value != null) {
                    response.setActiveList(value);
                    allActiveList.addAll(value);
                }
                //计算指标
                sql = "select sum(totalCount) as requestCount,sum(totalCount)/" + diffInMillis + " as tps,sum(successCount)/sum(totalCount) as successRatio,sum(totalRt)/sum(totalCount) as responseConsuming from trace_metrics\n" +
                        "where time >'" + startTime + "' and time <'" + endTime + "'\n" +
                        "and appName = '" + response.getAppName() + "'\n" +
                        "and service = '" + response.getService() + "'\n" +
                        "and method = '" + response.getMethod() + "'\n";
                if (request.getClusterTest() != -1) {
                    sql += " and clusterTest = '" + (0 == request.getClusterTest() ? "false" : "true") + "'\n";
                }
                sql += "group by appName,service,method\n" +
                        " TZ('Asia/Shanghai')";
                log.info("查询sql02:{}", sql.replace("\n", " "));
                List<QueryResult.Result> influxResult1 = influxDbManager.query(sql);
                List<QueryResult.Series> list1 = influxResult1.get(0).getSeries();
                float requestCount = Float.parseFloat(list1.get(0).getValues().get(0).get(1).toString());
                float tps = Float.parseFloat(list1.get(0).getValues().get(0).get(2).toString());
                float successRatio = Float.parseFloat(list1.get(0).getValues().get(0).get(3).toString());
                float responseConsuming = Float.parseFloat(list1.get(0).getValues().get(0).get(4).toString());
                response.setRequestCount(requestCount);                 //总请求次数
                response.setTps(tps);                                   //tps
                response.setResponseConsuming(responseConsuming);       //耗时
                response.setSuccessRatio(successRatio);                 //成功率
                resultList2.add(response);
            }
        }
        int responseSize = resultList2.size();
        // 是否可以分页
        if (responseSize <= current * pageSize) {
            return new Pair<>(new ArrayList<>(0), responseSize);
        }
        List<MetricsDetailResponse> currentPageList = sortAndPaging(resultList2, request);
        currentPageList.get(0).setAllActiveList(allActiveList);
        return new Pair<>(currentPageList, responseSize);
    }

    //根据业务活动找服务列表,linkId    /   appname+server+method
    private Cache<String, List<String>> cache1 = CacheBuilder.newBuilder().maximumSize(90000).expireAfterWrite(1, TimeUnit.HOURS).build();
    //查询关联业务活动
    private Cache<String, List<String>> cache2 = CacheBuilder.newBuilder().maximumSize(90000).expireAfterWrite(1, TimeUnit.HOURS).build();

    public MetricsServiceImpl() {
        initCache();
    }

    private synchronized void initCache() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> refreshCache(), 0, 5, TimeUnit.MINUTES);
    }

    private void refreshCache() {
        try {
            if (pradarLinkEdgeMapper == null) {
                return;
            }
            List<TAmdbPradarLinkEdgeDO> allList1 = pradarLinkEdgeMapper.getAllEdge1();
            for (TAmdbPradarLinkEdgeDO edge : allList1) {
                if (this.cache1.getIfPresent(edge.getLinkId()) == null) {
                    this.cache1.put(edge.getLinkId(), new ArrayList<>());
                }
                this.cache1.getIfPresent(edge.getLinkId()).add(edge.getService());
            }
            List<TAmdbPradarLinkEdgeDO> allList2 = pradarLinkEdgeMapper.getAllEdge2();
            for (TAmdbPradarLinkEdgeDO edge : allList2) {
                if (this.cache2.getIfPresent(edge.getService()) == null) {
                    this.cache2.put(edge.getService(), new ArrayList<>());
                }

                if (!this.cache2.getIfPresent(edge.getService()).contains(edge.getExtend())) {
                    this.cache2.getIfPresent(edge.getService()).add(edge.getExtend());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理查询列
     *
     * @param fieldsMap
     * @return
     */
    private String parseAliasFields(Map<String, String> fieldsMap) {
        List<String> aliasList = new ArrayList<>();
        fieldsMap.forEach((k, v) -> {
            aliasList.add(v + " as " + k);
        });
        return StringUtils.join(aliasList, ",");
    }

    /**
     * 处理查询条件
     *
     * @param tagMap
     * @return
     */
    private String parseWhereFilter(Map<String, String> tagMap) {
        List<String> filterList = new ArrayList<>();
        tagMap.forEach((k, v) -> {
            filterList.add(k + "='" + v + "'");
        });
        return StringUtils.join(filterList, " and ");
    }

    /**
     * 处理分组条件
     *
     * @param groupFields
     * @return
     */
    private String parseGroupBy(String groupFields) {
        if (StringUtils.isBlank(groupFields)) {
            return "";
        }
        return "group by " + groupFields;
    }

    /**
     * 时间格式化
     *
     * @param timestamp
     * @return
     */
    private long formatTimestamp(long timestamp) {
        String temp = timestamp + "000000";
        return Long.valueOf(temp);
    }

    /**
     * 浮点数据格式化
     *
     * @param data
     * @return
     */
    private BigDecimal formatDouble(Double data) {
        if (data == null) {
            return new BigDecimal("0");
        }
        BigDecimal b = BigDecimal.valueOf(data);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP);
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

    // 按照控制台传递过来的关注列表(优先排序)、排序字段、以及分页参数进行排序和分页，
    private List<MetricsDetailResponse> sortAndPaging(List<MetricsDetailResponse> allResult, MetricsDetailQueryRequest request) {
        List<String> focusOn = request.getAttentionList();
        String[] orderBy = request.getOrderBy().split(" ");
        String orderName = orderBy[0];
        String orderType;
        if (orderBy.length < 2) {
            orderType = "desc";
        } else {
            orderType = orderBy[1];
        }
        boolean orderByAsc = "asc".equalsIgnoreCase(orderType);
        List<MetricsDetailResponse> responseList = allResult.stream().sorted((left, right) -> {
            boolean leftAttention = focusOn.contains(left.getServiceAndMethod());
            boolean rightAttention = focusOn.contains(right.getServiceAndMethod());
            if ((leftAttention && rightAttention) || (!leftAttention && !rightAttention)) {
                float result = 0f;
                switch (orderName) {
                    case "QPS":
                        result = left.getTps() - right.getTps();
                        break;
                    case "TPS":
                        result = left.getRequestCount() - right.getRequestCount();
                        break;
                    case "RT":
                        result = left.getResponseConsuming() - right.getResponseConsuming();
                        break;
                    case "SUCCESSRATE":
                        result = left.getSuccessRatio() - right.getSuccessRatio();
                        break;
                }
                int diff = result > 0 ? 1 : -1;
                return orderByAsc ? diff : -diff;
            } else {
                return leftAttention ? -1 : 1;
            }
        }).collect(Collectors.toList());
        Integer current = request.getCurrentPage();
        Integer pageSize = request.getPageSize();
        int limit = Integer.min((current + 1) * pageSize, responseList.size());
        List<MetricsDetailResponse> responses = new ArrayList<>(pageSize);
        for (int i = current * pageSize; i < limit; i++) {
            responses.add(responseList.get(i));
        }
        return responses;
    }

    public String entranceFromChickHouse(MetricsFromInfluxdbQueryRequest request) {
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        String in_appName = request.getInAppName();          //入口应用
        String in_service = request.getInService();          //入口接口
        String in_method = request.getInMethod();            //入口方法
        String sql1 = "select DISTINCT entranceId from t_trace_all " +
                "where startDate between '" + startTime + "' and  '" + endTime + "' " +
                "and parsedServiceName ='" + in_service + "' and parsedMethod = '" + in_method + "' " +
                "and parsedAppName = '" + in_appName + "'";
        List<Map<String, Object>> entranceIdList = traceDao.queryForList(sql1);
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> temp : entranceIdList) {
            String tempEntranceId = temp.get("entranceId") != null ? temp.get("entranceId").toString() : "test";
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append("'" + tempEntranceId + "'");
        }
        return sb.toString();
    }

    public Map<String, Object> metricsFromChickHouse(MetricsFromInfluxdbQueryRequest request) {
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        String entranceStr = request.getEntranceStr();
        int clusterTest = request.getClusterTest();             //-1,混合  0,业务  1,压测
        String f_appName = request.getFromAppName();           //上游应用
        String t_appName = request.getAppName();                //应用
        String t_service = request.getService();                //接口
        String t_method = request.getMethod();                  //方法
        StringBuilder where1 = new StringBuilder();
        where1.append(
                "where startDate between '" + startTime + "' and  '" + endTime + "' " +
                        "and upAppName = '" + f_appName + "' " +
                        "and parsedServiceName ='" + t_service + "' and parsedMethod = '" + t_method + "' " +
                        "and parsedAppName = '" + t_appName + "' " +
                        "and entranceId in(" + entranceStr + ") ");
        if (clusterTest != -1) {
            where1.append(" and clusterTest = '" + (0 == clusterTest ? "0" : "1") + "'");
        }
        String selectsql1 = "select sum(toInt8(samplingInterval)) as allTotalCount,\n" +
                "MAX(cost) as allMaxRt,\n" +
                "sum(cost) as allTotalRt,\n" +
                "(sum(toInt8(samplingInterval))/210) as allTotalTps\n" +
                "from t_trace_all \n" + where1;
        Map<String, Object> modelList = traceDao.queryForMap(selectsql1);
        if(modelList.get("allTotalCount")==null){
            modelList.put("allTotalCount",0);
        }
        if(modelList.get("allTotalTps")==null){
            modelList.put("allTotalTps",0);
        }
        if(modelList.get("allSuccessCount")==null){
            modelList.put("allSuccessCount",0);
        }
        modelList.put("realSeconds",210);
        String selectsql2 = "select sum(toInt8(samplingInterval)) as allSuccessCount\n" +
                "from t_trace_all \n" + where1 + " and resultCode in('00','200') ";
        Map<String, Object> successCount = traceDao.queryForMap(selectsql2);
        modelList.putAll(successCount);
        if(modelList.get("allSuccessCount")==null){
            modelList.put("allSuccessCount",0);
        }
        return modelList;
    }

}
