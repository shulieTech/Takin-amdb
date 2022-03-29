package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.dto.waterline.TendencyChart;
import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;
import io.shulie.amdb.service.WaterlineService;
import io.shulie.amdb.utils.InfluxDBManager;
import org.apache.commons.collections.CollectionUtils;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WaterlineServiceImpl implements WaterlineService {

    @Autowired
    private InfluxDBManager influxDbManager;

    @Override
    public List<WaterlineMetrics> getAllApplicationWithMetrics(List<String> names, String startTime) {
        startTime += "000000";
        StringBuilder sql = new StringBuilder("select sum(cpu_load) as cpu_load,sum(memory) as memory from app_base_data where time >= ")
                .append(startTime);
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
        System.out.println(sql);
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
    public List<TendencyChart> getTendencyChart(String applicationName, String startTime, String endTime, List<String> nodes) {
        startTime += "000000";
        endTime += "000000";
        List<TendencyChart> tendencyCharts = new ArrayList<>();
        StringBuilder nodeSql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (int i = 0; i < nodes.size(); i++) {
                nodeSql.append(" hostIp = '")
                        .append(nodes.get(i).replace("[","").replace("]","").replace("\"",""))
                        .append("'");
                if (i != nodes.size() - 1) {
                    nodeSql.append(" or");
                }
            }
        }
        StringBuilder sql = new StringBuilder("select sum(totalCount) as totalCount from waterline_trace_metrics where")
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
                .append(" group by appName,hostIp");
        System.out.println(sql);
        QueryResult queryResult = influxDbManager.query(sql.toString(), "pradar");
        List<QueryResult.Result> results = queryResult.getResults();
        QueryResult.Result result = results.get(0);
        List<QueryResult.Series> series = result.getSeries();
        if (CollectionUtils.isNotEmpty(series)) {
            String finalStartTime = startTime;
            String finalEndTime = endTime;
            series.forEach(s -> {
                double totalCount;
                Map<String, String> tags = s.getTags();
                TendencyChart tendencyChart = new TendencyChart();
                if (null != tags) {
                    tendencyChart.setApplicationName(tags.get("appName"));
                    tendencyChart.setHostIp(tags.get("hostIp"));
                }
                List<List<Object>> values = s.getValues();
                if (CollectionUtils.isNotEmpty(values)) {
                    List<Object> objects = values.get(0);
                    if (CollectionUtils.isNotEmpty(objects)) {
                        totalCount = (double) objects.get(1);
                        tendencyChart.setTotalCount(totalCount);
                    }
                    StringBuilder baseSql = new StringBuilder();
                    baseSql.append("select sum(cpu_load) as cpu,sum(memory) as memory,sum(disk) as disk,sum(net_bandwidth) as net from app_base_data where time >= ")
                            .append(finalStartTime)
                            .append(" and time <= ")
                            .append(finalEndTime);
                    if (null != tags) {
                        baseSql.append(" and tag_app_name = '")
                                .append(tags.get("appName"))
                                .append("'")
                                .append(" and tag_app_ip = '")
                                .append(tags.get("hostIp"))
                                .append("'");
                    }
                    baseSql.append(" group by tag_app_name,tag_app_ip");
                    System.out.println(baseSql);
                    QueryResult queryBaseResult = influxDbManager.query(baseSql.toString(), "base");
                    List<QueryResult.Result> baseResults = queryBaseResult.getResults();
                    QueryResult.Result baseResult = baseResults.get(0);
                    List<QueryResult.Series> baseSeries = baseResult.getSeries();
                    if (CollectionUtils.isNotEmpty(baseSeries)) {
                        baseSeries.stream().map(QueryResult.Series::getValues).forEach(baseValue -> {
                            double cpu;
                            double memory;
                            double disk;
                            double net;
                            DecimalFormat df = new DecimalFormat("00");
                            if (CollectionUtils.isNotEmpty(baseValue)) {
                                List<Object> baseObjects = baseValue.get(0);
                                if (CollectionUtils.isNotEmpty(baseObjects)) {
                                    cpu = (double) baseObjects.get(1);
                                    memory = (double) baseObjects.get(2);
                                    disk = (double) baseObjects.get(3);
                                    net = (double) baseObjects.get(4);
                                    tendencyChart.setCpuLoad(df.format(cpu));
                                    tendencyChart.setMemory(df.format(memory));
                                    tendencyChart.setDisk(df.format(disk));
                                    tendencyChart.setNet(df.format(net));
                                }
                            }
                        });
                    }
                }
                tendencyCharts.add(tendencyChart);
            });
        }
        return tendencyCharts;
    }
}
