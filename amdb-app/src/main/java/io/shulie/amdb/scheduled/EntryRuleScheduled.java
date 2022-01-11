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
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.service.AppInstanceService;
import io.shulie.surge.data.common.utils.HttpUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sunshiyu
 * @description 每隔2分钟查询控制台获取全部在线应用的入口规则
 * @datetime 2022-01-10 下午
 */
@Slf4j
@Component
@Getter
@Setter
public class EntryRuleScheduled {

    @Autowired
    private AppInstanceService appInstanceService;

    @Value("${config.tenantConfig.host}")
    private String host;

    @Value("${config.tenantConfig.port}")
    private String port;

    @Value("${config.entryRule.url}")
    private String url;

    @Value("${config.entryRule.queryThreads}")
    private int queryThreads;

    public static Map<String, List<String>> apisList = new HashMap<>();

    /**
     * 每隔2分钟查询控制台获取全部在线应用的入口规则
     */
    @Scheduled(cron = "0 */2 * * * *")
    //@Scheduled(cron = "*/2 * * * * *")
    private void queryApiList() {
        try {
            //1.首先获取全量在线的应用
            List<TAmdbAppInstanceDO> appInstanceList = appInstanceService.selectOnlineAppList();
            //2.设置一个50(暂定20)个线程的线程池
            ExecutorService executorService = Executors.newFixedThreadPool(queryThreads, Executors.defaultThreadFactory());
            //3.提交任务到线程池,查询每个应用的入口规则
            appInstanceList.forEach(appInstance -> {
                String appName = appInstance.getAppName();
                String userAppKey = appInstance.getUserAppKey();
                String envCode = appInstance.getEnvCode();
                String key = userAppKey + "#" + envCode + "#" + appName;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        //如果缓存为空,查询tro接口
                        HashMap<String, String> requestHeaders = Maps.newHashMap();
                        requestHeaders.put("TenantAppkey", userAppKey);
                        requestHeaders.put("EnvCode", envCode);

                        HashMap<String, String> params = Maps.newHashMap();
                        params.put("appName", appName);

                        Map<String, Object> res = null;
                        try {
                            res = JSONObject.parseObject(HttpUtil.doGet(host, Integer.valueOf(port), url, requestHeaders, params), Map.class);
                        } catch (Throwable e) {
                            log.error("query apiList catch exception :{},{}", e, e.getStackTrace());
                        }
                        if (Objects.nonNull(res) && Objects.nonNull(res.get("data"))) {
                            Map<String, Object> data = (Map<String, Object>) res.get("data");
                            List<String> dataList = (List<String>) (data.get(appName));
                            if (CollectionUtils.isNotEmpty(dataList)) {
                                //每次查询都会更新对应应用的入口规则,如果没查到,则没有
                                apisList.put(key, dataList);
                            }
                        }
                    }
                });
            });
            log.info("查询入口规则成功");
        } catch (Exception e) {
            log.error("查询入口规则发生异常:{},异常堆栈", e, e.getStackTrace());
        }
    }
}

