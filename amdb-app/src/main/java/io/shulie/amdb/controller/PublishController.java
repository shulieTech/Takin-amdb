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

package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.TAmdbPublishInfo;
import io.shulie.amdb.service.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("amdb/db/api/publish")
/**
 * 应用部署信息记录对接发布平台
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class PublishController {
    @Autowired
    private PublishService publishService;

    @RequestMapping("/insert")
    public Response insert(@RequestBody TAmdbPublishInfo tAmdbApp) {
        return Response.success(publishService.insert(tAmdbApp));
    }

    @RequestMapping("/insertBatch")
    public Response insertBatch(List<TAmdbPublishInfo> tAmdbApp) {
        return Response.success(publishService.insertBatch(tAmdbApp));
    }

    @RequestMapping("/update")
    public Response update(TAmdbPublishInfo tAmdbApp) {
        return Response.success(publishService.update(tAmdbApp));
    }

    @RequestMapping("/updateBatch")
    public Response updateBatch(List<TAmdbPublishInfo> tAmdbApp) {
        return Response.success(publishService.updateBatch(tAmdbApp));
    }

    @RequestMapping("/delete")
    public Response delete(TAmdbPublishInfo tAmdbApp) {
        return Response.success(publishService.delete(tAmdbApp));
    }

    @RequestMapping("/select")
    public Response select(TAmdbPublishInfo tAmdbApp) {
        return Response.success(publishService.select(tAmdbApp));
    }
}
