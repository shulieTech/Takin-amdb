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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.shulie.amdb.adaptors.AdaptorTemplate;
import io.shulie.amdb.adaptors.base.AbstractDefaultAdaptor;
import io.shulie.amdb.adaptors.connector.Connector;
import io.shulie.amdb.adaptors.connector.DataContext;
import io.shulie.amdb.adaptors.instance.model.InstanceModel;
import io.shulie.amdb.adaptors.utils.FlagUtil;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.instance.AppInstanceExtDTO;
import io.shulie.amdb.common.dto.instance.ModuleLoadDetailDTO;
import io.shulie.amdb.entity.AppDO;
import io.shulie.amdb.entity.TAmdbAgentConfigDO;
import io.shulie.amdb.entity.TAmdbAppInstanceDO;
import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import io.shulie.amdb.mapper.TAmdbAgentConfigDOMapper;
import io.shulie.amdb.request.query.AppInstanceStatusQueryRequest;
import io.shulie.amdb.service.AppInstanceService;
import io.shulie.amdb.service.AppInstanceStatusService;
import io.shulie.amdb.service.AppService;
import io.shulie.amdb.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @author vincent
 */
// FIXME 如果增加分布式部署，需要增加分布式锁，保证每个path只有一个服务在处理 或者 只开启一个节点的zk监听(压力会到一个节点上面)
@Slf4j
public class InstanceAdaptor extends AbstractDefaultAdaptor {

    private static final String INSTANCE_PATH = "/config/log/pradar/client/";
    private static String serverUrl = System.getProperty("instance.amdb.server.url");

    /**
     * path-> appName+"#"+ip+"#"+pid
     */
    private static final Map<String, String> INSTANCEID_CACHE = new HashMap<>();

    private AppService appService;

    private AppInstanceService appInstanceService;

    private AdaptorTemplate adaptorTemplate;
    private AppInstanceStatusService appInstanceStatusService;
    private TAmdbAgentConfigDOMapper agentConfigDOMapper;

    public InstanceAdaptor() {

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
        if (log.isDebugEnabled()) {
            log.debug("当前dataContext为:{}", dataContext);
        }
        String path[] = dataContext.getPath().replaceAll(INSTANCE_PATH, "").split("/");
        String appName = path[0];
        InstanceModel instanceModel = (InstanceModel) dataContext.getModel();
        String oldInstanceKey = INSTANCEID_CACHE.get(dataContext.getPath());
        if (instanceModel != null) {
            updateAppAndInstance(appName, instanceModel, oldInstanceKey);
            //配置DO更新
            updateAgentConfig(appName, instanceModel);
            String newInstanceKey = appName + "#" + instanceModel.getAddress() + "#" + instanceModel.getPid();
            INSTANCEID_CACHE.put(dataContext.getPath(), newInstanceKey);
        } else {
            // 说明节点被删除，执行实例下线
            if (oldInstanceKey != null) {
                instanceOffline(oldInstanceKey);
                //instanceIdCache.remove(dataContext.getPath());

                String agentId = path[1];
                removeConfig(appName, agentId);
            }
        }
        return null;
    }

