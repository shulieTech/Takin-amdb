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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author sunshiyu
 * @description agentinfo定时(每隔5h)日志清理功能(清理7天前日志, 时间可配置)
 * @datetime 2021-08-26 12:11 下午
 */
@Slf4j
@Component
public class AgentLogClearScheduled {

    @Value("${config.agentlog.reserveDays}")
    private int reserveDays;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 每隔5小时清理指定时间段之前的数据
     */
    @Scheduled(cron = "0 0 */5 * * *")
    //@Scheduled(cron = "*/5 * * * * *")
    public void cleanInfo() {
        log.info("定时清除Agent错误日志数据");
        try {
            //获取数据库时间,防止和现实时间不一致
            Long time = jdbcTemplate.queryForObject("select max(agent_timestamp) as time from t_amdb_agent_info", Long.class);
            //如果表里没数据,不执行删除
            if (time == null) {
                return;
            }
            long cleanTime = time - reserveDays * 24L * 60 * 60 * 1000;
            String sql = "delete from t_amdb_agent_info where agent_timestamp < " + cleanTime;
            jdbcTemplate.execute(sql);
            log.info("已清理{}天前agentlog,清理sql:{}", reserveDays, sql);
        } catch (Exception e) {
            log.error("清理{}天前agentlog数据失败{},异常堆栈:{}", reserveDays, e, e.getStackTrace());
        }
    }
}

