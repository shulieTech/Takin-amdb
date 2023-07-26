package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.trace.TracePressureCodeDTO;
import io.shulie.amdb.common.dto.trace.TracePressureCostDTO;
import io.shulie.amdb.common.dto.trace.TracePressureOneTraceDTO;
import io.shulie.amdb.common.request.trace.TraceCostCountQueryParam;
import io.shulie.amdb.common.request.trace.TracePressureQueryParam;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/amdb/db/api/tracePressure")
@Slf4j
public class TracePressureController {

    @Resource
    private ClickHouseSupport clickHouseSupport;

    @GetMapping("/queryStatusCode")
    public Response<List<TracePressureCodeDTO>> getCodeListByParam(TracePressureQueryParam queryParam) {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct(resultCode) as statusCode");
        sql.append(" from t_trace_pressure");
        sql.append(" where startDate>='"+queryParam.getStartTime()+"'");
        sql.append(" and startDate<='"+queryParam.getEndTime()+"'");
        sql.append(" and taskId='"+queryParam.getJobId()+"'");
        sql.append(" and serviceName='"+queryParam.getServiceName()+"'");
        if(StringUtils.isNotBlank(queryParam.getRequestMethod())) {
            sql.append(" and methodName='"+queryParam.getRequestMethod()+"'");
        }
        log.info("TracePressureController#getCodeListByParam execute sql={}", sql);
        return Response.success(clickHouseSupport.queryForList(sql.toString(), TracePressureCodeDTO.class));
    }

    @GetMapping("/queryOneTrace")
    public Response<TracePressureOneTraceDTO> getOneTraceByParam(TracePressureQueryParam queryParam) {
        StringBuilder sql = new StringBuilder();
        sql.append("select traceId,cost,request,response,startDate");
        sql.append(" from t_trace_pressure");
        sql.append(" where startDate>='"+queryParam.getStartTime()+"'");
        sql.append(" and startDate<='"+queryParam.getEndTime()+"'");
        sql.append(" and taskId='"+queryParam.getJobId()+"'");
        sql.append(" and serviceName='"+queryParam.getServiceName()+"'");
        if(StringUtils.isNotBlank(queryParam.getRequestMethod())) {
            sql.append(" and methodName='"+queryParam.getRequestMethod()+"'");
        }
        if(StringUtils.isNotBlank(queryParam.getResultCode())) {
            StringBuilder code = new StringBuilder();
            code.append("(");
            for(String statusCode : StringUtils.split(queryParam.getResultCode(), ",")) {
                code.append("'"+statusCode+"'");
                code.append(",");
            }
            code.deleteCharAt(code.length() - 1);
            code.append(")");
            sql.append(" and resultCode in " + code.toString());
        }
        sql.append(" order by cost desc");
        sql.append(" limit 1");
        log.info("TracePressureController#getOneTraceByParam execute sql={}", sql);
        List<TracePressureOneTraceDTO> dataList = clickHouseSupport.queryForList(sql.toString(), TracePressureOneTraceDTO.class);
        return CollectionUtils.isNotEmpty(dataList) ? Response.success(dataList.get(0)) : Response.success(new TracePressureOneTraceDTO());
    }

    @GetMapping("/queryCostCount")
    public Response<TracePressureCostDTO> getCostCount(TraceCostCountQueryParam queryParam) {
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(count) as count");
        sql.append(" from t_engine_pressure_all");
        sql.append(" where time>="+queryParam.getStartTime());
        sql.append(" and time<="+queryParam.getEndTime());
        sql.append(" and job_id='"+queryParam.getJobId()+"'");
        sql.append(" and transaction='"+queryParam.getTransaction()+"'");
        sql.append(" and avg_rt>=" + queryParam.getMinCost());
        sql.append(" and avg_rt<" + queryParam.getMaxCost());
        log.info("TracePressureController#getCostCount execute sql={}", sql);
        List<TracePressureCostDTO> dataList = clickHouseSupport.queryForList(sql.toString(), TracePressureCostDTO.class);
        return CollectionUtils.isNotEmpty(dataList) ? Response.success(dataList.get(0)) : Response.success(new TracePressureCostDTO());
    }
}
