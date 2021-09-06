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

package io.shulie.amdb.adaptors.instance;

import io.shulie.amdb.adaptors.AdaptorTemplate;
import io.shulie.amdb.adaptors.base.AbstractDefaultAdaptor;
import io.shulie.amdb.adaptors.connector.Connector;
import io.shulie.amdb.adaptors.connector.DataContext;
import io.shulie.amdb.adaptors.instance.model.InstanceStatusModel;
import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import io.shulie.amdb.request.query.AppInstanceStatusQueryRequest;
import io.shulie.amdb.service.AppInstanceStatusService;
import io.shulie.amdb.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vincent
 */
// FIXME 如果增加分布式部署，需要增加分布式锁，保证每个path只有一个服务在处理
@Slf4j
public class InstanceStatusAdaptor extends AbstractDefaultAdaptor {

    private static final String INSTANCE_STATUS_PATH = "/config/log/pradar/status/";

    /**
     * path-> appName+"#"+ip+"#"+pid
     */
    private static final Map<String, String> INSTANCE_STATUS_CACHE = new HashMap<>();

    private AppInstanceStatusService appInstanceStatusService;

    private AdaptorTemplate adaptorTemplate;

    public InstanceStatusAdaptor() {

    }


    @Override
    public void addConnector() {
        adaptorTemplate.addConnector(Connector.ConnectorType.ZOOKEEPER_NODE);
    }

    /**
     *
     */
    @Override
    public void registor() {

    }


    @Override
    public void setAdaptorTemplate(AdaptorTemplate adaptorTemplate) {
        this.adaptorTemplate = adaptorTemplate;
    }


    @Override
    public boolean close() throws Exception {
        return false;
    }

    @Override
    public Object process(DataContext dataContext) {
        log.info("当前dataContext为:{}", dataContext);
        String path[] = dataContext.getPath().replaceAll(INSTANCE_STATUS_PATH, "").split("/");
        String appName = path[0];
        InstanceStatusModel instanceStatusModel = (InstanceStatusModel) dataContext.getModel();
        String oldInstanceKey = INSTANCE_STATUS_CACHE.get(dataContext.getPath());

        //如果从zk中获取到的数据模型不为空,则进入更新或者插入逻辑
        if (instanceStatusModel != null) {
            updateOrInsertInstanceStatus(appName, instanceStatusModel, oldInstanceKey);
            String newInstanceKey = appName + "#" + instanceStatusModel.getAddress() + "#" + instanceStatusModel.getPid();
            INSTANCE_STATUS_CACHE.put(dataContext.getPath(), newInstanceKey);
        } else {
            // 说明节点被删除，执行实例下线
            if (oldInstanceKey != null) {
                log.info("节点:{}已删除", oldInstanceKey);
                instanceOffline(oldInstanceKey);
                //清除缓存
                INSTANCE_STATUS_CACHE.remove(oldInstanceKey);
            }
        }
        return null;
    }

    private void updateOrInsertInstanceStatus(String appName, InstanceStatusModel newInstanceStatusModel, String oldInstanceKey) {
        Date curr = new Date();
        // 判断instance是否存在
        TAmdbAppInstanceStatusDO instanceStatus = null;

        TAmdbAppInstanceStatusDO selectParam = new TAmdbAppInstanceStatusDO();
        // 如果有更新则需要拿到原来的唯一值检索
        if (StringUtils.isBlank(oldInstanceKey)) {
            selectParam.setAppName(appName);
            selectParam.setIp(newInstanceStatusModel.getAddress());
            // 老的没有，说明服务重启缓存重置，这种情况下只能根据AgentID来更新
            selectParam.setAgentId(newInstanceStatusModel.getAgentId());
        } else {
            String instanceInfo[] = oldInstanceKey.split("#");
            selectParam.setAppName(instanceInfo[0]);
            selectParam.setIp(instanceInfo[1]);
            selectParam.setPid(instanceInfo[2]);
        }
        TAmdbAppInstanceStatusDO oldInstanceStatusDO = appInstanceStatusService.selectOneByParam(selectParam);
        if (oldInstanceStatusDO == null) {
            instanceStatus = getInsertInstanceStatusModel(newInstanceStatusModel, appName, curr);
            appInstanceStatusService.insert(instanceStatus);
        } else {
            instanceStatus = getUpdateInstanceStatusModel(oldInstanceStatusDO, newInstanceStatusModel, curr);
            appInstanceStatusService.update(instanceStatus);
        }
    }

