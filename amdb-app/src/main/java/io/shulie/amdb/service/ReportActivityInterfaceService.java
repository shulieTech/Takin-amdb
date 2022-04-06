package io.shulie.amdb.service;

import java.util.List;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.ReportActivityInterfaceDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;

public interface ReportActivityInterfaceService {

    List<ReportActivityInterfaceDO> queryReportActivityInterface(ReportInterfaceQueryRequest request);
}
