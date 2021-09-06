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
import java.io.Serializable;
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
@Table(name = "`t_amdb_app_server_metrics`")
public class AppServerMetricsDO implements Serializable {
    /**
     * ID
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("ID")
    private Long id;

    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 服务名
     */
    @Column(name = "`server_name`")
    @ApiModelProperty("服务名")
    private String serverName;

    /**
     * 服务类型
     */
    @Column(name = "`server_type`")
    @ApiModelProperty("服务类型")
    private String serverType;

    /**
     * rt
     */
    @Column(name = "`rt`")
    @ApiModelProperty("rt")
    private Double rt;

    /**
     * qps
     */
    @Column(name = "`qps`")
    @ApiModelProperty("qps")
    private Double qps;

    /**
     * 成功率
     */
    @Column(name = "`success_rate`")
    @ApiModelProperty("成功率")
    private Double successRate;

    /**
     * P90/P95/P99
     */
    @Column(name = "`p_type`")
    @ApiModelProperty("P90/P95/P99")
    private String pType;

    /**
     * 采样时间间隔
     */
    @Column(name = "`sampling_interval`")
    @ApiModelProperty("采样时间间隔")
    private Integer samplingInterval;

    /**
     * 统计开始时间
     */
    @Column(name = "`statistics_start`")
    @ApiModelProperty("统计开始时间")
    private Date statisticsStart;

    /**
     * 统计结束时间
     */
    @Column(name = "`statistics_end`")
    @ApiModelProperty("统计结束时间")
    private Date statisticsEnd;

    /**
     * 标记位
     */
    @Column(name = "`flag`")
    @ApiModelProperty("标记位")
    private Integer flag;

    /**
     * 创建人编码
     */
    @Column(name = "`creator`")
    @ApiModelProperty("创建人编码")
    private String creator;

    /**
     * 创建人名称
     */
    @Column(name = "`creator_name`")
    @ApiModelProperty("创建人名称")
    private String creatorName;

    /**
     * 更新人编码
     */
    @Column(name = "`modifier`")
    @ApiModelProperty("更新人编码")
    private String modifier;

    /**
     * 更新人名称
     */
    @Column(name = "`modifier_name`")
    @ApiModelProperty("更新人名称")
    private String modifierName;

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
        sb.append(", serverName=").append(serverName);
        sb.append(", serverType=").append(serverType);
        sb.append(", rt=").append(rt);
        sb.append(", qps=").append(qps);
        sb.append(", successRate=").append(successRate);
        sb.append(", pType=").append(pType);
        sb.append(", samplingInterval=").append(samplingInterval);
        sb.append(", statisticsStart=").append(statisticsStart);
        sb.append(", statisticsEnd=").append(statisticsEnd);
        sb.append(", flag=").append(flag);
        sb.append(", creator=").append(creator);
        sb.append(", creatorName=").append(creatorName);
        sb.append(", modifier=").append(modifier);
        sb.append(", modifierName=").append(modifierName);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModify=").append(gmtModify);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}