    private void updateAppAndInstance(String appName, InstanceModel instanceModel, String oldInstanceKey) {
        Date curr = new Date();
        // 判断APP记录是否存在
        AppDO params = new AppDO();
        params.setAppName(appName);
        AppDO appDO = appService.selectOneByParam(params);
        if (appDO == null) {
            appDO = getTamdAppCreateModelByInstanceModel(appName, instanceModel, curr);
            // insert,拿到返回ID
            Response insertResponse = appService.insert(appDO);
            appDO.setId(NumberUtils.toLong(insertResponse.getData() + ""));
        } else {
            appDO = getTamdAppUpdateModelByInstanceModel(instanceModel, appDO, curr);
            // update
            appService.update(appDO);
        }
        // 判断instance是否存在
        TAmdbAppInstanceDO amdbAppInstance = null;
        TAmdbAppInstanceDO selectParam = new TAmdbAppInstanceDO();
        // 如果有更新则需要拿到原来的唯一值检索
        if (StringUtils.isBlank(oldInstanceKey)) {
            selectParam.setAppName(appName);
            selectParam.setIp(instanceModel.getAddress());
            // 老的没有，说明服务重启缓存重置，这种情况下只能根据AgentID来更新
            selectParam.setAgentId(instanceModel.getAgentId());
        } else {
            String instanceInfo[] = oldInstanceKey.split("#");
            selectParam.setAppName(instanceInfo[0]);
            selectParam.setIp(instanceInfo[1]);
            selectParam.setPid(instanceInfo[2]);
        }
        TAmdbAppInstanceDO appInstanceDO = appInstanceService.selectOneByParam(selectParam);
        if (appInstanceDO == null) {
            amdbAppInstance = getTamdAppInstanceCreateModelByInstanceModel(appDO, instanceModel, curr);
            appInstanceService.insert(amdbAppInstance);
        } else {
            amdbAppInstance = getTamdAppInstanceUpdateModelByInstanceModel(appDO, instanceModel, appInstanceDO, curr);
            appInstanceService.update(amdbAppInstance);
        }

        // 处理agent状态映射关系，与 探针 status 处理一致
        dealWithProbeStatusModel(instanceModel);
        TAmdbAppInstanceStatusDO instanceStatus = createInstanceStatus(appName, instanceModel);
        appInstanceStatusService.insertOrUpdate(instanceStatus);
    }

    /**
     * 创建APP对象
     *
     * @param appName
     * @param instanceModel
     * @param curr
     * @return
     */
    private AppDO getTamdAppCreateModelByInstanceModel(String appName, InstanceModel instanceModel, Date curr) {
        AppDO tAmdbApp = new AppDO();
        tAmdbApp.setAppName(appName);
        if (instanceModel.getExt() == null || instanceModel.getExt().length() == 0) {
            instanceModel.setExt("{}");
        }
        Map<String, Object> ext = JSON.parseObject(instanceModel.getExt());
        ext.put("jars", instanceModel.getJars());
        tAmdbApp.setExt(JSON.toJSONString(ext));
        tAmdbApp.setCreator("");
        tAmdbApp.setCreatorName("");
        tAmdbApp.setModifier("");
        tAmdbApp.setModifierName("");
        tAmdbApp.setGmtCreate(curr);
        tAmdbApp.setGmtModify(curr);
        return tAmdbApp;
    }

    /**
     * APP更新对象
     *
     * @param instanceModel
     * @param oldAmdbApp
     * @param curr
     * @return
     */
    private AppDO getTamdAppUpdateModelByInstanceModel(InstanceModel instanceModel, AppDO oldAmdbApp, Date curr) {
        Map<String, Object> ext = JSON.parseObject(oldAmdbApp.getExt() == null ? "{}" : oldAmdbApp.getExt());
        if (ext == null) {
            ext = new HashMap<>();
        }
        ext.put("jars", instanceModel.getJars());
        oldAmdbApp.setExt(JSON.toJSONString(ext));
        oldAmdbApp.setGmtModify(curr);
        return oldAmdbApp;
    }

