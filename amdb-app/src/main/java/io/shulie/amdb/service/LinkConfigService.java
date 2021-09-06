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
import io.shulie.amdb.common.request.link.CalculateParam;
import io.shulie.amdb.entity.TAMDBPradarLinkConfigDO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface LinkConfigService {

    Response<String> buildLinkConfig(CalculateParam calculateParam);

    boolean isLinkConfigOpen(CalculateParam calculateParam);

    List<TAMDBPradarLinkConfigDO> selectAll();

    Response<String> deleteLinkConfig(CalculateParam calculateParam);
}
