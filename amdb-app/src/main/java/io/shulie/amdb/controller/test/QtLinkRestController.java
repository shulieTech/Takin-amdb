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

package io.shulie.amdb.controller.test;

import io.shulie.amdb.adaptors.starter.ClientAdaptorStarter;
import io.shulie.amdb.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * api for test
 */
@Slf4j
@RestController
@RequestMapping("/amdb/qtLink")
public class QtLinkRestController {
    @Autowired
    ClientAdaptorStarter adaptorStarter;

    @RequestMapping(path = "/restartAdaptor", method = RequestMethod.POST)
    public Response restartAdaptor() {
        try {
            adaptorStarter.restart();
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("restart fail", e);
            return Response.fail();
        }
    }
}
