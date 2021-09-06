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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.request.query.MachineMetricsReportQueryRequest;
import io.shulie.amdb.request.submit.MachineMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.MachineMetricsReportResponse;

import java.util.List;

public interface MachineMetricsReportService {
    /**
     * 批量插入
     * @param requestList
     * @return
     */
    Response batchInsert(List<MachineMetricsReportSubmitRequest> requestList);

    /**
     * 批量查询
     * @param request
     * @return
     */
    Response<MachineMetricsReportResponse> batchQuery(MachineMetricsReportQueryRequest request);
}
