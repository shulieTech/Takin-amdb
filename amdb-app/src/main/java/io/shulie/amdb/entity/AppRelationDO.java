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

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "`t_amdb_app_relation`")
public class AppRelationDO implements Serializable {
    /**
     * 实例id
     */
    @Id
    @Column(name = "id")
    Integer id;
    /**
     * From应用
     */
    @Column(name = "from_app_name")
    String fromAppName;
    /**
     * To应用
     */
    @Column(name = "to_app_name")
    String toAppName;
    /**
     * 创建人编码
     */
    @Column(name = "creator")
    String creator;
    /**
     * 创建人名称
     */
    @Column(name = "creator_name")
    String creatorName;
    /**
     * 租户标示
     */
    @Column(name = "tenant")
    String tenant;
    /**
     * 创建时间
     */
    @Column(name = "gmt_create")
    Date gmtCreate;
    /**
     * 更新时间
     */
    @Column(name = "gmt_modify")
    Date gmtModify;
}
