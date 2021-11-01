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

package io.shulie.amdb.response.e2e;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("节点异常信息响应")
public class E2ENodeErrorInfosResponse extends E2EBaseResponse {
    /**
     * E2E-请求数
     */
    @ApiModelProperty("E2E-请求数")
    double e2eRequestCount = 0;
    /**
     * 业务请求数
     */
    @ApiModelProperty("业务请求数")
    double businessRequestCount = 0;
    /**
     * 总请求数
     */
    @ApiModelProperty("总请求数")
    double requestCount = 0;
    /**
     * 错误信息列表
     */
    @ApiModelProperty("错误信息列表")
    List<ErrorInfo> errorInfoList = new ArrayList<>();

    @ApiModel("错误信息列表")
    @Data
    public static class ErrorInfo {
        /**
         * E2E-错误数
         */
        @ApiModelProperty("E2E-错误数")
        double e2eErrorCount;
        /**
         * 业务错误数
         */
        @ApiModelProperty("业务错误数")
        double businessErrorCount;
        /**
         * 总错误数
         */
        @ApiModelProperty("总错误数")
        double errorCount;
        /**
         * 错误类型
         */
        @ApiModelProperty("错误类型")
        String errorType;
        /**
         * 最近一次命中断言 traceId
         */
        @ApiModelProperty("traceId")
        String traceId;
    }
}
