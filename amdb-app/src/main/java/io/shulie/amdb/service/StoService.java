package io.shulie.amdb.service;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.sto.StoQueryDTO;
import io.shulie.amdb.common.request.sto.StoQueryRequest;

public interface StoService {
    public Response<StoQueryDTO> getServiceMetrics(StoQueryRequest request);
}
