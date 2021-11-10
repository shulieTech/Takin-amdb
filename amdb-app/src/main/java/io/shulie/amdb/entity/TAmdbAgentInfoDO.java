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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel("")
@Table(name = "`t_amdb_agent_info`")
public class TAmdbAgentInfoDO extends BaseDatabaseDO {
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("")
    private Integer id;

    /**
     * agent Id
     */
    @Column(name = "`agent_id`")
    @ApiModelProperty("agent Id")
    private String agentId;

    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * agentip
     */
    @Column(name = "`ip`")
    @ApiModelProperty("agentip")
    private String ip;

    /**
     * agent端口
     */
    @Column(name = "`port`")
    @ApiModelProperty("agent端口")
    private Integer port;

    @Column(name = "`user_app_key`")
    @ApiModelProperty("")
    private String userAppKey;

    /**
     * agent日志时间
     */
    @Column(name = "`agent_timestamp`")
    @ApiModelProperty("agent日志时间")
    private Long agentTimestamp;

    /**
     * 创建时间
     */
    @Column(name = "`gmt_create`")
    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    /**
     * agent日志
     */
    @Column(name = "`agent_info`")
    @ApiModelProperty("agent日志")
    private String agentInfo;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", agentId=").append(agentId);
        sb.append(", appName=").append(appName);
        sb.append(", ip=").append(ip);
        sb.append(", port=").append(port);
        sb.append(", userAppKey=").append(userAppKey);
        sb.append(", agentTimestamp=").append(agentTimestamp);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", agentInfo=").append(agentInfo);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}