    /**
     * 实例创建对象
     *
     * @param amdbApp
     * @param instanceModel
     * @param curr
     * @return
     */
    private TAmdbAppInstanceDO getTamdAppInstanceCreateModelByInstanceModel(AppDO amdbApp, InstanceModel instanceModel, Date curr) {
        TAmdbAppInstanceDO amdbAppInstance = new TAmdbAppInstanceDO();
        amdbAppInstance.setAppName(amdbApp.getAppName());
        amdbAppInstance.setAppId(amdbApp.getId());
        amdbAppInstance.setAgentId(instanceModel.getAgentId());
        amdbAppInstance.setIp(instanceModel.getAddress());
        amdbAppInstance.setPid(instanceModel.getPid());
        amdbAppInstance.setAgentVersion(instanceModel.getAgentVersion());
        amdbAppInstance.setMd5(instanceModel.getMd5());
        amdbAppInstance.setAgentLanguage(instanceModel.getAgentLanguage());
//        if (instanceModel.getErrorCode() != null && instanceModel.getErrorCode().trim().length() > 0) {
//            Map<String, Map<String, Object>> errorMsgInfos = new HashMap<String, Map<String, Object>>();
//            Map<String, Object> errorMsgInfo = new HashMap<String, Object>();
//            errorMsgInfo.put("msg", instanceModel.getErrorMsg());
//            errorMsgInfo.put("time", new Date());
//            errorMsgInfos.put(instanceModel.getErrorCode(), errorMsgInfo);
//            ext.put("errorMsgInfos", errorMsgInfos);
//        } else {
//            ext.put("errorMsgInfos", "{}");
//        }
        AppInstanceExtDTO ext = new AppInstanceExtDTO();
        Map<String, String> simulatorConfig = JSON.parseObject(instanceModel.getSimulatorFileConfigs(), new TypeReference<Map<String, String>>() {
        });
        ext.setSimulatorConfigs(simulatorConfig);
        ext.setModuleLoadResult(instanceModel.getModuleLoadResult());
        List<ModuleLoadDetailDTO> moduleLoadDetailDTOS = JSON.parseArray(instanceModel.getModuleLoadDetail(), ModuleLoadDetailDTO.class);
        ext.setModuleLoadDetail(moduleLoadDetailDTOS);
        ext.setErrorMsgInfos("{}");
        ext.setGcType(instanceModel.getGcType());
        ext.setHost(instanceModel.getHost());
        ext.setStartTime(instanceModel.getStartTime());
        ext.setJdkVersion(instanceModel.getJdkVersion());
        amdbAppInstance.setHostname(instanceModel.getHost());
        amdbAppInstance.setExt(JSON.toJSONString(ext));
        amdbAppInstance.setFlag(0);
        amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 1, true));
        if (instanceModel.isStatus()) {
            amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 2, true));
        } else {
            amdbAppInstance.setFlag(FlagUtil.setFlag(amdbAppInstance.getFlag(), 2, false));
        }
        amdbAppInstance.setCreator("");
        amdbAppInstance.setCreatorName("");
        amdbAppInstance.setModifier("");
        amdbAppInstance.setModifierName("");
        amdbAppInstance.setGmtCreate(curr);
        amdbAppInstance.setGmtModify(curr);
        return amdbAppInstance;
    }

    /**
     * 实例更新对象
     *
     * @param amdbApp
     * @param instanceModel
     * @param oldAmdbAppInstance
     * @param curr
     * @return
     */
    private TAmdbAppInstanceDO getTamdAppInstanceUpdateModelByInstanceModel(AppDO amdbApp, InstanceModel instanceModel, TAmdbAppInstanceDO oldAmdbAppInstance, Date curr) {
        oldAmdbAppInstance.setAppName(amdbApp.getAppName());
        oldAmdbAppInstance.setAppId(amdbApp.getId());
        oldAmdbAppInstance.setAgentId(instanceModel.getAgentId());
        oldAmdbAppInstance.setIp(instanceModel.getAddress());
        oldAmdbAppInstance.setPid(instanceModel.getPid());
        oldAmdbAppInstance.setAgentVersion(instanceModel.getAgentVersion());
        oldAmdbAppInstance.setMd5(instanceModel.getMd5());
        oldAmdbAppInstance.setAgentLanguage(instanceModel.getAgentLanguage());
        oldAmdbAppInstance.setHostname(instanceModel.getHost());
        AppInstanceExtDTO ext = new AppInstanceExtDTO();
        Map<String, String> simulatorConfig = JSON.parseObject(instanceModel.getSimulatorFileConfigs(), new TypeReference<Map<String, String>>() {
        });
        ext.setSimulatorConfigs(simulatorConfig);
        ext.setModuleLoadResult(instanceModel.getModuleLoadResult());
        List<ModuleLoadDetailDTO> moduleLoadDetailDTOS = JSON.parseArray(instanceModel.getModuleLoadDetail(), ModuleLoadDetailDTO.class);
        ext.setModuleLoadDetail(moduleLoadDetailDTOS);
        ext.setErrorMsgInfos("{}");
        ext.setGcType(instanceModel.getGcType());
        ext.setHost(instanceModel.getHost());
        ext.setStartTime(instanceModel.getStartTime());
        ext.setJdkVersion(instanceModel.getJdkVersion());
        oldAmdbAppInstance.setExt(JSON.toJSONString(ext));
        // 改为在线状态
        oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 1, true));
        // 设置Agent状态
        if (instanceModel.isStatus()) {
            // 设为正常状态
            oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 2, true));
        } else {
            // 设置为异常状态
            oldAmdbAppInstance.setFlag(FlagUtil.setFlag(oldAmdbAppInstance.getFlag(), 2, false));
        }
        oldAmdbAppInstance.setGmtModify(curr);
        return oldAmdbAppInstance;
    }

    /**
     * 执行实例下线
     *
     * @param oldInstanceKey
     */
    private void instanceOffline(String oldInstanceKey) {
        TAmdbAppInstanceDO selectParam = new TAmdbAppInstanceDO();
        // 如果AgentId被修改，则用原先的ID来更新
        String instanceInfo[] = oldInstanceKey.split("#");
        selectParam.setAppName(instanceInfo[0]);
        selectParam.setIp(instanceInfo[1]);
        selectParam.setPid(instanceInfo[2]);
        TAmdbAppInstanceDO amdbAppInstanceDO = appInstanceService.selectOneByParam(selectParam);
        if (amdbAppInstanceDO == null) {
            return;
        }
        amdbAppInstanceDO.setFlag(FlagUtil.setFlag(amdbAppInstanceDO.getFlag(), 1, false));
        appInstanceService.update(amdbAppInstanceDO);

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

        /**
         *     private AppService appService;
         *
         *     private AppInstanceService appInstanceService;
         */
        if (!config.containsKey("appService") || !config.containsKey("appInstanceService")) {
            throw new IllegalArgumentException("AppService and appInstanceService is not init.");
        }
        this.appService = (AppService) config.get("appService");
        this.appInstanceService = (AppInstanceService) config.get("appInstanceService");
        //todo check
        this.appInstanceStatusService = (AppInstanceStatusService) config.get("appInstanceStatusService");
        this.agentConfigDOMapper = (TAmdbAgentConfigDOMapper) config.get("agentConfigDOMapper");

    }

    private void dealWithProbeStatusModel(InstanceModel instanceModel) {
        //探针状态转换
        if (instanceModel != null) {
            if (instanceModel.getSimulatorVersion() == null) {
                log.info("探针版本为null");
                instanceModel.setSimulatorVersion("未知版本");
            }
            switch (StringUtil.parseStr(instanceModel.getAgentStatus())) {
                case "INSTALLED":
                    instanceModel.setAgentStatus("0");
                    break;
                case "UNINSTALL":
                    instanceModel.setAgentStatus("1");
                    break;
                case "INSTALLING":
                    instanceModel.setAgentStatus("2");
                    break;
                case "UNINSTALLING":
                    instanceModel.setAgentStatus("3");
                    break;
                case "INSTALL_FAILED":
                    instanceModel.setAgentStatus("4");
                    break;
                case "UNINSTALL_FAILED":
                    instanceModel.setAgentStatus("5");
                    break;
                default:
                    log.info("agent未知状态:{}", StringUtil.parseStr(instanceModel.getAgentStatus()));
                    instanceModel.setAgentStatus("99");
            }
        }
    }

    // 这里应该设置 探针 的状态、错误码、错误信息
    private TAmdbAppInstanceStatusDO createInstanceStatus(String appName, InstanceModel instanceModel) {
        TAmdbAppInstanceStatusDO instanceStatus = new TAmdbAppInstanceStatusDO();
        instanceStatus.setAppName(appName);
        instanceStatus.setIp(instanceModel.getAddress());
        instanceStatus.setAgentId(instanceModel.getAgentId());
        instanceStatus.setPid(instanceModel.getPid());
        instanceStatus.setAgentErrorCode(instanceModel.getErrorCode());
        instanceStatus.setAgentErrorMsg(instanceModel.getErrorMsg());
        instanceStatus.setAgentStatus(instanceModel.getAgentStatus());
        return instanceStatus;
    }

    private void updateAgentConfig(String appName, InstanceModel instanceModel) {
        removeConfig(appName, instanceModel.getAgentId());

        List<TAmdbAgentConfigDO> agentConfigs = buildAgentConfig(appName, instanceModel);

        if (agentConfigs != null && !agentConfigs.isEmpty()) {
            saveConfig(agentConfigs);
        }
    }

    private void saveConfig(List<TAmdbAgentConfigDO> agentConfigs) {
        agentConfigDOMapper.batchInsert(agentConfigs);
    }

    private void removeConfig(String appName, String agentId) {
        Objects.requireNonNull(appName);
        Objects.requireNonNull(agentId);
        Example example = new Example(TAmdbAppInstanceStatusDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("appName", appName);
        criteria.andEqualTo("agentId", agentId);
        agentConfigDOMapper.deleteByExample(example);
    }

    private List<TAmdbAgentConfigDO> buildAgentConfig(String appName, InstanceModel instanceModel) {
        String agentAndSimulatorFileConfigsCheck = instanceModel.getSimulatorFileConfigsCheck();
        if (StringUtils.isBlank(agentAndSimulatorFileConfigsCheck)) {
            return null;
        }
        // 配置生效状态
        Map<String, String> configCheck = JSON.parseObject(agentAndSimulatorFileConfigsCheck, new TypeReference<Map<String, String>>() {
        });
        List<TAmdbAgentConfigDO> ret = new ArrayList<>();
        String agentId = instanceModel.getAgentId();

        // agent配置项
        String agentConfig = instanceModel.getAgentFileConfigs();
        if (StringUtils.isNotBlank(agentConfig)) {
            Map<String, String> configKeyValues = JSON.parseObject(agentConfig, new TypeReference<Map<String, String>>() {
            });
            configKeyValues.forEach((configKey, configValue) -> {
                TAmdbAgentConfigDO configDO = new TAmdbAgentConfigDO();
                configDO.setAgentId(agentId);
                configDO.setAppName(appName);
                configDO.setConfigKey(configKey);
                configDO.setConfigValue(configValue);
                String status = configCheck.get(configKey);
                if (status == null) {
                    status = configCheck.get("status");
                }
                configDO.setStatus(Boolean.parseBoolean(status));
                ret.add(configDO);
            });
        }
        // 探针配置项
        String simulatorConfigs = instanceModel.getSimulatorFileConfigs();
        if (StringUtils.isNotBlank(simulatorConfigs)) {
            Map<String, String> simulatorConfigsKeyValues = JSON.parseObject(simulatorConfigs, new TypeReference<Map<String, String>>() {
            });
            simulatorConfigsKeyValues.forEach((configKey, configValue) -> {
                TAmdbAgentConfigDO configDO = new TAmdbAgentConfigDO();
                configDO.setAgentId(agentId);
                configDO.setAppName(appName);
                configDO.setConfigKey(configKey);
                configDO.setConfigValue(configValue);
                String status = configCheck.get(configKey);
                if (status == null) {
                    status = configCheck.get("status");
                }
                configDO.setStatus(Boolean.parseBoolean(status));
                ret.add(configDO);
            });
        }
        return ret;
    }
}
