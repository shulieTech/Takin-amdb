package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.request.trodata.TrodataQueryParam;
import io.shulie.amdb.service.TroDataService;
import io.shulie.amdb.tro.mapper.TroDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunshiyu
 * @description 控制台数据查询实现类
 * @datetime 2021-09-27 7:45 下午
 */
@Service
@Slf4j
public class TroDataServiceImpl implements TroDataService {
    @Autowired
    TroDataMapper troDataMapper;

    @Override
    public String queryTroData(TrodataQueryParam param) {
        //查询应用配置
        String samplingIntervel = troDataMapper.queryConfigValueByParams(param.getUserAppKey(), param.getEnvCode(), param.getAppName(), param.getConfigKey());
        if (StringUtils.isBlank(samplingIntervel)) {
            //查询对应租户对应环境配置
            samplingIntervel = troDataMapper.queryTenantConfigValueByParams(param.getUserAppKey(), param.getEnvCode(), param.getConfigKey());
            if (StringUtils.isBlank(samplingIntervel)) {
                //查询全局配置
                return troDataMapper.queryGlobalConfigValueByParams(param.getConfigKey());
            } else {
                log.info("query tenant samplingInterval params:{},result:{}", param, samplingIntervel);
            }
        } else {
            log.info("query app samplingInterval params:{},result:{}", param, samplingIntervel);
        }
        return samplingIntervel;
    }
}
