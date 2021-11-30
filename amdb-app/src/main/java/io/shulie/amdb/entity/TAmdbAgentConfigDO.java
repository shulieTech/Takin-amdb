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
@Table(name = "`t_amdb_agent_config`")
public class TAmdbAgentConfigDO extends BaseDatabaseDO {
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("")
    private Long id;

    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * agent id
     */
    @Column(name = "`agent_id`")
    @ApiModelProperty("agent id")
    private String agentId;

    /**
     * 配置key
     */
    @Column(name = "`config_key`")
    @ApiModelProperty("配置key")
    private String configKey;

    /**
     * 配置value
     */
    @Column(name = "`config_value`")
    @ApiModelProperty("配置value")
    private String configValue;

    /**
     * 配置校验结果 ture 校验成功 false校验失败
     */
    @Column(name = "`status`")
    @ApiModelProperty("配置校验结果 ture 校验成功 false校验失败")
    private Boolean status;

    /**
     * 创建时间
     */
    @Column(name = "`gmt_create`")
    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appName=").append(appName);
        sb.append(", agentId=").append(agentId);
        sb.append(", configKey=").append(configKey);
        sb.append(", configValue=").append(configValue);
        sb.append(", status=").append(status);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}