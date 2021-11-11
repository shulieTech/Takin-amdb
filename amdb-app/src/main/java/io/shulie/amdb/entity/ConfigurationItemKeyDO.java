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

package io.shulie.amdb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.shulie.amdb.response.app.model.ConfigurationKey;
import io.shulie.surge.config.common.ValueTypeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Table(name = "t_amdb_configuration_item_key")
public class ConfigurationItemKeyDO {

    @Id
    @Column(name = "`id`")
    private Long id;
    @Column(name = "`configuration_type`")
    private String configurationType;
    @Column(name = "`name`")
    private String name;
    @Column(name = "`desc`")
    private String desc;
    @Column(name = "value_type")
    private Integer valueType;
    @Column(name = "default_value")
    private String defaultValue;
    @Column(name = "`status`")
    private Integer status;
    @Column(name = "not_empty")
    private Integer notEmpty;
    @Column(name = "gmt_create")
    private Date gmtCreate;

    public ConfigurationKey convert() {
        ConfigurationKey key = new ConfigurationKey();
        key.setType(getConfigurationType());
        key.setName(getName());
        key.setDesc(getDesc());
        key.setValueType(ValueTypeEnum.findByOrdinal(getValueType()).getDesc());
        return key;
    }

    @Override
    public String toString() {
        String content = name + "(" + desc + ", 值类型：" + ValueTypeEnum.findByOrdinal(valueType).getDesc();
        if (StringUtils.isNotBlank(defaultValue)) {
            content += ", 默认值：" + defaultValue;
        }
        if (notEmpty != null && notEmpty.compareTo(1) == 0) {
            content += ", 必填";
        }
        return content + ")";
    }
}
