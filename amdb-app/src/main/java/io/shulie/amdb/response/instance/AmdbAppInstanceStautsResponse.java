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

package io.shulie.amdb.response.instance;

import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;

@Data
public class AmdbAppInstanceStautsResponse implements Serializable {
    // 应用名称
    String appName;
    // 用来判断实例的唯一性
    String agentId;
    // agent 版本
    String agentVersion;
    //agent状态
    private String agentStatus;
    // 更新时间
    String agentUpdateTime;
    // 进程号 id
    String progressId;
    // ip 地址
    String ipAddress;
    // agent 支持的语言
    String agentLanguage;
    // hostname
    String hostname;
    // 探针Version
    String probeVersion;
    // 探针beStatus
    String probeStatus;
    /**
     * 错误信息
     */
    private String errorMsg;
    // 探针错误信息
    private String agentErrorMsg;

    public AmdbAppInstanceStautsResponse() {

    }

    public AmdbAppInstanceStautsResponse(TAmdbAppInstanceStatusDO amdbAppInstanceStatusDO) {
        this.appName = amdbAppInstanceStatusDO.getAppName();
        this.agentId = amdbAppInstanceStatusDO.getAgentId();
        this.agentVersion = amdbAppInstanceStatusDO.getAgentVersion();
        this.agentStatus = amdbAppInstanceStatusDO.getAgentStatus();
        this.agentUpdateTime = DateFormatUtils.format(amdbAppInstanceStatusDO.getGmtModify(), "yyyy-MM-dd HH:mm:ss");
        this.progressId = amdbAppInstanceStatusDO.getPid();
        this.ipAddress = amdbAppInstanceStatusDO.getIp();
        this.agentLanguage = amdbAppInstanceStatusDO.getAgentLanguage();
        this.hostname = amdbAppInstanceStatusDO.getHostname();
        this.errorMsg = amdbAppInstanceStatusDO.getErrorMsg();
        //2021-05-28
        this.probeVersion = amdbAppInstanceStatusDO.getProbeVersion();
        this.probeStatus = amdbAppInstanceStatusDO.getProbeStatus();
        this.agentErrorMsg = amdbAppInstanceStatusDO.getAgentErrorMsg();
    }
}