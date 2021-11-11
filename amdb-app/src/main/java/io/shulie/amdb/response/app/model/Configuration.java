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

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Configuration {

    @ApiModelProperty("配置编码")
    private String number;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("配置描述")
    private String desc;

    @ApiModelProperty("配置类型：参考 ConfigurationTypeEnum")
    private String type;

    @ApiModelProperty("适用环境")
    private String availableEnv;

    @ApiModelProperty("配置项")
    private List<ConfigurationItem> itemList;
}
