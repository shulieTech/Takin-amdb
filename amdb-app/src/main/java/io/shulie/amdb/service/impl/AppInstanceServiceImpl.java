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
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.agent.AgentInfoDTO;
import io.shulie.amdb.common.dto.instance.AppInfo;
import io.shulie.amdb.common.request.agent.AmdbAgentInfoQueryRequest;
import io.shulie.amdb.common.request.app.AppInfoQueryRequest;
import io.shulie.amdb.entity.TAmdbAgentInfoDO;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.mapper.AppInstanceMapper;
import io.shulie.amdb.mapper.TAmdbAgentInfoDOMapper;
import io.shulie.amdb.request.query.TAmdbAppInstanceBatchAppQueryRequest;
import io.shulie.amdb.request.query.TAmdbAppInstanceErrorInfoByQueryRequest;
import io.shulie.amdb.request.query.TAmdbAppInstanceQueryRequest;
import io.shulie.amdb.response.instance.AmdbAppInstanceResponse;
import io.shulie.amdb.response.instance.InstanceErrorInfoResponse;
import io.shulie.amdb.service.AppInstanceService;
import io.shulie.amdb.utils.PagingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppInstanceServiceImpl implements AppInstanceService {
    @Resource
    AppInstanceMapper appInstanceMapper;
    @Resource
    TAmdbAgentInfoDOMapper tAmdbAgentInfoDOMapper;

    @Override
    public Response insert(TAmdbAppInstanceDO record) {
        try {
            insertOrUpdate(record);
            return Response.success(record.getId());
        } catch (Exception e) {
            log.error("新增失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_UPDATE);
        }
    }

    @Override
    public Response insertBatch(List<TAmdbAppInstanceDO> tAmdbApps) {
        try {
            if (!CollectionUtils.isEmpty(tAmdbApps)) {
                tAmdbApps.forEach(this::insertOrUpdate);
            }
        } catch (Exception e) {
            log.info("insertBatch error", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_UPDATE);
        }
        return Response.emptySuccess();
    }

    @Override
    public TAmdbAppInstanceDO selectOneByParam(TAmdbAppInstanceDO instance) {
        return appInstanceMapper.selectOneByParam(instance);
    }

    @Override
    public int update(TAmdbAppInstanceDO record) {
        return appInstanceMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateBatch(List<TAmdbAppInstanceDO> tAmdbApps) {
        if (!CollectionUtils.isEmpty(tAmdbApps)) {
            tAmdbApps.forEach(record -> {
                appInstanceMapper.updateByPrimaryKey(record);
            });
            return 1;
        }
        return 0;
    }

    @Override
    public PageInfo<AmdbAppInstanceResponse> selectByParams(TAmdbAppInstanceQueryRequest param) {
        int page = param.getCurrentPage();
        int pageSize = param.getPageSize();
        String filter = "app_name='" + param.getAppName() + "' ";
        if (param.getCustomerId() != null && param.getCustomerId().trim().length() > 0) {
            filter += "and customer_id ='" + param.getCustomerId() + "'";
        }
        PageHelper.startPage(page, pageSize);
        List<TAmdbAppInstanceDO> amdbApps = appInstanceMapper.selectByFilter(filter);
        List<AmdbAppInstanceResponse> amdbAppResponseParams = amdbApps.stream().map(amdbApp -> new AmdbAppInstanceResponse(amdbApp)).collect(Collectors.toList());
        return PagingUtils.result(amdbApps, amdbAppResponseParams);
    }

    @Override
    public Integer getAllInstanceCount(Long appId) {
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appId", appId);
        return appInstanceMapper.selectCountByExample(example);
    }

    /**
     * 获取在线实例列表
     *
     * @param appId
     * @return
     */
    @Override
    public Integer getOnlineInstanceCount(Long appId) {
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appId", appId);
        criteria.andCondition("(flag&1)=1");
        return appInstanceMapper.selectCountByExample(example);
    }

    /**
     * 获取异常实例列表
     *
     * @param appId
     * @return
     */
    @Override
    public Integer getExceptionInstanceCount(Long appId) {
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appId", appId);
        criteria.andCondition("(flag&2)!=2");
        criteria.andCondition("(flag&1)=1");
        return appInstanceMapper.selectCountByExample(example);
    }

    @Override
    public PageInfo<AmdbAppInstanceResponse> selectByBatchAppParams(TAmdbAppInstanceBatchAppQueryRequest param) {
        int page = param.getCurrentPage();
        int pageSize = param.getPageSize();
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (!CollectionUtils.isEmpty(param.getAppIds())) {
            criteria.andIn("appId", param.getAppIds());
        }
        if (StringUtils.isNotBlank(param.getAppNames())) {
            criteria.andIn("appName", Arrays.asList(param.getAppNames().split(",")));
        }
        if (!CollectionUtils.isEmpty(param.getAgentIds())) {
            criteria.andIn("agentId", param.getAgentIds());
        }
        if (!CollectionUtils.isEmpty(param.getIpAddress())) {
            criteria.andIn("ip", param.getIpAddress());
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
        criteria.andCondition("(flag&1)=1");
        PageHelper.startPage(page, pageSize);
        List<TAmdbAppInstanceDO> amdbApps = appInstanceMapper.selectByExample(example);
        List<AmdbAppInstanceResponse> amdbAppResponseParams = amdbApps.stream().map(amdbApp -> new AmdbAppInstanceResponse(amdbApp)).collect(Collectors.toList());
        return PagingUtils.result(amdbApps, amdbAppResponseParams);
    }

    @Override
    public List<InstanceErrorInfoResponse> selectErrorInfoByParams(TAmdbAppInstanceErrorInfoByQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(param.getAppId())) {
            criteria.andEqualTo("appId", param.getAppId());
        }
        if (StringUtils.isNotBlank(param.getAppName())) {
            criteria.andEqualTo("appName", param.getAppName());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        criteria.andCondition("(flag&1)=1");
        List<TAmdbAppInstanceDO> amdbAppInstances = appInstanceMapper.selectByExample(example);
        Map<String, InstanceErrorInfoResponse> errorMaps = new HashMap<>();
        if (CollectionUtils.isEmpty(amdbAppInstances)) {
            return new ArrayList<>();
        }
        amdbAppInstances.forEach(amdbAppInstance -> {
            String ext = amdbAppInstance.getExt();
            JSONObject jsonObject = JSON.parseObject(ext);
            JSONObject errorMsgInfos = jsonObject.getJSONObject("errorMsgInfos");
            if (CollectionUtils.isEmpty(errorMsgInfos)) {
                return;
            }
            Set<String> keySet = errorMsgInfos.keySet();
            keySet.forEach(key -> {
                JSONObject errorMsgInfoDetail = (JSONObject) errorMsgInfos.get(key);
                if (errorMaps.get(key) == null) {
                    errorMaps.put(key, new InstanceErrorInfoResponse());
                }
                InstanceErrorInfoResponse responseParam = errorMaps.get(key);
                responseParam.setId(key);
                responseParam.getAgentIds().add(amdbAppInstance.getAgentId());
                responseParam.setTime(DateFormatUtils.format((Long) errorMsgInfoDetail.get("time"), "yyyy-MM-dd HH:mm:ss"));
                responseParam.setDescription((String) errorMsgInfoDetail.get("msg"));
            });
        });
        return CollectionUtils.arrayToList(errorMaps.values().toArray());
    }

    @Override
    public void initOnlineStatus() {
        appInstanceMapper.initOnlineStatus();
    }

    @Override
    public void deleteByParams(TAmdbAppInstanceQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("appName", Arrays.asList(param.getAppName().split(",").clone()));
        if (StringUtils.isNotBlank(param.getIp())) {
            criteria.andEqualTo("ip", param.getIp());
        }
        if (StringUtils.isNotBlank(param.getAgentId())) {
            criteria.andEqualTo("agentId", param.getAgentId());
        }
        appInstanceMapper.deleteByExample(example);
    }

    @Override
    public PageInfo<AgentInfoDTO> queryAgentInfo(AmdbAgentInfoQueryRequest request) {
        Example example = new Example(TAmdbAgentInfoDO.class);
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(request.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", request.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(request.getEnvCode())) {
            criteria.andEqualTo("envCode", request.getEnvCode());
        }
        if (StringUtils.isNotBlank(request.getAppName())) {
            criteria.andEqualTo("appName", request.getAppName());
        }
        if (StringUtils.isNotBlank(request.getAgentId())) {
            criteria.andLike("agentId", '%' + request.getAgentId() + '%');
        }
        if (request.getStartDate() != null) {
            criteria.andGreaterThanOrEqualTo("agentTimestamp", request.getStartDate());
        }
        if (request.getEndDate() != null) {
            criteria.andLessThanOrEqualTo("agentTimestamp", request.getEndDate());
        }
        if (StringUtils.isNotBlank(request.getAgentInfo())) {
            criteria.andLike("agentInfo", '%' + request.getAgentInfo() + '%');
        }

        example.orderBy("agentTimestamp").desc().orderBy("id").desc();
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<TAmdbAgentInfoDO> agentInfoDOS = tAmdbAgentInfoDOMapper.selectByExample(example);

        List<AgentInfoDTO> amdbAppResponseParams = agentInfoDOS.stream()
                .map(this::agentId).collect(Collectors.toList());

        return PagingUtils.result(agentInfoDOS, amdbAppResponseParams);
    }

    @Override
    public List<TAmdbAppInstanceDO> selectOnlineAppList() {
        try {
            return appInstanceMapper.selectOnlineAppList();
        } catch (Exception e) {
            log.error("查询在线应用列表发生异常:{},异常堆栈:{}", e, e.getStackTrace());
        }
        return new ArrayList<>();
    }

    /**
     * 查询应用信息
     *
     * @return
     */
    @Override
    public Response<List<AppInfo>> queryAppInfo(AppInfoQueryRequest request) {
        Map<String, Object> map = new HashMap<>();
        //Boolean非空
        map.put("appStatus", request.getAppStatus());
        map.put("userAppKey", request.getTenantAppKey());
        map.put("envCode", request.getEnvCode());
        if (StringUtils.isNotBlank(request.getAppName())) {
            map.put("appName", request.getAppName());
        }
        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<AppInfo> appInfos = appInstanceMapper.selectSummaryAppInfo(map);
        return Response.success(PagingUtils.result(appInfos, appInfos));
    }

    private AgentInfoDTO agentId(TAmdbAgentInfoDO agentInfoDO) {
        if (agentInfoDO == null) {
            return null;
        }
        AgentInfoDTO agentInfoDTO = new AgentInfoDTO();
        agentInfoDTO.setAgentId(agentInfoDO.getAgentId());
        agentInfoDTO.setAppName(agentInfoDO.getAppName());
        agentInfoDTO.setIp(agentInfoDO.getIp());
        agentInfoDTO.setPort(agentInfoDO.getPort());
        agentInfoDTO.setUserAppKey(agentInfoDO.getUserAppKey());
        agentInfoDTO.setAgentTimestamp(agentInfoDO.getAgentTimestamp());
        agentInfoDTO.setAgentInfo(agentInfoDO.getAgentInfo());
        return agentInfoDTO;
    }

    private void insertOrUpdate(TAmdbAppInstanceDO record) {
        if (record.getId() != null) {
            appInstanceMapper.updateByPrimaryKeySelective(record);
        } else {
            appInstanceMapper.insertSelective(record);
        }
    }
}
