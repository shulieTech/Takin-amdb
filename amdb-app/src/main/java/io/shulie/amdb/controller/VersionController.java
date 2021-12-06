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

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: xingchen
 * @ClassName: VersionController
 * @Package: io.shulie.amdb.controller
 * @Date: 2021/6/2815:47
 * @Description:
 */
@RestController
@Api(description = "amdb版本获取")
@RequestMapping(value = "/amdb")
public class VersionController {
    /**
     * 当前版本
     */
    @Value("${app.version}")
    private String serviceVersion;
    /**
     * 打包时间
     */
    @Value("${app.build.time}")
    private String serviceBuildDate;

    @RequestMapping(value = "/getVersion", method = RequestMethod.GET)
    public Map<String, String> getVersion() {
        Map<String, String> ret = Maps.newHashMap();
        ret.put("version", serviceVersion);
        ret.put("buildTime", serviceBuildDate);
        return ret;
    }

    @GetMapping(value = "/api/health")
    public String checkHealth() {
        return "success";
    }
}
