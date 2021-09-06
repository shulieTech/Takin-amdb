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

import javax.persistence.Table;

/**
 * t_amdb_publish_info
 * @author 
 */
@Data
@ToString
@EqualsAndHashCode
@ApiModel("")
@Table(name = "`t_amdb_publish_info`")
public class TAmdbPublishInfo implements Serializable {
    /**
     * 发布ID
     */
    private Long id;

    /**
     * 发布人
     */
    private String publisher;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 发布服务器
     */
    private String publishServer;

    /**
     * 发布应用
     */
    private String publishApp;

    /**
     * 发布环境
     */
    private String publishEnv;

    /**
     * 发布版本
     */
    private String publishVersion;

    /**
     * 扩展字段
     */
    private String ext;

    /**
     * 标记位
     */
    private Integer flag;

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