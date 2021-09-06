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
import io.shulie.amdb.entity.TAmdbMapperSqlInfo;
import io.shulie.amdb.service.AmdbMapperSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("amdb/db/api/mapperSql")
/**
 * sonar sql扫描
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class AMDBMapperSqlController {

    @Autowired
    AmdbMapperSqlService amdbMapperSqlService;

    @RequestMapping("/insert")
    @ResponseBody
    public Response insert(TAmdbMapperSqlInfo tAmdbApp) {
        return Response.success(amdbMapperSqlService.insert(tAmdbApp));
    }

    @PostMapping("/insertBatch")
    @ResponseBody
    public Response insertBatch(@RequestBody List<TAmdbMapperSqlInfo> tAmdbApp) {
        return Response.success(amdbMapperSqlService.insertBatch(tAmdbApp));
    }

    @RequestMapping("/update")
    @ResponseBody
    public Response update(TAmdbMapperSqlInfo tAmdbApp) {
        return Response.success(amdbMapperSqlService.update(tAmdbApp));
    }

    @PostMapping("/updateBatch")
    @ResponseBody
    public Response updateBatch(@RequestBody List<TAmdbMapperSqlInfo> tAmdbApp) {
        return Response.success(amdbMapperSqlService.updateBatch(tAmdbApp));
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Response delete(TAmdbMapperSqlInfo tAmdbApp) {
        return Response.success(amdbMapperSqlService.delete(tAmdbApp));
    }

    @RequestMapping("/selectByPrimaryKey")
    @ResponseBody
    public Response selectByPrimaryKey(TAmdbMapperSqlInfo tAmdbApp) {
        return Response.success(amdbMapperSqlService.selectByPrimaryKey(tAmdbApp));
    }

    @RequestMapping("/selectList")
    @ResponseBody
    public Response selectList(TAmdbMapperSqlInfo tAmdbApp) {
        return Response.success(amdbMapperSqlService.selectList(tAmdbApp));
    }

    @RequestMapping("/selectByFilter")
    @ResponseBody
    public Response selectByFilter(String filter, Integer page, Integer pageSize) {
        return Response.success(amdbMapperSqlService.selectByFilter(filter, page, pageSize));
    }
}
