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

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * t_amdb_mapper_sql_info
 * @author 
 */
@Data
@ToString
@EqualsAndHashCode
@ApiModel("")
@Table(name = "`t_amdb_mapper_sql_info`")
public class TAmdbMapperSqlInfo implements Serializable {
    /**
     * ID
     */
    @Id
    private Long id;

    /**
     * sqlID
     */
    private String sqlId;

    /**
     * 完整sql
     */
    private String sql;

    /**
     * mapper路径
     */
    private String mapperPath;

    /**
     * 所属应用
     */
    private String belongsApp;

    /**
     * 环境
     */
    private String env;

    /**
     * 应用版本
     */
    private String publishPackageName;

    /**
     * 上报时间
     */
    private Date reportTime;

    /**
     * 扫描时间
     */
    private Date scanTime;

    /**
     * 扩展字段
     */
    private String ext;

    /**
     * 标记位
     */
    private Integer flag;

    /**
     * 代码分支
     */
    private String branch;

    /**
     * 所在行数
     */
    private Integer line;

    /**
     * 创建人编码
     */
    private String creator;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 更新人编码
     */
    private String modifier;

    /**
     * 更新人名称
     */
    private String modifierName;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModify;

    private static final long serialVersionUID = 1L;
}