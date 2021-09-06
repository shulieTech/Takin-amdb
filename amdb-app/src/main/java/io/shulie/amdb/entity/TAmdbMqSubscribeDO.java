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
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "`t_amdb_mq_subscribe`")
public class TAmdbMqSubscribeDO {
    /**
     * ID
     */
    @Column(name = "`id`")
    Long id;
    /**
     * 订阅目标
     */
    @Column(name = "`subscribe_target`")
    String subscribeTarget;
    /**
     * Topic
     */
    @Column(name = "`topic`")
    String topic;
    /**
     * 订阅字段
     */
    @Column(name = "`fields`")
    String fields;
    /**
     * 订阅参数
     */
    @Column(name = "`params`")
    String params;
    /**
     * 租户标识
     */
    @Column(name = "`tenant`")
    String tenant;
    /**
     * 创建人编码
     */
    @Column(name = "`creator`")
    String creator;
    /**
     * 创建人名称
     */
    @Column(name = "`creator_name`")
    String creatorName;
    /**
     * 更新人编码
     */
    @Column(name = "`modifier`")
    String modifier;
    /**
     * 更新人名称
     */
    @Column(name = "`modifier_name`")
    String modifierName;
    /**
     * 创建时间
     */
    @Column(name = "`gmt_create`")
    Date gmtCreate;
    /**
     * 更新时间
     */
    @Column(name = "`gmt_modify`")
    Date gmtModify;
}
