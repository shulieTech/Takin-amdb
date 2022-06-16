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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.shulie.amdb.entity.PradarLinkEntranceDO;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.mapper.LinkEntranceMapper;
import io.shulie.amdb.service.AppInstanceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sunshiyu
 * @description 每隔2分钟查询控制台获取全部在线应用的入口规则
 * @datetime 2022-01-10 下午
 */
@Slf4j
@Component
@Getter
@Setter
public class EntryAlignScheduled  {

    @Autowired
    private AppInstanceService appInstanceService;

    @Resource
    private LinkEntranceMapper linkEntranceMapper;

    @Value("${config.entryRule.queryThreads}")
    private int queryThreads;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        //设置一个50(暂定20)个线程的线程池
        executorService = Executors.newFixedThreadPool(queryThreads, Executors.defaultThreadFactory());
    }

    public static Cache<String, List<String>> apisCacheLoc = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).maximumSize(10000).build();

    /**
     * 每隔5分钟查询控制台获取全部在线应用的入口规则(启动之后立马执行一次)
     */
    @Scheduled(initialDelay = 0, fixedRate = 1000*60*1)
    private void AlignApi() {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        long startTime = System.currentTimeMillis();
        log.info("对齐规则开始,批次:{}",startTime);
        try {
            ConcurrentMap<String, List<String>> locMap = apisCacheLoc.asMap();
            ConcurrentMap<String, List<String>> rmtMap = EntryRuleScheduled.apisCache.asMap();
            //System.out.println("----locMap----"+locMap);
            //System.out.println("----rmtMap---->"+rmtMap);
            //1.首先获取全量在线的应用
            List<TAmdbAppInstanceDO> appInstanceList = appInstanceService.selectOnlineAppList();
            //3.提交任务到线程池,查询每个应用的入口规则
            appInstanceList.forEach(appInstance -> {
                String appName = appInstance.getAppName();
                String userAppKey = appInstance.getUserAppKey();
                String envCode = appInstance.getEnvCode();
                String key = userAppKey + "#" + envCode + "#" + appName;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<String> locResult = locMap.get(key)==null?new ArrayList<>():locMap.get(key);
                        List<String> rmtResult = rmtMap.get(key)==null?new ArrayList<>():rmtMap.get(key);
                        //System.out.println(key+"----locResult----"+locResult);
                        //System.out.println(key+"----rmtResult---->"+rmtResult);
                        rmtResult.stream().filter(s -> !locResult.contains(s)).forEach(s2 -> {
                            //新增入口规则
                            Example example = new Example(PradarLinkEntranceDO.class);
                            Example.Criteria criteria = example.createCriteria();
                            if(s2.split("#").length==2){
                                List<Long> ids = new ArrayList<>();
                                criteria.andEqualTo("appName", appName);
                                criteria.andEqualTo("methodName", s2.split("#")[1]);
                                criteria.andEqualTo("userAppKey", userAppKey);
                                criteria.andEqualTo("envCode", envCode);
                                List<PradarLinkEntranceDO> linkEntranceDOList = linkEntranceMapper.selectByExample(example);
                                //System.out.println("key:"+key+"\nrule:"+s2+"\n"+linkEntranceDOList.size());
                                linkEntranceDOList.forEach(s3 ->{
                                    boolean match = antPathMatcher.match(s2.split("#")[0], s3.getServiceName());
                                    //System.out.println(match+"  <|>  "+s2+"  <|>  "+s3.getServiceName()+"  <|>  "+antPathMatcher.isPattern(s3.getServiceName()));
                                    if(match&&!s2.equals(s3.getServiceName()+"#"+s3.getMethodName())){
                                        ids.add(s3.getId());
                                    }
                                });
                                //TODO:测试
                                if(ids.size()!=0){
                                    //批量删除
                                    linkEntranceMapper.deleteByIds(ids);
                                    //System.out.println("---------------------------------");
                                    //System.out.println(s2+">--ids-->:"+ids);
                                    //System.out.println("---------------------------------");
                                }
                            }
                        });
                    }
                });
            });
            //远端同步至本地
            apisCacheLoc.cleanUp();
            apisCacheLoc.putAll(EntryRuleScheduled.apisCache.asMap());
            log.info("对齐规则成功,批次:{}",startTime);
        } catch (Exception e) {
            log.error("对齐规则发生异常:{},异常堆栈", e, e.getStackTrace());
        }finally {
            long endTime = System.currentTimeMillis();
            long cost = (endTime - startTime)/1000;
            log.info("对齐规则统计,批次:{},耗时:{}秒",startTime,cost);
        }
    }
}