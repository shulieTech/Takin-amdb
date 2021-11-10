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

import lombok.Data;

@Table(name = "t_amdb_pradar_e2e_assert_config")
@Data
public class TAMDBPradarE2EAssertConfigDO extends BaseDatabaseDO {
    @Id
    @Column(name = "`id`")
    Long id;
    @Column(name = "`node_id`")
    String nodeId;
    @Column(name = "`assert_code`")
    String assertCode;
    @Column(name = "`assert_condition`")
    String assertCondition;
    @Column(name = "`gmt_create`")
    Date gmtCreate;
    @Column(name = "`gmt_modify`")
    Date gmtModify;
}
