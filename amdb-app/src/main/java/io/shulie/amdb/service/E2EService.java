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

import java.util.List;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.request.query.E2EAssertRequest;
import io.shulie.amdb.request.query.E2ENodeErrorInfosRequest;
import io.shulie.amdb.request.query.E2ENodeMetricsRequest;
import io.shulie.amdb.request.query.E2ENodeRequest;
import io.shulie.amdb.request.query.E2EStatisticsRequest;
import io.shulie.amdb.response.e2e.E2ENodeErrorInfosResponse;
import io.shulie.amdb.response.e2e.E2ENodeMetricsResponse;
import io.shulie.amdb.response.e2e.E2EStatisticsResponse;

public interface E2EService {
    /**
     * 节点指标数据查询
     *
     * @param param
     * @return
     */
    Response<List<E2ENodeMetricsResponse>> getNodeMetrics(List<E2ENodeMetricsRequest> param);

    /**
     * 节点异常信息查询
     *
     * @param param
     * @return
     */
    Response<List<E2ENodeErrorInfosResponse>> getNodeErrorInfos(List<E2ENodeErrorInfosRequest> param);

    /**
     * 统计信息
     *
     * @param param
     * @return
     */
    Response<List<E2EStatisticsResponse>> getStatistics(List<E2EStatisticsRequest> param);

    /**
     * 添加节点
     *
     * @param request
     */
    void addNode(E2ENodeRequest request);

    /**
     * 移除节点
     *
     * @param request
     */
    void removeNode(E2ENodeRequest request);

    /**
     * 添加断言
     * @param request
     */
    void addAssert(E2EAssertRequest request);

    /**
     * 删除断言
     * @param request
     */
    void removeAssert(E2EAssertRequest request);

    /**
     * 调整断言
     * @param request
     */
    void modifyAssert(E2EAssertRequest request);
}
