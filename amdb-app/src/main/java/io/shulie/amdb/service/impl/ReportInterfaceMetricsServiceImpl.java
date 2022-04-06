package io.shulie.amdb.service.impl;

import java.util.List;

import javax.annotation.Resource;

import io.shulie.amdb.entity.ReportInterfaceMetricsDO;
import io.shulie.amdb.mapper.ReportInterfaceMetricsMapper;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import io.shulie.amdb.service.ReportInterfaceMetricsService;
import org.springframework.stereotype.Service;

@Service
public class ReportInterfaceMetricsServiceImpl implements ReportInterfaceMetricsService {

    @Resource
    private ReportInterfaceMetricsMapper mapper;

    @Override
    public List<ReportInterfaceMetricsDO> queryReportInterfaceMetrics(ReportInterfaceQueryRequest request) {
        return mapper.selectByRequest(request);
    }
}
