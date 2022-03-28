package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;
import io.shulie.amdb.service.WaterlineService;
import io.shulie.amdb.utils.InfluxDBManager;
import org.apache.commons.collections.CollectionUtils;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WaterlineServiceImpl implements WaterlineService {

    @Autowired
    private InfluxDBManager influxDbManager;

    @Override
    public List<WaterlineMetrics> getAllApplicationWithMetrics(List<String> names, String startTime) {
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
}
