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

package io.shulie.amdb.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.shulie.surge.data.common.utils.HttpUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author sunshiyu
 * @description 每隔10分钟查询无涯提供全量租户配置接口
 * @datetime 2021-11-24 下午
 */
@Slf4j
@Component
@Getter
@Setter
public class TenantConfigScheduled implements ApplicationContextAware {

    @Value("${config.tenantConfig.host}")
    private String host;

    @Value("${config.tenantConfig.port}")
    private String port;

    @Value("${config.tenantConfig.url}")
    private String tenantConfigUrl;

    @Value("${config.tenantConfig.defaultTenantAppKey}")
    private String defaultTenantAppKey;

    private static GenericApplicationContext applicationContext;

    private static Map<String, String> tenantConfigMap = new HashMap<>();

    /**
     * 每隔5min清理指定时间段之前的数据
     */
    @Scheduled(cron = "0 */5 * * * *")
    //@Scheduled(cron = "*/5 * * * * *")
    private void queryTenantConfig() {
        //重复应用列表
        Set<String> repeatAppList = new HashSet<>();
        //唯一应用列表
        Set<String> uniqueAppList = new HashSet<>();
        Map<String, Object> res = null;
        try {
            res = JSONObject.parseObject(HttpUtil.doGet(host, Integer.valueOf(port), tenantConfigUrl, null, null), Map.class);
            if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
                Object data = res.get("data");
                List<Map<String, Object>> tenantConfigList = (List<Map<String, Object>>) data;
                if (CollectionUtils.isNotEmpty(tenantConfigList)) {
                    tenantConfigList.forEach((tenantConfig) -> {
                        if (!tenantConfig.containsKey("tenantAppKey") || !tenantConfig.containsKey("envAppMap")) {
                            return;
                        }
                        String tenantAppKey = (String) tenantConfig.get("tenantAppKey");
                        Map<String, Object> envAppMap = (Map<String, Object>) tenantConfig.get("envAppMap");
                        if (MapUtils.isNotEmpty(envAppMap)) {
                            envAppMap.forEach((k, v) -> {
                                List<String> appList = (List<String>) v;
                                if (CollectionUtils.isNotEmpty(appList)) {
                                    appList.forEach((appName) -> {
                                        if (uniqueAppList.contains(appName)) {
                                            repeatAppList.add(appName + "#" + tenantAppKey);
                                        } else {
                                            uniqueAppList.add(appName);
                                        }
                                        tenantConfigMap.put(appName, tenantAppKey + "#" + k);
                                    });
                                }
                            });
                        }
                    });
                }
                //如果不同环境应用名相同,默认test环境,不考虑不同租户同名应用的情况
                repeatAppList.forEach((app) -> {
                    tenantConfigMap.put(app.split("#")[0], app.split("#")[1] + "#test");
                });
            }
        } catch (Exception e) {
            log.error("query tenant config catch exception:{},{}", e, e.getStackTrace());
        }
    }

    public static Map<String, String> getTenantConfigByAppName(String appName) {
        Map<String, String> config = Maps.newHashMap();
        //首次启动应用,如果租户配置为空,需要手动触发一次查询 || 新的老探针应用接入时,此时缓存中没有该应用配置,也需要触发一次查询
        ConfigurableEnvironment contextEnvironment = applicationContext.getEnvironment();
        if (tenantConfigMap.isEmpty() || !tenantConfigMap.containsKey(appName)) {
            TenantConfigScheduled tenantConfigScheduled = new TenantConfigScheduled();
            tenantConfigScheduled.setHost(contextEnvironment.getProperty("config.tenantConfig.host"));
            tenantConfigScheduled.setPort(contextEnvironment.getProperty("config.tenantConfig.port"));
            tenantConfigScheduled.setTenantConfigUrl(contextEnvironment.getProperty("config.tenantConfig.url"));
            tenantConfigScheduled.queryTenantConfig();
            log.info("应用启动或新应用接入:{},获取配置{}", appName, tenantConfigMap.get(appName));
        }
        if (tenantConfigMap.containsKey(appName)) {
            config.put("tenantAppKey", tenantConfigMap.get(appName).split("#")[0]);
            config.put("envCode", tenantConfigMap.get(appName).split("#")[1]);
        } else {
            config.put("tenantAppKey", StringUtils.defaultString(contextEnvironment.getProperty("config.tenantConfig.defaultTenantAppKey"), "default"));
            config.put("envCode", "test");
        }
        return config;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TenantConfigScheduled.applicationContext = (GenericApplicationContext) applicationContext;
    }
}

