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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.scheduled.ClearEdgeScheduled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * api for test
 */
@Slf4j
@RestController
@RequestMapping("/amdb/scheduler")
public class SchedulerController {
    @Autowired
    ClearEdgeScheduled clearEdgeScheduled;

    /**
     * 逻辑删除由于边ID计算方法升级导致对重复边
     * 把数据关系破坏，linkid增加_bak
     * @param startFlag
     * @return
     */
    @RequestMapping(path = "/cheanEdge", method = RequestMethod.GET)
    public Response chearEdge(@RequestParam(value = "startFlag",defaultValue = "no")String startFlag,
                              @RequestParam(value = "delFlag",defaultValue = "no")String delFlag) {
        try {
            boolean isRun = "yes".equals(startFlag)?true:false;
            boolean isDel = "yes".equals(delFlag)?true:false;
            clearEdgeScheduled.startTask(isRun,isDel);
            String msg = clearEdgeScheduled.cleanEdge();
            return Response.success(msg);
        } catch (Exception e) {
            log.error("chearedge fail", e);
            return Response.fail();
        }
    }

    /**
     * 物理清楚过期边数据
     * @return
     */
    @RequestMapping(path = "/deleteEdge", method = RequestMethod.GET)
    public Response deleteedge() {
        try {
            clearEdgeScheduled.deleteExpiredEdge();
            return Response.emptySuccess();
        } catch (Exception e) {
            log.error("deleteedge fail", e);
            return Response.fail();
        }
    }
}
