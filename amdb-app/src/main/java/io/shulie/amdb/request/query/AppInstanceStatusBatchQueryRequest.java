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

import io.shulie.amdb.common.request.PagingRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppInstanceStatusBatchQueryRequest extends PagingRequest {
    /**
     * 应用名称
     */
    private List<String> appNames;

    /**
     * agentID
     */
    private List<String> agentIds;

    /**
     * 探针状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)
     */
    private String probeStatus;

    /**
     * 应用IP
     */
    private List<String> ipAddress;

    /**
     * 客户Id
     */
    private String tenantKey;

    /**
     * 查询内容
     */
    private List<String> fields;

    public List<String> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }
}
