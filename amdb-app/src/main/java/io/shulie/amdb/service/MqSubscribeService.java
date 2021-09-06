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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.request.submit.MqSubscribeAddSubmitRequest;
import io.shulie.amdb.request.submit.MqSubscribeDeleteRequest;
import io.shulie.amdb.request.submit.MqSubscribeUpdateSubmitRequest;

public interface MqSubscribeService {
    /**
     * 新增MQ订阅
     *
     * @param addSubmitRequest
     * @return
     */
    Response addMqSubscribe(MqSubscribeAddSubmitRequest addSubmitRequest);

    /**
     * 更新MQ订阅
     *
     * @param updateSubmitRequest
     * @return
     */
    Response updateMqSubscribe(MqSubscribeUpdateSubmitRequest updateSubmitRequest);

    /**
     * 移除MQ订阅
     *
     * @param deleteRequest
     * @return
     */
    Response removeMqSubscribe(MqSubscribeDeleteRequest deleteRequest);
}
