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

package io.shulie.amdb.request.submit;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("更新MQ订阅")
public class MqSubscribeUpdateSubmitRequest extends AbstractAmdbBaseRequest {
    /**
     * 订阅目标
     */
    @ApiModelProperty("订阅目标")
    String subscribeTarget;
    /**
     * 订阅字段
     */
    @ApiModelProperty("订阅字段")
    String fields;
    /**
     * 订阅参数
     */
    @ApiModelProperty("订阅参数")
    String params;
}
