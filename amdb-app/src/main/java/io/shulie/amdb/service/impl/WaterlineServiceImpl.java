package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.dto.waterline.TendencyChart;
import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;
import io.shulie.amdb.service.WaterlineService;
import io.shulie.amdb.utils.InfluxDBManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WaterlineServiceImpl implements WaterlineService {

    @Autowired
    private InfluxDBManager influxDbManager;

    @Override
    public List<WaterlineMetrics> getAllApplicationWithMetrics(List<String> names, String startTime, String tenantAppKey, String envCode) {
        startTime += "000000";
        //查询app_base_data平均数据
        StringBuilder sql = new StringBuilder("select mean(cpu_rate) as cpu_rate,mean(mem_rate) as memory from app_base_data where time >= ")
                .append(startTime)
                .append(" and env_code = '")
                .append(envCode)
                .append("'")
                .append(" and tenant_app_key = '")
                .append(tenantAppKey)
                .append("'");
        if (CollectionUtils.isNotEmpty(names)) {
            sql.append(" and ( ");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i).replace("[", "").replace("]", "").replace("\"", "");
                sql.append("tag_app_name = '").append(name).append("'");
                if (i != names.size() - 1) {
                    sql.append(" or ");
                }
            }
            sql.append(" )");
        }
        sql.append(" group by tag_app_name");
        log.debug(String.valueOf(sql));
        QueryResult queryResult = influxDbManager.query(sql.toString(), "base");
        List<QueryResult.Result> results = queryResult.getResults();
        if (CollectionUtils.isNotEmpty(results)) {
            QueryResult.Result result = results.get(0);
            List<QueryResult.Series> series = result.getSeries();
            if (CollectionUtils.isNotEmpty(series)) {
                return series.stream().map(s -> {
                    Map<String, String> tags = s.getTags();
                    String applicationName = null;
                    double cpu = 0;
                    double memory = 0;
                    if (null != tags) {
                        applicationName = tags.get("tag_app_name");
                    }
                    List<List<Object>> values = s.getValues();
                    if (CollectionUtils.isNotEmpty(values)) {
                        List<Object> objects = values.get(0);
                        if (CollectionUtils.isNotEmpty(objects)) {
                            cpu = (Double) objects.get(1);
                            memory = (Double) objects.get(2);
                        }
                    }
                    DecimalFormat df = new DecimalFormat("00");
                    return new WaterlineMetrics(applicationName, df.format(cpu), df.format(memory));
                }).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public List<TendencyChart> getTendencyChart(String applicationName, String startTime, String endTime, List<String> nodes, String tenantAppKey, String envCode) {
        startTime += "000000";
        endTime += "000000";
        List<TendencyChart> tendencyCharts = new ArrayList<>();
        StringBuilder nodeSql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(nodes) && !org.apache.commons.lang3.StringUtils.equals("[]",nodes.get(0))) {
            for (int i = 0; i < nodes.size(); i++) {
                nodeSql.append(" agentId = '")
                        .append(nodes.get(i).replace("[", "").replace("]", "").replace("\"", ""))
                        .append("'");
                if (i != nodes.size() - 1) {
                    nodeSql.append(" or");
                }
            }
        }
        //先查询指标表里的数据
        StringBuilder sql = new StringBuilder("select totalCount,appName,agentId,totalTps from waterline_trace_metrics where")
                .append(" appName = '")
                .append(applicationName)
                .append("'");
        if (StringUtils.hasText(nodeSql.toString())) {
            sql.append(" and (")
                    .append(nodeSql)
                    .append(" )");
        }
        sql.append(" and time >= ")
                .append(startTime)
                .append(" and time <= ")
                .append(endTime)
                .append(" and env_code = '")
                .append(envCode)
                .append("'")
                .append(" and tenant_app_key = '")
                .append(tenantAppKey)
                .append("'");
        QueryResult queryResult = influxDbManager.query(sql.toString(), "pradar");
        List<QueryResult.Result> results = queryResult.getResults();
        QueryResult.Result result = results.get(0);
        List<QueryResult.Series> series = result.getSeries();
        if (CollectionUtils.isNotEmpty(series)) {
            String finalStartTime = startTime;
            String finalEndTime = endTime;
            Map<String, Integer> offsetMap = new HashMap<>();
            for (QueryResult.Series s : series) {
                List<List<Object>> values = s.getValues();
                if (CollectionUtils.isNotEmpty(values)) {
                    //遍历每条数据查询base_data表
                    values.forEach(objects -> {
                        TendencyChart tendencyChart = new TendencyChart();
                        tendencyChart.setTime(objects.get(0).toString());
                        tendencyChart.setTotalCount((double) objects.get(1));
                        tendencyChart.setApplicationName(objects.get(2).toString());
                        tendencyChart.setAgentId(objects.get(3).toString());
                        tendencyChart.setTotalTps((Double) objects.get(4));
                        StringBuilder baseSql = new StringBuilder();
                        Integer offset = offsetMap.get(objects.get(3).toString());
                        int offsetNum = null == offset ? 0 : offset.intValue();
                        baseSql.append("select cpu_rate,mem_rate,disk,net_bandwidth_rate,tag_app_name,tag_agent_id,cpu_load from app_base_data where time >= ")
                                .append(finalStartTime)
                                .append(" and time <= ")
                                .append(finalEndTime)
                                .append(" and tag_app_name = '")
                                .append(objects.get(2).toString())
                                .append("'")
                                .append(" and tag_agent_id = '")
                                .append(objects.get(3).toString())
                                .append("'")
                                .append(" limit 5 offset ")
                                .append(offsetNum);
                        offsetMap.put(objects.get(3).toString(), offsetNum + 5);
                        //数据的聚合5S的数据聚合成一条
                        QueryResult queryBaseResult = influxDbManager.query(baseSql.toString(), "base");
                        List<QueryResult.Result> baseResults = queryBaseResult.getResults();
                        QueryResult.Result baseResult = baseResults.get(0);
                        List<QueryResult.Series> baseSeries = baseResult.getSeries();
                        if (CollectionUtils.isNotEmpty(baseSeries)) {
                            AtomicInteger size = new AtomicInteger();
                            DecimalFormat df = new DecimalFormat("0.00");
                            baseSeries.stream().map(QueryResult.Series::getValues).forEach(baseValue -> {
                                if (CollectionUtils.isNotEmpty(baseValue)) {
                                    size.set(baseValue.size());
                                    baseValue.forEach(baseObjects -> {
                                        double cpuRate;
                                        double memory;
                                        double disk;
                                        double net;
                                        double cpuLoad;
                                        if (CollectionUtils.isNotEmpty(baseObjects)) {
                                            cpuRate = (double) baseObjects.get(1) + Double.parseDouble(tendencyChart.getCpuRate());
                                            memory = (double) baseObjects.get(2) + Double.parseDouble(tendencyChart.getMemory());
                                            disk = (double) baseObjects.get(3) + Double.parseDouble(tendencyChart.getDisk());
                                            net = (double) baseObjects.get(4) + Double.parseDouble(tendencyChart.getNet());
                                            cpuLoad = (double) baseObjects.get(7) + Double.parseDouble(tendencyChart.getCpuLoad());
                                            tendencyChart.setCpuRate(df.format(cpuRate));
                                            tendencyChart.setMemory(df.format(memory));
                                            tendencyChart.setDisk(df.format(disk));
                                            tendencyChart.setNet(df.format(net));
                                            tendencyChart.setCpuLoad(df.format(cpuLoad));
                                        }
                                    });
                                }
                            });
                            tendencyChart.setCpuRate(df.format(Double.parseDouble(tendencyChart.getCpuRate()) / size.get()));
                            tendencyChart.setMemory(df.format(Double.parseDouble(tendencyChart.getMemory()) / size.get()));
                            tendencyChart.setDisk(df.format(Double.parseDouble(tendencyChart.getDisk()) / size.get()));
                            tendencyChart.setNet(df.format(Double.parseDouble(tendencyChart.getNet()) / size.get()));
                            tendencyCharts.add(tendencyChart);
                        }
                    });
                }
            }
        }
        return tendencyCharts;
    }
}
