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

import java.util.List;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.ConfigurationDO;
import io.shulie.amdb.request.submit.ConfigurationSubmitRequest;
import io.shulie.amdb.response.app.model.Configuration;
import io.shulie.amdb.response.app.model.ConfigurationKey;

public interface ConfigurationService {

    Response<String> addOrUpdateConfiguration(ConfigurationSubmitRequest request);

    void deleteConfiguration(String number);

    void enableConfiguration(String number);

    void disableConfiguration(String number);

    List<Configuration> query(ConfigurationDO record);

    List<Configuration> query(ConfigurationDO record, boolean returnItems);

    List<ConfigurationKey> queryKeys(String type);
}
