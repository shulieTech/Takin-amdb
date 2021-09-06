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

package io.shulie.amdb.common.dto.link.topology;

import lombok.Data;

@Data
public class LinkEdgeDTO {
    String sourceId;
    String targetId;
    String eagleId;
    String eagleType;
    String eagleTypeGroup;
    String serverAppName;
    //Object extendInfo;
    //服务
    String service;
    //方法
    String method;
    //扩展信息
    String extend;
    //应用名称
    String appName;
    //RPC类型
    String rpcType;
    //日志类型
    String logType;
    //中间件名称
    String middlewareName;
}
