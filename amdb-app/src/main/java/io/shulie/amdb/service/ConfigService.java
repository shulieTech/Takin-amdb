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
import io.shulie.amdb.request.query.ConfigQueryRequest;
import io.shulie.amdb.request.submit.ConfigAddOrUpdateSubmitRequest;

public interface ConfigService {

    /**
     * 更新或创建配置项
     *
     * @param submitRequest
     * @return
     */
    Response updateOrInsertConfig(ConfigAddOrUpdateSubmitRequest submitRequest);

    /**
     * 查询配置内容
     *
     * @param queryRequest
     * @return
     */
    String getConfigValue(ConfigQueryRequest queryRequest);

    /**
     * 查询配置内容
     *
     * @param tenant
     * @param atrName
     * @return
     */
    String getConfigValue(String tenant, String atrName);
}
