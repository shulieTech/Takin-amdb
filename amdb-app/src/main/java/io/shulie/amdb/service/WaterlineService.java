package io.shulie.amdb.service;

import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;

import java.util.List;

public interface WaterlineService {
    List<WaterlineMetrics> getAllApplicationWithMetrics(List<String> names, String startTime);

}
