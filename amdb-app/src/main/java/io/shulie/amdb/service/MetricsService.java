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

package io.shulie.amdb.service;

import io.shulie.amdb.adaptors.common.Pair;
import io.shulie.amdb.request.query.MetricsDetailQueryRequest;
import io.shulie.amdb.request.query.MetricsFromInfluxdbQueryRequest;
import io.shulie.amdb.request.query.MetricsFromInfluxdbRequest;
import io.shulie.amdb.request.query.MetricsQueryRequest;
import io.shulie.amdb.response.metrics.MetricsDetailResponse;
import io.shulie.amdb.response.metrics.MetricsResponse;

import java.util.List;
import java.util.Map;

public interface MetricsService {
    Map<String, MetricsResponse> getMetrics(MetricsQueryRequest request);

    Pair<List<MetricsDetailResponse>, Integer> metricsDetailes(MetricsDetailQueryRequest request);

    String entranceFromChickHouse(MetricsFromInfluxdbQueryRequest request);

    Map<String, Object> metricsFromChickHouse(MetricsFromInfluxdbQueryRequest request);

    List<Map<String, Object>> metricFromInfluxdb(MetricsFromInfluxdbRequest request);

}
