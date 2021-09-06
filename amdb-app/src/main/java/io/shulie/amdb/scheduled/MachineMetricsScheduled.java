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

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.mapper.AppInstanceMapper;
import io.shulie.amdb.mapper.AppMapper;
import io.shulie.amdb.service.ConfigService;
import io.shulie.amdb.service.TaskSenderService;
import io.shulie.amdb.utils.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
@Slf4j
public class MachineMetricsScheduled {

    @Resource
    private AppMapper appMapper;
    @Resource
    AppInstanceMapper appInstanceMapper;

    @Autowired
    ConfigService configService;

    @Autowired
    TaskSenderService taskSenderService;

    @Autowired
    ZookeeperUtils zookeeperUtils;

    private long samplingInterval;

    private Integer pointEveryday;

    private String promethusServer;

    @Scheduled(cron = "0 0 0 * * ? ")
    public void run() {
        samplingInterval = NumberUtils.toLong(configService.getConfigValue("DEFAULT", "samplingInterval"), 0L);
        pointEveryday = NumberUtils.toInt(configService.getConfigValue("DEFAULT", "pointEveryday"), 0);
        promethusServer = configService.getConfigValue("DEFAULT", "promethusServer");
        if (StringUtils.isBlank(promethusServer) || samplingInterval == 0L || pointEveryday == 0) {
            return;
        }

        for (int page = 1; ; page++) {
            Page<AppDO> appDos = PageHelper.startPage(page, 100).doSelectPage(() ->
                    appMapper.selectAll());
            sendTask(appDos.stream().map(appDO -> appDO.getAppName()).collect(Collectors.toList()));
            if (appDos.size() < 100) {
                break;
            }
        }
    }

    public void sendTask(List<String> appNameList) {
        List<String> executorUrlList = null;
        try {
            executorUrlList = zookeeperUtils.getZkClient().getChildren("/config/pradar/task/executors");
        } catch (Exception e) {
            log.error("getListError ", e);
        }
        if (CollectionUtils.isEmpty(executorUrlList)) {
            return;
        }
        int executorIndex = 0;
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("samplingInterval", samplingInterval);
        taskParams.put("pointEveryday", pointEveryday);
        taskParams.put("promethusServer", promethusServer);
        for (String appName : appNameList) {
            List<String> ipList = getAppIpList(appName);
            if (CollectionUtils.isEmpty(ipList)) {
                continue;
            }
            taskParams.put("appName", appName);
            taskParams.put("instanceList", ipList);
            String executorUrl = getNextExecutor(executorUrlList, executorIndex++);
            for (int j = 0; j < executorUrlList.size() * 10; j++) {
                try {
                    if (taskSenderService.sendTask(executorUrl, "MachineMetricsByApp", taskParams)) {
                        // 执行成功就直接返回
                        break;
                    }
                } catch (Exception e) {
                    log.error("sendTask error ", e);
                }
                executorUrl = getNextExecutor(executorUrlList, executorIndex++);
                // 每个执行器重试10次均失败，就没必要再重试了
                if (j == executorUrlList.size() * 10) {
                    return;
                }
            }
            if (executorIndex > Integer.MAX_VALUE - executorUrlList.size() * 10) {
                executorIndex = 0;
            }
        }
    }

    private List<String> getAppIpList(String appName) {
        return appInstanceMapper.selectIpListByAppName(appName);
    }

    private String getNextExecutor(List<String> executorUrlList, int indexOri) {
        int index = indexOri % executorUrlList.size();
        return executorUrlList.get(index);
    }
}
