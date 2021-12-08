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
import java.util.Date;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel("")
@Table(name = "`t_amdb_app_instance_status`")
public class TAmdbAppInstanceStatusDO extends BaseDatabaseDO {
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
     * Agent 语言
     */
    @Column(name = "`agent_language`")
    @ApiModelProperty("Agent 语言")
    private String agentLanguage;

    /**
     * Agent 版本号
     */
    @Column(name = "`agent_version`")
    @ApiModelProperty("Agent 版本号")
    private String agentVersion;

    /**
     * agent 状态
     */
    @Column(name = "`agent_status`")
    @ApiModelProperty("agent 状态")
    private String agentStatus;

    /**
     * jdk版本号
     */
    @Column(name = "`jdk`")
    @ApiModelProperty("jdk版本号")
    private String jdk;

    /**
     * jdk版本号
     */
    @Column(name = "`jvm_args`")
    @ApiModelProperty("jdk版本号")
    private String jvmArgs;

    /**
     * agent异常日志
     */
    @Column(name = "`agent_error_msg`")
    @ApiModelProperty("agent异常日志")
    private String agentErrorMsg;

    /**
     * agnet异常code
     */
    @Column(name = "`agent_error_code`")
    @ApiModelProperty("agnet异常code")
    private String agentErrorCode;

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
    @ApiModelProperty("探针状态(0-已安装,1-未安装,2-安装中,3-卸载中,4-安装失败,5-卸载失败,99-未知状态)")
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
        sb.append(", hostname=").append(hostname);
        sb.append(", agentLanguage=").append(agentLanguage);
        sb.append(", agentVersion=").append(agentVersion);
        sb.append(", agentStatus=").append(agentStatus);
        sb.append(", jdk=").append(jdk);
        sb.append(", jvmArgs=").append(jvmArgs);
        sb.append(", agentErrorMsg=").append(agentErrorMsg);
        sb.append(", agentErrorCode=").append(agentErrorCode);
        sb.append(", probeVersion=").append(probeVersion);
        sb.append(", probeStatus=").append(probeStatus);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModify=").append(gmtModify);
        sb.append(", envCode=").append(getEnvCode());
        sb.append(", userId=").append(getUserId());
        sb.append(", userAppKey=").append(getUserAppKey());
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}