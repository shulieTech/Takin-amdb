package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.ReportActivityDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface ReportActivityMapper extends Mapper<ReportActivityDO> {

    @Select("<script>"
        + " select report_id reportId, app_name appName, service_name serviceName, method_name methodName, rpc_type rpcType"
        + " , min_cost minCost, max_cost maxCost, sum_cost sumCost, avg_cost avgCost, req_cnt reqCnt, `state`"
        + " , gmt_create gmtCreate, gmt_update gmtUpdate from t_amdb_report_activity where report_id = #{reportId}"
        + "</script>")
    List<ReportActivityDO> selectByRequest(ReportInterfaceQueryRequest request);
}
