package io.shulie.amdb.service;

import io.shulie.amdb.common.request.trodata.TrodataQueryParam;

/**
 * @author sunshiyu
 * @description 控制台数据查询
 * @datetime 2021-09-27 7:43 下午
 */
public interface TroDataService {
    String queryTroData(TrodataQueryParam param);
}
