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

package io.shulie.amdb.common.dto.instance;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author anjone
 * @date 2021/8/17
 */
@Data
public class AppInstanceExtDTO implements Serializable {
    // 该字段表示探针的配置文件配置信息，是一个hashmap的json字符串
    private Map<String, String> simulatorConfigs;
    //模块加载结果 ture 加载成功
    private Boolean moduleLoadResult;
    //模块加载详情  json list
    private List<ModuleLoadDetailDTO> moduleLoadDetail;
    //异常信息
    private String errorMsgInfos;
    //主机名
    private String host;
    //GC类型
    private String gcType;
    //启动时间
    private long startTime;
    //JDK版本
    private String jdkVersion;
}
