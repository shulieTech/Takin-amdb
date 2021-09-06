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

package io.shulie.amdb.convert;

import io.shulie.amdb.entity.TAmdbAppServer;
import io.shulie.amdb.request.query.AppServerQueryRequest;
import io.shulie.amdb.response.app.AppServerResponse;
import org.springframework.beans.BeanUtils;

public class AppServerConvert {
    /**
     * 转换DO
     *
     * @param appServerQueryRequest
     * @return
     */
    public static TAmdbAppServer convertTamdbAppServer(AppServerQueryRequest appServerQueryRequest) {
        final TAmdbAppServer appServer = new TAmdbAppServer();
        BeanUtils.copyProperties(appServerQueryRequest, appServer);
        return appServer;
    }

    /**
     * 转换DTO
     *
     * @param tmpDO
     * @return
     */
    public static AppServerResponse convertAppServerResponse(TAmdbAppServer tmpDO) {
        final AppServerResponse tmpDTO = new AppServerResponse();
        BeanUtils.copyProperties(tmpDO, tmpDTO);
        return tmpDTO;
    }
}
