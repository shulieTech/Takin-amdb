package io.shulie.amdb.service;

import java.util.List;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.entity.ReportInterfaceMetricsDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;

public interface ReportInterfaceMetricsService {

    List<ReportInterfaceMetricsDO> queryReportInterfaceMetrics(ReportInterfaceQueryRequest request);
}
