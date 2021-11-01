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

import io.shulie.amdb.entity.TAmdbPradarLinkEdgeDO;
import io.shulie.amdb.mapper.PradarLinkEdgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author anjone
 * @date 2021/8/12
 */
@Slf4j
@Component
public class ClearEdgeScheduled {
    @Resource
    PradarLinkEdgeMapper pradarLinkEdgeMapper;

    private boolean isRun = false;
    private boolean isDel = false;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void startTask(boolean isRun,boolean isDel) {
        this.isRun = isRun;
        this.isDel = isDel;
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public String cleanEdge() {
        String taskResult;
        if(isRun) {
            int size = 0;
            //获取所有重复对边
            List<TAmdbPradarLinkEdgeDO> resultList = pradarLinkEdgeMapper.getExpiredEdge();
            for(TAmdbPradarLinkEdgeDO edgeTemp:resultList){
                //保留被最后刷新对边
                int latestId = pradarLinkEdgeMapper.getLatestEdge(edgeTemp);
                log.info(edgeTemp.getLinkId()+"/"+edgeTemp.getFromAppId()+"/"+edgeTemp.getToAppId()+"--->"+latestId);
                edgeTemp.setId(latestId);
                //清除其他过期对边
                try {
                    size += pradarLinkEdgeMapper.clearExpiredEdge(edgeTemp);
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            taskResult = sdf.format(new Date()) + ":本次清理数量("+size+")";
            log.info(taskResult);
        }else{
            taskResult= "任务未启动!";
        }
        if(isDel) {
            int size = pradarLinkEdgeMapper.deleteExpiredEdge();
            taskResult += "--|--删除("+size+")条!";
        }else{
            taskResult += "--|--删除任务未启动!";
        }
        return taskResult;
    }

    public void deleteExpiredEdge() {
        pradarLinkEdgeMapper.deleteExpiredEdge();
    }
}