    /**
     * 创建实例状态对象
     *
     * @param instanceStatusModel
     * @param curr
     * @return
     */
    private TAmdbAppInstanceStatusDO getInsertInstanceStatusModel(InstanceStatusModel instanceStatusModel, String appName, Date curr) {
        TAmdbAppInstanceStatusDO appInstanceStatus = new TAmdbAppInstanceStatusDO();
        appInstanceStatus.setAppName(appName);
        appInstanceStatus.setAgentId(instanceStatusModel.getAgentId());
        appInstanceStatus.setIp(instanceStatusModel.getAddress());
        appInstanceStatus.setPid(instanceStatusModel.getPid());
        appInstanceStatus.setHostname(instanceStatusModel.getHost());
        appInstanceStatus.setAgentLanguage(instanceStatusModel.getAgentLanguage());
        appInstanceStatus.setAgentVersion(instanceStatusModel.getAgentVersion());

        dealWithInstanceStatusModel(instanceStatusModel);

        appInstanceStatus.setProbeStatus(instanceStatusModel.getAgentStatus());
        appInstanceStatus.setProbeVersion(instanceStatusModel.getSimulatorVersion());
        appInstanceStatus.setErrorCode(instanceStatusModel.getErrorCode());
        appInstanceStatus.setErrorMsg(instanceStatusModel.getErrorMsg());
        appInstanceStatus.setGmtCreate(curr);
        appInstanceStatus.setGmtModify(curr);
        return appInstanceStatus;
    }

    private void dealWithInstanceStatusModel(InstanceStatusModel instanceStatusModel) {
        //探针状态转换
        if (instanceStatusModel != null) {
            if (instanceStatusModel.getSimulatorVersion() == null) {
                log.info("探针版本为null");
                instanceStatusModel.setSimulatorVersion("未知版本");
            }
            switch (StringUtil.parseStr(instanceStatusModel.getAgentStatus())) {
                case "INSTALLED":
                    instanceStatusModel.setAgentStatus("0");
                    break;
                case "UNINSTALL":
                    instanceStatusModel.setAgentStatus("1");
                    break;
                case "INSTALLING":
                    instanceStatusModel.setAgentStatus("2");
                    break;
                case "UNINSTALLING":
                    instanceStatusModel.setAgentStatus("3");
                    break;
                case "INSTALL_FAILED":
                    instanceStatusModel.setAgentStatus("4");
                    break;
                case "UNINSTALL_FAILED":
                    instanceStatusModel.setAgentStatus("5");
                    break;
                default:
                    //do nothing
                    log.info("未知状态:{}", StringUtil.parseStr(instanceStatusModel.getAgentStatus()));
                    instanceStatusModel.setAgentStatus("99");
            }
        }
    }


    /**
     * 更新实例状态对象
     *
     * @param oldInstanceStatusDO
     * @param newInstanceStatusModel
     * @param curr
     * @return
     */
    private TAmdbAppInstanceStatusDO getUpdateInstanceStatusModel(TAmdbAppInstanceStatusDO oldInstanceStatusDO, InstanceStatusModel newInstanceStatusModel, Date curr) {
        oldInstanceStatusDO.setAgentId(newInstanceStatusModel.getAgentId());
        oldInstanceStatusDO.setIp(newInstanceStatusModel.getAddress());
        oldInstanceStatusDO.setPid(newInstanceStatusModel.getPid());
        oldInstanceStatusDO.setHostname(newInstanceStatusModel.getHost());
        oldInstanceStatusDO.setAgentLanguage(newInstanceStatusModel.getAgentLanguage());
        oldInstanceStatusDO.setAgentVersion(newInstanceStatusModel.getAgentVersion());

        dealWithInstanceStatusModel(newInstanceStatusModel);

        oldInstanceStatusDO.setProbeStatus(newInstanceStatusModel.getAgentStatus());
        oldInstanceStatusDO.setProbeVersion(newInstanceStatusModel.getSimulatorVersion());
        oldInstanceStatusDO.setErrorCode(newInstanceStatusModel.getErrorCode());
        oldInstanceStatusDO.setErrorMsg(newInstanceStatusModel.getErrorMsg());
        oldInstanceStatusDO.setGmtModify(curr);
        return oldInstanceStatusDO;
    }

    /**
     * 执行实例下线
     *
     * @param oldInstanceKey
     */
    private void instanceOffline(String oldInstanceKey) {
        TAmdbAppInstanceStatusDO selectParam = new TAmdbAppInstanceStatusDO();
        // 如果AgentId被修改，则用原先的ID来更新
        String instanceInfo[] = oldInstanceKey.split("#");
        selectParam.setAppName(instanceInfo[0]);
        selectParam.setIp(instanceInfo[1]);
        selectParam.setPid(instanceInfo[2]);

        TAmdbAppInstanceStatusDO amdbAppInstanceDO = appInstanceStatusService.selectOneByParam(selectParam);
        if (amdbAppInstanceDO == null) {
            return;
        }

        AppInstanceStatusQueryRequest request = new AppInstanceStatusQueryRequest();
        request.setAppName(selectParam.getAppName());
        request.setIp(selectParam.getIp());
        request.setPid(selectParam.getPid());
        appInstanceStatusService.deleteByParams(request);
    }


    /**
     * @param config
     */
    @Override
    public void addConfig(Map<String, Object> config) {
        super.addConfig(config);

        if (!config.containsKey("appInstanceStatusService")) {
            throw new IllegalArgumentException("AppInstanceStatusService is not init.");
        }
        this.appInstanceStatusService = (AppInstanceStatusService) config.get("appInstanceStatusService");
    }
}
