/*
package io.shulie.amdb.scheduled;

import io.shulie.amdb.entity.TAMDBPradarLinkConfigDO;
import io.shulie.amdb.mapper.PradarLinkConfigMapper;
import io.shulie.amdb.service.LinkUnKnowService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

*/
/**
 * @Author: xingchen
 * @ClassName: UnKnowNodeSchedule
 * @Package: io.shulie.amdb.scheduled
 * @Date: 2021/1/2010:08
 * @Description:
 *//*

@Service
@Order(3)
public class UnKnowNodeSchedule implements ApplicationRunner {
    @Value("${config.link.processUnknow:true}")
    private boolean processUnknow;

    private static final int period = 2 * 60;

    private static final int initialDelay = 30;

    @Autowired
    private PradarLinkConfigMapper pradarLinkConfigMapper;

    @Autowired
    private LinkUnKnowService linkUnKnowService;

    */
/**
     * 判断未知节点
     *
     * @param args
     *//*

    @Override
    public void run(ApplicationArguments args) {
        if (processUnknow) {
            ScheduledExecutorService nodeService = Executors.newScheduledThreadPool(1);
            ScheduledExecutorService mqService = Executors.newScheduledThreadPool(1);
            ScheduledExecutorService cleanService = Executors.newScheduledThreadPool(1);
            nodeService.scheduleAtFixedRate(() -> {
                // 读取所有的配置
                List<TAMDBPradarLinkConfigDO> nodeConfigList = pradarLinkConfigMapper.selectAll();
                if (CollectionUtils.isNotEmpty(nodeConfigList)) {
                    nodeConfigList.stream().forEach(config -> {
                        CompletableFuture.runAsync(() -> linkUnKnowService.processUnKnowNodeCommon(config));
                    });
                }
            }, initialDelay, period, TimeUnit.SECONDS);

            mqService.scheduleAtFixedRate(() -> {
                // 读取所有的配置
                List<TAMDBPradarLinkConfigDO> mqConfigList = pradarLinkConfigMapper.selectAll();
                if (CollectionUtils.isNotEmpty(mqConfigList)) {
                    mqConfigList.stream().forEach(config -> {
                        CompletableFuture.runAsync(() -> linkUnKnowService.processUnKnowNodeMQ(config));
                    });
                }
            }, initialDelay, period, TimeUnit.SECONDS);

            // 清除未知节点
            cleanService.scheduleAtFixedRate(() -> {
                linkUnKnowService.clearUnknownNode();
            }, initialDelay, period, TimeUnit.SECONDS);
        }
    }
}
*/
