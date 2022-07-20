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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

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

    private static Properties VERSION = null;

    @RequestMapping(value = "/getVersion", method = RequestMethod.GET)
    public Map<String, Object> getVersion() {
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("version", serviceVersion);
        ret.put("buildTime", serviceBuildDate);
        ret.put("gitVersion", Objects.isNull(VERSION) ? (VERSION = readGitVersion()) : VERSION);
        return ret;
    }

    @GetMapping(value = "/api/health")
    public String checkHealth() {
        return "success";
    }

    private Properties readGitVersion() {
        Properties properties = new Properties();
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        if (resource.exists()) {
            try (InputStream stream = resource.getInputStream()) {
                properties.load(stream);
            } catch (Exception ignore) {
            }
        }
        return properties;
    }
}
