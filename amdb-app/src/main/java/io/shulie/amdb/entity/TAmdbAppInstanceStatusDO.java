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
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel("")
@Table(name = "`t_amdb_app_instance_status`")
public class TAmdbAppInstanceStatusDO implements Serializable {
    /**
     * 实例id
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("实例id")
    private Long id;

    /**
     * 应用名
     */
    @Column(name = "`app_name`")
    @ApiModelProperty("应用名")
    private String appName;

    /**
     * agentId
     */
    @Column(name = "`agent_id`")
    @ApiModelProperty("agentId")
    private String agentId;

    /**
     * ip
     */
    @Column(name = "`ip`")
    @ApiModelProperty("ip")
    private String ip;

    /**
     * 进程号
     */
    @Column(name = "`pid`")
    @ApiModelProperty("进程号")
    private String pid;

    /**
     * 主机名称
     */
    @Column(name = "`hostname`")
    @ApiModelProperty("主机名称")
    private String hostname;

    /**
     * Agent语言
     */
    @Column(name = "`agent_language`")
    @ApiModelProperty("Agent语言")
    private String agentLanguage;

    /**
     * Agent版本号
     */
    @Column(name = "`agent_version`")
    @ApiModelProperty("agent版本号")
    private String agentVersion;

    /**
     * 探针版本
     */
    @Column(name = "`probe_version`")
    @ApiModelProperty("探针版本")
    private String probeVersion;

    /**
     * 探针状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)
     */
    @Column(name = "`probe_status`")
    @ApiModelProperty("探针状态")
    private String probeStatus;

    /**
     * 错误码
     */
    @Column(name = "`error_code`")
    @ApiModelProperty("错误码")
    private String errorCode;

    /**
     * 错误信息
     */
    @Column(name = "`error_msg`")
    @ApiModelProperty("错误信息")
    private String errorMsg;


    /**
     * 创建时间
     */
    @Column(name = "`gmt_create`")
    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @Column(name = "`gmt_modify`")
    @ApiModelProperty("更新时间")
    private Date gmtModify;

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
        sb.append(", ip=").append(ip);
        sb.append(", pid=").append(pid);
        sb.append(", agentVersion=").append(agentVersion);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModify=").append(gmtModify);
        sb.append(", agentLanguage=").append(agentLanguage);
        sb.append(", probeVersion=").append(probeVersion);
        sb.append(", probeStatus=").append(probeStatus);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}