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

package io.shulie.amdb.service;

import org.springframework.stereotype.Component;

/**
 * @Author: xingchen
 * @ClassName: TimeService
 * @Package: com.pamirs.smon.service.impl
 * @Date: 2020/10/2114:03
 * @Description:
 */
@Component
public class TimeService {
    private static int DEFAULT_DELAY_TIME = 2 * 60 + 30;

    public long getSystemTime(long time) {
        return System.currentTimeMillis() - time * 1000;
    }

    public long getDefaultDelayTime() {
        return System.currentTimeMillis() - DEFAULT_DELAY_TIME * 1000;
    }
}