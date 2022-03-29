package io.shulie.amdb.service;

import io.shulie.amdb.common.dto.waterline.TendencyChart;
import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;

import java.util.List;

public interface WaterlineService {
    List<WaterlineMetrics> getAllApplicationWithMetrics(List<String> names, String startTime);

    List<TendencyChart> getTendencyChart(String applicationName, String startTime, String endTime, List<String> nodes);
}
