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

package io.shulie.amdb.service.impl;

import io.shulie.amdb.service.TaskSenderService;
import io.shulie.amdb.utils.HttpUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskSenderServiceImpl implements TaskSenderService {
    @Override
    public boolean sendTask(String url, String taskType, Map<String, Object> taskParam) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskType", taskType);
        params.put("taskParam", taskParam);
        HttpUtil.sendPost(url, params);
        return true;
    }
}
