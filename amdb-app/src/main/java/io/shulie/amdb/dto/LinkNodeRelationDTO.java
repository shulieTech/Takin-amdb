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
public class LinkNodeRelationDTO implements Serializable {
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

    private String eagleId;

    private String targetAppType;

    public int hashCode() {
        return eagleId.hashCode();
    }

    public boolean equals(Object relationDTO) {
        if (relationDTO == null || (!(relationDTO instanceof LinkNodeRelationDTO))) {
            return false;
        }
        return this.eagleId.equals(((LinkNodeRelationDTO) relationDTO).getEagleId());
    }
}