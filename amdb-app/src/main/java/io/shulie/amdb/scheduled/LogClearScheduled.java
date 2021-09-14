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

import io.shulie.amdb.dao.ITraceDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author anjone
 * @date 2021/8/12
 */
@Slf4j
@Component
public class LogClearScheduled {
    @Resource(name = "traceDaoImpl")
    private ITraceDao traceDao;

    @Value("${datasource.traceAll}")
    private String traceAll;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanInfo() {
        if ("mysql".equalsIgnoreCase(traceAll)) {
            log.info("定时清除链路日志数据");
            Date date = traceDao.queryForObject("select now()", Date.class);
            long threeDayAgo = date.getTime() - 3 * 24 * 60 * 60 * 1000;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String format = simpleDateFormat.format(new Date(threeDayAgo));
            traceDao.execute("delete from t_trace_all where startDate < '" + format + "'");
        }
    }
}
