package io.shulie.amdb.service;

import java.util.List;

import io.shulie.amdb.entity.ReportActivityDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;

public interface ReportActivityService {

    List<ReportActivityDO> queryReportActivity(ReportInterfaceQueryRequest request);

    boolean createReportTable(String reportId);
}
