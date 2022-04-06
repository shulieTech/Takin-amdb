package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.ReportActivityInterfaceDO;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface ReportActivityInterfaceMapper extends Mapper<ReportActivityInterfaceDO> {

    @Select("<script>"
        + " select report_id reportId, app_name appName, service_name serviceName, method_name methodName, rpc_type rpcType"
        + " , min_cost minCost, max_cost maxCost, sum_cost sumCost, req_cnt reqCnt, gmt_create gmtCreate"
        + " , avg_cost avgCost, cost_percent costPercent, service_avg_cost serviceAvgCost"
        + " , entrance_app_name entranceAppName, entrance_service_name entranceServiceName, entrance_method_name entranceMethodName"
        + " , entrance_rpc_type entranceRpcType, gmt_update gmtUpdate, `state` from t_amdb_report_interface where report_id = #{reportId}"
        + "</script>")
    List<ReportActivityInterfaceDO> selectByRequest(ReportInterfaceQueryRequest request);
}
