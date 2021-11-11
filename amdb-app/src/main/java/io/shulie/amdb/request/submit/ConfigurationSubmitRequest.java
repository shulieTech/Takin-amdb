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

import java.util.Date;
import java.util.List;

import io.shulie.amdb.entity.ConfigurationDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("配置新增")
public class ConfigurationSubmitRequest {

    @ApiModelProperty("配置编码")
    private String number;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("配置描述")
    private String desc;

    @ApiModelProperty("配置类型，参考：ConfigurationTypeEnum")
    private String type;

    @ApiModelProperty("适用环境：0-非生产，1-生产")
    private Integer availableEnv;

    @ApiModelProperty("详细配置项")
    private List<ConfigurationItem> items;

    @Data
    public static class ConfigurationItem {

        @ApiModelProperty("配置项key")
        private String key;

        @ApiModelProperty("配置项value")
        private String value;
    }

    public ConfigurationDO convertAdd() {
        Date date = new Date();
        ConfigurationDO configuration = new ConfigurationDO();
        configuration.setNumber(this.getNumber());
        configuration.setName(this.getName());
        configuration.setDesc(this.getDesc());
        configuration.setType(this.getType());
        configuration.setStatus(1);
        configuration.setDeleted(0);
        configuration.setAvailableEnv(this.getAvailableEnv());
        configuration.setGmtCreate(date);
        configuration.setGmtModify(date);
        return configuration;
    }
}
