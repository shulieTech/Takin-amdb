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
@Table(name = "`t_amdb_link_node_relation`")
public class LinkNodeRelationDO implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 来源节点ID
     */
    @Column(name = "`source_id`")
    @ApiModelProperty("来源节点ID")
    private String sourceId;

    /**
     * 来源应用名
     */
    @Column(name = "`source_app_name`")
    @ApiModelProperty("来源应用名")
    private String sourceAppName;

    /**
     * 目标节点ID
     */
    @Column(name = "`target_id`")
    @ApiModelProperty("目标节点ID")
    private String targetId;

    /**
     * 目标应用名
     */
    @Column(name = "`target_app_name`")
    @ApiModelProperty("目标应用名")
    private String targetAppName;

    /**
     * 关联链路ID
     */
    @Column(name = "`link_id`")
    @ApiModelProperty("关联链路ID")
    private Long linkId;

    /**
     * 顺序
     */
    @Column(name = "`order_num`")
    @ApiModelProperty("顺序")
    private String orderNum;

    /**
     * 扩展字段，json存储
     */
    @Column(name = "`ext_info`")
    @ApiModelProperty("扩展字段，json存储")
    private String extInfo;

    /**
     * 创建人工号
     */
    @Column(name = "`creator`")
    @ApiModelProperty("创建人工号")
    private String creator;

    /**
     * 创建人姓名
     */
    @Column(name = "`creator_name`")
    @ApiModelProperty("创建人姓名")
    private String creatorName;

    /**
     * 修改人工号
     */
    @Column(name = "`modifier`")
    @ApiModelProperty("修改人工号")
    private String modifier;

    /**
     * 修改人姓名
     */
    @Column(name = "`modifier_name`")
    @ApiModelProperty("修改人姓名")
    private String modifierName;

    /**
     * 创建时间
     */
    @Column(name = "`gmt_create`")
    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "`gmt_modify`")
    @ApiModelProperty("修改时间")
    private Date gmtModify;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", sourceId=").append(sourceId);
        sb.append(", sourceAppName=").append(sourceAppName);
        sb.append(", targetId=").append(targetId);
        sb.append(", targetAppName=").append(targetAppName);
        sb.append(", linkId=").append(linkId);
        sb.append(", orderNum=").append(orderNum);
        sb.append(", extInfo=").append(extInfo);
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