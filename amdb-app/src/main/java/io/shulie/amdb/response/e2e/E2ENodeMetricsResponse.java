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

@Data
@ApiModel("节点指标信息响应")
public class E2ENodeMetricsResponse extends E2EBaseResponse {
    /**
     * 成功率
     */
    @ApiModelProperty("成功率")
    double successRate;

    /**
     * 成功次数
     */
    @ApiModelProperty("成功次数")
    double successCount;

    /**
     * 总次数
     */
    @ApiModelProperty("总次数")
    double totalCount;

    /**
     * 平均RT
     */
    @ApiModelProperty("平均RT")
    double rt;
    /**
     * QPS
     */
    @ApiModelProperty("平均QPS/TPS")
    double qps;
    /**
     * maxRt
     */
    @ApiModelProperty("最大RT")
    double maxRt;

    @ApiModelProperty("SQL语句")
    String sqlStatement;

    @ApiModelProperty("耗时最长的traceId")
    String traceId;
}
