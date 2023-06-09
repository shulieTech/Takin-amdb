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
@Table(name = "`t_amdb_data_script`")
public class ScriptDO extends BaseDatabaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Id
    @Column(name = "`id`")
    @ApiModelProperty("数据清洗脚本 ID")
    private Long id;

    /**
     * 应用名称
     */
    @Column(name = "`script_code`")
    @ApiModelProperty("脚本编码")
    private String scriptCode;

    /**
     * 应用负责人
     */
    @Column(name = "`script_name`")
    @ApiModelProperty("脚本名称")
    private String scriptName;

    /**
     * 工程名称
     */
    @Column(name = "`script`")
    @ApiModelProperty("脚本")
    private String script;

    /**
     * 租户标示
     */
    @Column(name = "`tenant`")
    @ApiModelProperty("租户标示")
    private Integer tenant;

    /**
     * 应用类型
     */
    @Column(name = "`script_type`")
    @ApiModelProperty("脚本类型")
    private String scriptType;


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
}
