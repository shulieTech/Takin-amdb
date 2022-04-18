package io.shulie.amdb.service;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.request.query.InfluxDbQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/28
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public interface InfluxDbQueryService {

    Response<List<Map<String, Object>>> queryObjectByConditions(InfluxDbQueryRequest request);
}
