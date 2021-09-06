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

package io.shulie.amdb.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
public class LinkNodeDTO implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 节点ID(前端拼接)
     */
    @Column(name = "`node_id`")
    @ApiModelProperty("节点ID(前端拼接)")
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
     * 图标(base64存储)
     */
    @Column(name = "`icon`")
    @ApiModelProperty("图标(base64存储)")
    private String icon;

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

    private String nodeType;

    public int hashCode() {
        return nodeId.hashCode();
    }

    public boolean equals(Object linkNodeDTO) {
        if (linkNodeDTO == null || (!(linkNodeDTO instanceof LinkNodeDTO))) {
            return false;
        }
        return this.nodeId.equals(((LinkNodeDTO) linkNodeDTO).getNodeId());
    }
}