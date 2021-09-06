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
@ApiModel("统计信息响应")
public class E2EStatisticsResponse extends E2EBaseResponse{
    /**
      * 业务请求数量
     */
    @ApiModelProperty("业务请求数量")
    double businessRequestCount;
    /**
     * 巡检请求数量
     */
    @ApiModelProperty("巡检请求数量")
    double e2eRequestCount;

}
