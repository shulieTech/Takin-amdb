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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.instance.AgentStatusStatInfo;
import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import io.shulie.amdb.mapper.AppInstanceStatusMapper;
import io.shulie.amdb.request.query.AppInstanceStatusQueryRequest;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsResponse;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsSumResponse;
import io.shulie.amdb.service.AppInstanceStatusService;
import io.shulie.amdb.utils.PagingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author sunshiyu
 */
@Service
@Slf4j
public class AppInstanceStatusServiceImpl implements AppInstanceStatusService {
    @Resource
    AppInstanceStatusMapper appInstanceStatusMapper;

    @Override
    public Response insertOrUpdate(TAmdbAppInstanceStatusDO record) {
        try {
            appInstanceStatusMapper.insertSelective(record);
            return Response.success(record.getId());
        } catch (Exception e) {
            log.error("新增失败", e);
            return Response.fail(e.getMessage());
        }
    }

    @Override
    public Response insertBatch(List<TAmdbAppInstanceStatusDO> tAmdbApps) {
        try {
            if (!CollectionUtils.isEmpty(tAmdbApps)) {
                tAmdbApps.forEach(record -> {
                    try {
                        appInstanceStatusMapper.insertSelective(record);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            log.info("insertBatch error", e);
            return Response.fail(e.getMessage());
        }
        return Response.emptySuccess();
    }

    @Override
    public TAmdbAppInstanceStatusDO selectOneByParam(TAmdbAppInstanceStatusDO instance) {
        return appInstanceStatusMapper.selectOneByParam(instance);
    }

    @Override
    public int update(TAmdbAppInstanceStatusDO record) {
        return appInstanceStatusMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateBatch(List<TAmdbAppInstanceStatusDO> tAmdbApps) {
        if (!CollectionUtils.isEmpty(tAmdbApps)) {
            tAmdbApps.forEach(record -> {
                appInstanceStatusMapper.updateByPrimaryKey(record);
            });
            return 1;
        }
        return 0;
    }

    @Override
    public PageInfo<AmdbAppInstanceStautsResponse> selectByParams(AppInstanceStatusQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        //0611 接流川通知,临时修改为新增倒序
        example.setOrderByClause("id DESC");
//        example.setOrderByClause("gmt_modify DESC");

        Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(param.getAppNames())) {
            String[] split = param.getAppNames().split(",");
            List<String> appNames = Arrays.asList(split);
            criteria.andIn("appName", appNames);
        }
        if (StringUtils.isNotBlank(param.getAppName())) {
            criteria.andEqualTo("appName", param.getAppName());
        }

        if (StringUtils.isNotBlank(param.getIp())) {
            String[] ipAry = param.getIp().split(",");
            String ipFilter = "";
            for (int i = 0; i < ipAry.length; i++) {
                if (i > 0) {
                    ipFilter += " or ";
                }
                ipFilter += "ip like '%" + ipAry[i] + "%'";
            }
            criteria.andCondition("(" + ipFilter + ")");
        }
        if (StringUtils.isNotBlank(param.getAgentId())) {
            criteria.andEqualTo("agentId", param.getAgentId());
        }

        /**
         * 2021/05/28 新增探针状态查询过滤条件
         * 如果未传探针状态,则查所有状态
         */
        if (StringUtils.isNotBlank(param.getProbeStatus())) {
            criteria.andEqualTo("probeStatus", param.getProbeStatus());
        }

        if (StringUtils.isNotBlank(param.getAgentStatus())) {
            criteria.andEqualTo("agentStatus", param.getAgentStatus());
        }

        Long minUpdateDate = param.getMinUpdateDate();
        if (minUpdateDate != null) {
            criteria.andGreaterThan("gmtModify", DateFormatUtils.format(new Date(minUpdateDate), "yyyy/MM/dd HH:mm:ss"));
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        int page = param.getCurrentPage();
        int pageSize = param.getPageSize();
        PageHelper.startPage(page, pageSize);

        List<TAmdbAppInstanceStatusDO> instanceStatusDos = appInstanceStatusMapper.selectByExample(example);
        List<AmdbAppInstanceStautsResponse> amdbAppResponseParams = instanceStatusDos.stream().map(AmdbAppInstanceStautsResponse::new).collect(Collectors.toList());
        return PagingUtils.result(instanceStatusDos, amdbAppResponseParams);
    }

    @Override
    public AmdbAppInstanceStautsSumResponse queryInstanceSumInfo(AppInstanceStatusQueryRequest param) {
        AmdbAppInstanceStautsSumResponse response = new AmdbAppInstanceStautsSumResponse();
        try {
            response.setOnlineNodesCount(getOnlineInstanceCount(param));
            response.setSpecificStatusNodesCount(getSpecificStatusInstanceCount(param));
            response.setVersionList(getAllProbeVersionsByAppName(param));
        } catch (Exception e) {
            log.error("查询应用实例探针状态汇总信息时发生异常,异常堆栈", e);
        }
        return response;
    }

    @Override
    public Integer getOnlineInstanceCount(AppInstanceStatusQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appName", param.getAppName());
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        return appInstanceStatusMapper.selectCountByExample(example);
    }

    @Override
    public Integer getSpecificStatusInstanceCount(AppInstanceStatusQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appName", param.getAppName());

        if (StringUtils.isNotBlank(param.getProbeStatus())) {
            criteria.andEqualTo("probeStatus", param.getProbeStatus());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        return appInstanceStatusMapper.selectCountByExample(example);
    }

    @Override
    public List<String> getAllProbeVersionsByAppName(AppInstanceStatusQueryRequest param) {
        TAmdbAppInstanceStatusDO appInstanceStatusDO = new TAmdbAppInstanceStatusDO();
        appInstanceStatusDO.setAppName(param.getAppName());
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            appInstanceStatusDO.setUserAppKey(param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            appInstanceStatusDO.setEnvCode(param.getEnvCode());
        }
        return appInstanceStatusMapper.selectDistinctVersionByParam(appInstanceStatusDO);
    }


    @Override
    public void deleteByParams(AppInstanceStatusQueryRequest param) {
        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        Criteria criteria = example.createCriteria();
        criteria.andIn("appName", Arrays.asList(param.getAppName().split(",").clone()));
        if (StringUtils.isNotBlank(param.getIp())) {
            criteria.andEqualTo("ip", param.getIp());
        }
        if (StringUtils.isNotBlank(param.getPid())) {
            criteria.andEqualTo("pid", param.getPid());
        }
        if (Objects.nonNull(param.getProbeStatus())) {
            criteria.andEqualTo("probeStatus", param.getProbeStatus());
        }

        appInstanceStatusMapper.deleteByExample(example);
    }

    @Override
    public void truncateTable() {
        try {
            appInstanceStatusMapper.truncateTable();
        } catch (Exception e) {
            log.error("执行t_amdb_app_instance_status表truncate出现异常:{},异常堆栈:{}", e, e.getStackTrace());
            appInstanceStatusMapper.deleteAll();
        }
        log.warn("表t_amdb_app_instance_status已truncate");
    }

    @Override
    public AgentStatusStatInfo countStatus(AppInstanceStatusQueryRequest param) {
        AgentStatusStatInfo ret = new AgentStatusStatInfo();

        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        Criteria criteria = createInstanceStatusCriteria(example, param);

        int i = appInstanceStatusMapper.selectCountByExample(example);
        ret.setAgentCount(i);
        ret.setProbeCount(i);

        criteria.andEqualTo("agentStatus", "4");
        int agentFailCount = appInstanceStatusMapper.selectCountByExample(example);
        ret.setAgentFailCount(agentFailCount);

        example.clear();
        criteria = createInstanceStatusCriteria(example, param);
        criteria.andEqualTo("probeStatus", "4");
        int probeFailCount = appInstanceStatusMapper.selectCountByExample(example);
        ret.setProbeFailCount(probeFailCount);

        return ret;
    }

    private Criteria createInstanceStatusCriteria(Example example, AppInstanceStatusQueryRequest param) {
        Criteria criteria = example.createCriteria();
        String appNames = param.getAppNames();
        if (StringUtils.isNotBlank(appNames)) {
            criteria.andIn("appName", Arrays.asList(appNames.split(",")));
        }
        String appName = param.getAppName();
        if (StringUtils.isNotBlank(appName)) {
            criteria.andEqualTo("appName", appName);
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        return criteria;
    }
}
