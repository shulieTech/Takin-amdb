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

package io.shulie.amdb.common.request.agent;

import io.shulie.amdb.common.request.PagingRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author anjone
 * @date 2021/8/18
 */
@Data
@ApiModel
public class AgentConfigQueryRequest extends PagingRequest implements Serializable {

    @ApiModelProperty("配置英文key")
    private String configKey;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("配置校验结果 ture 校验成功 false校验失败")
    private Boolean status;
}
