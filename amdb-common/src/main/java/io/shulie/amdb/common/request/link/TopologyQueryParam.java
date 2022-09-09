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

package io.shulie.amdb.common.request.link;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class TopologyQueryParam extends AbstractAmdbBaseRequest {
    String appName;
    String linkId;
    String serviceName;
    String method;
    String rpcType;
    String extend;
    Boolean isTrace;
    private String id;
    private Boolean extFlag;

    public boolean isTrace() {
        if (StringUtils.isEmpty(isTrace)) {
            isTrace = true;
        }
        return isTrace;
    }

    // 默认没有
    public boolean isExtFlag() {
        if (StringUtils.isEmpty(extFlag)) {
            extFlag = false;
        }
        return extFlag;
    }
}
