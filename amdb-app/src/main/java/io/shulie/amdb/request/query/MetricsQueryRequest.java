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

package io.shulie.amdb.request.query;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MetricsQueryRequest extends AbstractAmdbBaseRequest {
    /**
     * 表名
     */
    String measurementName;
    /**
     * tags
     */
    List<LinkedHashMap<String, String>> tagMapList;
    /**
     * aggrerate filelds
     */
    Map<String, String> fieldMap;
    /**
     * non-aggrerate filelds
     */
    Map<String, String> nonAggrerateFieldMap;
    /**
     * Group
     */
    String groups;
    /**
     * startTime
     */
    long startTime;
    /**
     * endTime
     */
    long endTime;
}
