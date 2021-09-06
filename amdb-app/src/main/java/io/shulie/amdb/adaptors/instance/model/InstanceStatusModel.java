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

package io.shulie.amdb.adaptors.instance.model;


import io.shulie.amdb.adaptors.AdaptorModel;
import lombok.Data;

/**
 * 实例状态模型
 *
 * @author vincent
 */
@Data
public class InstanceStatusModel implements AdaptorModel {

    //agent编号
    private String agentId;
    //地址
    private String address;
    //主机名
    private String host;
    //名称
    private String name;
    //agent版本
    private String agentVersion;
    //agent语言
    private String agentLanguage;
    //进程编号
    private String pid;
    //错误编码
    private String errorCode;
    //错误信息
    private String errorMsg;
    //探针版本
    private String simulatorVersion;
    //探针状态
    private String agentStatus;
}

