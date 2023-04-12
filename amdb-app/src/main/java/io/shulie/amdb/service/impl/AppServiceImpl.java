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

package io.shulie.amdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.entity.AppShadowBizTableDO;
import io.shulie.amdb.entity.AppShadowDatabaseDO;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.mapper.AppInstanceMapper;
import io.shulie.amdb.mapper.AppMapper;
import io.shulie.amdb.mapper.AppShadowDatabaseMapper;
import io.shulie.amdb.request.query.AppShadowBizTableRequest;
import io.shulie.amdb.request.query.AppShadowDatabaseRequest;
import io.shulie.amdb.request.query.TAmdbAppBatchAppQueryRequest;
import io.shulie.amdb.response.app.AmdbAppResponse;
import io.shulie.amdb.response.app.model.InstanceInfo;
import io.shulie.amdb.service.AppService;
import io.shulie.amdb.utils.PagingUtils;
import io.shulie.amdb.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppServiceImpl implements AppService {
    @Resource
    private AppMapper appMapper;
    @Resource
    AppInstanceMapper appInstanceMapper;

    @Resource
    private AppShadowDatabaseMapper appShadowDatabaseMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String APP_INSTANCE_SHADOW_VALUE = "app_instance_shadow_value";
    @Override
    public Response insert(AppDO record) {
        try {
            appMapper.insertSelective(record);
            return Response.success(record.getId());
        } catch (Exception e) {
            log.info("insertBatch error", e);
            return Response.fail(e.getMessage());
        }
    }

    @Override
    @Async
    public void insertAsync(AppDO record) {
        appMapper.inUpdateSelective(record);
    }

    @Override
    public int insertBatch(List<AppDO> tAmdbApps) {
        appMapper.insertList(tAmdbApps);
        return 1;
    }

    @Override
    public int update(AppDO tAmdbApp) {
        return appMapper.updateByPrimaryKeySelective(tAmdbApp);
    }

    @Override
    public int updateBatch(List<AppDO> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            update(tAmdbApp);
        });
        return 1;
    }

    @Override
    public int delete(AppDO tAmdbApp) {
        return appMapper.deleteByPrimaryKey(tAmdbApp.getId());
    }

    @Override
    public AppDO selectByPrimaryKey(AppDO tAmdbApp) {
        return appMapper.selectByPrimaryKey(tAmdbApp.getId());
    }

    @Override
    public AppDO selectOneByParam(AppDO tAmdbApp) {
        return appMapper.selectOneByParam(tAmdbApp);
    }

    @Override
    public List<AppDO> selectByFilter(String filter) {
        return appMapper.selectByFilter(filter);
    }



    @Override
    public PageInfo<AmdbAppResponse> selectByBatchAppParams(TAmdbAppBatchAppQueryRequest param) {
        Example example = new Example(AppDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (!CollectionUtils.isEmpty(param.getAppIds())) {
            criteria.andIn("id", param.getAppIds());
        }
        if (!CollectionUtils.isEmpty(param.getAppNames())) {
            criteria.andIn("appName", param.getAppNames());
        }
        if (StringUtils.isNotBlank(param.getTenantKey())) {
            criteria.andEqualTo("tenant", param.getTenantKey());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        PageHelper.startPage(param.getCurrentPage(), param.getPageSize());
        List<AppDO> amdbApps = appMapper.selectByExample(example);
        List<AmdbAppResponse> responses = amdbApps.stream().map(amdbApp -> new AmdbAppResponse(param.getFields(), amdbApp)).collect(Collectors.toList());

        //无数据直接返回
        if (responses.size() == 0) {
            return PagingUtils.result(amdbApps, responses);
        }

        //有数据 需填充返回字段 总节点数 在线节点数 应用状态

        List<TAmdbAppInstanceDO> tAmdbAppInstanceDOS = appInstanceMapper.selectFlagByAppId(amdbApps);
        for (AmdbAppResponse response : responses) {
            Long appId = response.getAppId();
            List<TAmdbAppInstanceDO> instanceDOS = tAmdbAppInstanceDOS.stream()
                    .filter(v -> {
                        if (!appId.equals(v.getAppId())) {
                            return false;
                        }
                        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
                            if (!param.getTenantAppKey().equals(v.getUserAppKey())) {
                                return false;
                            }
                        }
                        if (StringUtils.isNotBlank(param.getEnvCode())) {
                            if (!param.getEnvCode().equals(v.getEnvCode())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
            int totalCount = instanceDOS.size();
            int onlineCount = (int) instanceDOS.stream().filter(instance -> (instance.getFlag() & 1) == 1).count();
            long exceptionCount = instanceDOS.stream().filter(instance -> (instance.getFlag() & 2) != 2 && (instance.getFlag() & 1) == 1).count();

            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.setInstanceAmount(totalCount);
            instanceInfo.setInstanceOnlineAmount(onlineCount);

            response.setInstanceInfo(instanceInfo);
            response.setAppIsException(exceptionCount > 0 || onlineCount == 0);

        }

        return PagingUtils.result(amdbApps, responses);
    }

    @Override
    public PageInfo<AmdbAppResponse> selectByBatchAppParams2(TAmdbAppBatchAppQueryRequest param) {
        List<AppDO> appDOS = new ArrayList<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(param.getAppNames())) {
            Example example = new Example(AppDO.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("appName", param.getAppNames());
            extracted(param, criteria);
            example.selectProperties("id", "tenant", "userAppKey", "envCode");
            List<AppDO> appDOList = appMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(appDOList)) {
                return PagingUtils.result(new ArrayList<>(), new ArrayList<>());
            }

            Map<String,AppDO> appDOMap = new HashMap<>();
            for (AppDO appDO : appDOList) {
                String key = getKey(param, appDO.getId());
                appDOMap.put(key,appDO);
            }
            Map<String, String> map = RedisUtil.hmget(redisTemplate, APP_INSTANCE_SHADOW_VALUE,new ArrayList<>(appDOMap.keySet()));

            List<AppDO> cacheNoneList = new ArrayList<>();
            if (MapUtils.isEmpty(map)) {
                cacheNoneList.addAll(appDOList);
            }else {
                Iterator<String> iterator = appDOMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (map.containsKey(key)) {
                        String value = map.get(key);
                        appDOS.add(JSON.parseObject(value, AppDO.class));
                    } else {
                        cacheNoneList.add(appDOMap.get(key));
                    }
                }
            }

            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cacheNoneList)) {
                List<Long> ids = cacheNoneList.stream().map(appDO -> appDO.getId()).collect(Collectors.toList());
                Example newExample = new Example(AppDO.class);
                Example.Criteria newCriteria = newExample.createCriteria();
                extracted(ids, param, newCriteria);
                List<AppDO> appDOList1 = appMapper.selectByExample(newExample);
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(appDOList1)) {
                    appDOS.addAll(appDOList1);
                    RedisUtil.hmset(redisTemplate, APP_INSTANCE_SHADOW_VALUE, appDOS.stream().collect(Collectors.toMap(appDO -> getKey(param, appDO.getId()), appDO -> JSON.toJSONString(appDO))));
                    redisTemplate.expire(APP_INSTANCE_SHADOW_VALUE, 15, TimeUnit.MINUTES);
                }
            }
        } else if (!CollectionUtils.isEmpty(param.getAppIds())) {
            Example idExample = new Example(AppDO.class);
            Example.Criteria idCriteria = idExample.createCriteria();
            idCriteria.andIn("id", param.getAppIds());
            extracted(param, idCriteria);
            PageHelper.startPage(param.getCurrentPage(), param.getPageSize());
            appDOS = appMapper.selectByExample(idExample);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(appDOS)){
                RedisUtil.hmset(redisTemplate, APP_INSTANCE_SHADOW_VALUE, appDOS.stream().collect(Collectors.toMap(appDO -> getKey(param, appDO.getId()), appDO -> JSON.toJSONString(appDO))));
                redisTemplate.expire(APP_INSTANCE_SHADOW_VALUE, 15, TimeUnit.MINUTES);}
        }
        //无数据直接返回
        if (org.apache.commons.collections.CollectionUtils.isEmpty(appDOS)) {
            return PagingUtils.result(appDOS, new ArrayList<>());
        }

        List<AmdbAppResponse> responses = appDOS.stream()
                .filter(a -> Objects.nonNull(a))
                .map(amdbApp -> new AmdbAppResponse(param.getFields(), amdbApp)).collect(Collectors.toList());

        List<TAmdbAppInstanceDO> tAmdbAppInstanceDOS = appInstanceMapper.selectFlagByAppId(appDOS);
        for (AmdbAppResponse response : responses) {
            Long appId = response.getAppId();
            List<TAmdbAppInstanceDO> instanceDOS = tAmdbAppInstanceDOS.stream()
                    .filter(v -> {
                        if (!appId.equals(v.getAppId())) {
                            return false;
                        }
                        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
                            if (!param.getTenantAppKey().equals(v.getUserAppKey())) {
                                return false;
                            }
                        }
                        if (StringUtils.isNotBlank(param.getEnvCode())) {
                            if (!param.getEnvCode().equals(v.getEnvCode())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
            int totalCount = instanceDOS.size();
            int onlineCount = (int) instanceDOS.stream().filter(instance -> (instance.getFlag() & 1) == 1).count();
            long exceptionCount = instanceDOS.stream().filter(instance -> (instance.getFlag() & 2) != 2 && (instance.getFlag() & 1) == 1).count();

            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.setInstanceAmount(totalCount);
            instanceInfo.setInstanceOnlineAmount(onlineCount);

            response.setInstanceInfo(instanceInfo);
            response.setAppIsException(exceptionCount > 0 || onlineCount == 0);
        }
        return PagingUtils.result(appDOS, responses);
    }

    private static String getKey(TAmdbAppBatchAppQueryRequest param,long appId) {
        StringBuffer stringBuffer = new StringBuffer().append(appId);
        if (StringUtils.isNotBlank(param.getTenantKey())) {
            stringBuffer.append("_").append(param.getTenantKey());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            stringBuffer.append("_").append(param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            stringBuffer.append("_").append(param.getEnvCode());
        }
        return stringBuffer.toString();
    }

    private static void extracted(TAmdbAppBatchAppQueryRequest param, Example.Criteria criteria) {
        if (StringUtils.isNotBlank(param.getTenantKey())) {
            criteria.andEqualTo("tenant", param.getTenantKey());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
    }

    private static void extracted(List<Long> ids, TAmdbAppBatchAppQueryRequest param, Example.Criteria criteria) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            criteria.andIn("id", ids);
        }
        if (StringUtils.isNotBlank(param.getTenantKey())) {
            criteria.andEqualTo("tenant", param.getTenantKey());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
    }

    @Override
    public List<String> selectAllAppName(TAmdbAppBatchAppQueryRequest param) {
        String sql = "select app_name as appName from t_amdb_app where app_type in ('APP','virtual')";
        List<LinkedHashMap> result = appMapper.selectBySql(sql);
        return result.stream().map(map -> ObjectUtils.toString(map.get("appName"))).collect(Collectors.toList());
    }

    @Override
    public PageInfo<AppShadowDatabaseDO> selectShadowDatabase(AppShadowDatabaseRequest request) {
        Example example = new Example(AppShadowDatabaseDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appName", request.getAppName());
        if (StringUtils.isNotBlank(request.getDataSource())) {
            criteria.andEqualTo("dataSource", request.getDataSource());
        }
        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            criteria.andEqualTo("envCode", request.getEnvCode());
        }
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        return PageInfo.of(appShadowDatabaseMapper.selectByExample(example));
    }

    @Override
    public List<AppShadowBizTableDO> selectShadowBizTables(AppShadowBizTableRequest request) {
        return appShadowDatabaseMapper.selectShadowBizTables(request);
    }
}
