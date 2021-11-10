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
import java.util.Date;

@Data
@Table(name = "`t_amdb_pradar_link_edge`")
public class TAmdbPradarLinkEdgeDO extends BaseDatabaseDO {
    @Id
    @Column(name = "id")
    Integer id;
    @Column(name = "link_id")
    String linkId;
    @Column(name = "service")
    String service;
    @Column(name = "method")
    String method;
    @Column(name = "extend")
    String extend;
    @Column(name = "app_name")
    String appName;
    @Column(name = "trace_app_name")
    String traceAppName;
    @Column(name = "server_app_name")
    String serverAppName;
    @Column(name = "rpc_type")
    String rpcType;
    @Column(name = "log_type")
    String logType;
    @Column(name = "middleware_name")
    String middlewareName;
    @Column(name = "entrance_id")
    String entranceId;
    @Column(name = "from_app_id")
    String fromAppId;
    @Column(name = "to_app_id")
    String toAppId;
    @Column(name = "edge_id")
    String edgeId;
    @Column(name = "gmt_create")
    Date gmtCreate;
    @Column(name = "gmt_modify")
    Date gmtModify;
}
