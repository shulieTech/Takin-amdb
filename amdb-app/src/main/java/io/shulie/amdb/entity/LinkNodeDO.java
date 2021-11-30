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
@Table(name = "`t_amdb_link_node`")
public class LinkNodeDO extends BaseDatabaseDO {
    /**
     * 主键
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 节点ID(链路ID+应用+类型+入口)
     */
    @Column(name = "`node_id`")
    @ApiModelProperty("节点ID(链路ID+应用+类型+入口)")
    private String nodeId;

    /**
     * 链路主键
     */
    @Column(name = "`link_id`")
    @ApiModelProperty("链路主键")
    private Long linkId;

    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 节点名称
     */
    @Column(name = "`node_name`")
    @ApiModelProperty("节点名称")
    private String nodeName;

    /**
     * 是否为根节点(0是/1否)
     */
    @Column(name = "`parent`")
    @ApiModelProperty("是否为根节点(0是/1否)")
    private Boolean parent;

    /**
     * 应用入口
     */
    @Column(name = "`entrance`")
    @ApiModelProperty("应用入口")
    private String entrance;

    /**
     * 入口类型(http/rocketmq/rabbitmq/kafka)
     */
    @Column(name = "`entrance_type`")
    @ApiModelProperty("入口类型(http/rocketmq/rabbitmq/kafka)")
    private String entranceType;

    /**
     * 节点所属层级
     */
    @Column(name = "`node_level`")
    @ApiModelProperty("节点层级")
    private Integer nodeLevel;

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
        sb.append(", nodeId=").append(nodeId);
        sb.append(", linkId=").append(linkId);
        sb.append(", appName=").append(appName);
        sb.append(", nodeName=").append(nodeName);
        sb.append(", parent=").append(parent);
        sb.append(", entrance=").append(entrance);
        sb.append(", entranceType=").append(entranceType);
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