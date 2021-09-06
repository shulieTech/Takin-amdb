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
@Table(name = "`t_amdb_middle_ware_instance`")
public class MiddleWareInstanceDO implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    private Long id;
    /**
     * 服务/集群类型
     */
    @Column(name = "server_type")
    private String serverType;
    /**
     * 服务名称
     */
    @Column(name = "server_name")
    private String serverName;
    /**
     * IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;
    /**
     * 创建时间
     */
    @Column(name = "gmt_create")
    private Date gmtCreate;
    /**
     * 更新时间s
     */
    @Column(name = "gmt_modify")
    private Date gmtModify;
}
