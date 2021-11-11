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

package io.shulie.amdb.response.app.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ConfigurationKey {

    @ApiModelProperty("配置项类型")
    private String type;

    @ApiModelProperty("配置项名称")
    private String name;

    @ApiModelProperty("配置项描述")
    private String desc;

    @ApiModelProperty("配置项值类型")
    private String valueType;
}
