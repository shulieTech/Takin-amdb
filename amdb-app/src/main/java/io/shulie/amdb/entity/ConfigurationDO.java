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
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.shulie.amdb.response.app.model.Configuration;
import lombok.Data;
import org.springframework.util.CollectionUtils;

@Data
@Table(name = "t_amdb_configuration")
public class ConfigurationDO {

    @Id
    @Column(name = "`id`")
    private Long id;
    @Column(name = "`number`")
    private String number;
    @Column(name = "`name`")
    private String name;
    @Column(name = "`desc`")
    private String desc;
    @Column(name = "`status`")
    private Integer status;
    @Column(name = "type")
    private String type;
    @Column(name = "`is_delete`")
    private Integer deleted;
    @Column(name = "gmt_create")
    private Date gmtCreate;
    @Column(name = "gmt_modify")
    private Date gmtModify;
    @Column(name = "available_env")
    private Integer availableEnv;

    @Transient
    private List<ConfigurationItemDO> itemDOList;

    public Configuration convert(boolean convertItems) {
        Configuration configuration = new Configuration();
        configuration.setNumber(getNumber());
        configuration.setName(getName());
        configuration.setDesc(getDesc());
        configuration.setType(getType());
        configuration.setAvailableEnv(availableEnv != null && availableEnv.compareTo(1) == 0 ? "生产" : "非生产");
        List<ConfigurationItemDO> itemDOList = getItemDOList();
        if (convertItems && !CollectionUtils.isEmpty(itemDOList)) {
            configuration.setItemList(itemDOList.stream().map(ConfigurationItemDO::convert).collect(Collectors.toList()));
        }
        return configuration;
    }
}
