package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.ReportInterfaceMetricsDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import tk.mybatis.mapper.common.Mapper;

public interface ReportInterfaceMetricsMapper extends Mapper<ReportInterfaceMetricsDO> {

    List<ReportInterfaceMetricsDO> selectByRequest(ReportInterfaceQueryRequest request);
}
