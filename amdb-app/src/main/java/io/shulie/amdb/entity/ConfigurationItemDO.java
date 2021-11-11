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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.shulie.amdb.response.app.model.ConfigurationItem;
import lombok.Data;

@Data
@Table(name = "t_amdb_configuration_item")
public class ConfigurationItemDO {

    @Id
    @Column(name = "`id`")
    private Long id;
    @Column(name = "`configuration_id`")
    private Long configurationId;
    @Column(name = "`key`")
    private String key;
    @Column(name = "`value`")
    private String value;

    public ConfigurationItem convert() {
        ConfigurationItem item = new ConfigurationItem();
        item.setKey(getKey());
        item.setValue(getValue());
        return item;
    }
}
