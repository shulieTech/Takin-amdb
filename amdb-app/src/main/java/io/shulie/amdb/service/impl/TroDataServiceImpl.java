package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.request.trodata.TrodataQueryParam;
import io.shulie.amdb.service.TroDataService;
import io.shulie.amdb.tro.mapper.TroDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunshiyu
 * @description 控制台数据查询实现类
 * @datetime 2021-09-27 7:45 下午
 */
@Service
public class TroDataServiceImpl implements TroDataService {
    @Autowired
    TroDataMapper troDataMapper;

    @Override
    public String queryTroData(TrodataQueryParam param) {
        return troDataMapper.queryConfigValueByParams(param.getUserAppKey(), param.getEnvCode(), param.getAppName(), param.getConfigKey());
    }
}
