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

import lombok.Data;

import java.util.List;

@Data
public class MetricsFromInfluxdbRequest {
    long startMilli = 0L;
    long endMilli = 0L;         //
    long realSeconds = 0L;      //秒数
    Boolean metricsType = null; //流量类型
    String eagleId = "";        //边ID
    List<String> eagleIds;        //边ID集合
}
