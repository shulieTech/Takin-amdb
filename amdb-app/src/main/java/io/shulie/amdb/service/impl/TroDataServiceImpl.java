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
        //根据租户
        String samplingIntervel = troDataMapper.queryConfigValueByParams(param.getUserAppKey(), param.getEnvCode(), param.getAppName(), param.getConfigKey());
        log.info("query app samplingInterval params:{},result:{}", param, samplingIntervel);
        if (StringUtils.isBlank(samplingIntervel)) {
            //查询全局配置
            return troDataMapper.queryDefaultConfigValueByParams(param.getConfigKey());
        }
        return samplingIntervel;
    }
}
