package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.sto.StoMetric;
import io.shulie.amdb.common.dto.sto.StoQueryDTO;
import io.shulie.amdb.common.request.sto.StoQueryRequest;
import io.shulie.amdb.dao.ITraceDao;
import io.shulie.amdb.service.StoService;
import io.shulie.amdb.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class StoServiceImpl implements StoService {
    @Autowired
    @Qualifier("traceDaoImpl")
    ITraceDao traceDao;


    @Override
    public Response<StoQueryDTO> getServiceMetrics(StoQueryRequest request) {
        StoQueryDTO stoQueryDTO = new StoQueryDTO();
        try {
            StringBuilder condition = new StringBuilder();
            condition.append(" appName = '" + request.getAppName() + "'");
            condition.append(" and startDate >= '" + request.getStartTime() + "'");
            condition.append(" and startDate <= '" + request.getEndTime() + "'");
            if (StringUtils.isNotBlank(request.getServiceName())) {
                condition.append(" and parsedServiceName = '" + request.getServiceName() + "'");
            }
            if (StringUtils.isNotBlank(request.getMethodName())) {
                condition.append(" and parsedMethod = '" + request.getMethodName() + "'");
            }
            //只需要入口和服务端日志,代表该应用所提供的服务
            //目前只提供http和rpc类型的服务查询,redis、db、es等暂不提供
            condition.append(" and rpcType in ('0','1') and logType in ('1','3')");

            //这里的平均tps有可能小于1,因为有的接口本身调用量就少,如果时间范围跨度较大,数据就会被平均
            StringBuilder metricQuerySql = new StringBuilder();
            metricQuerySql.append("select appName,parsedServiceName,parsedMethod,parsedMiddlewareName,sum(toInt32(samplingInterval)) as invokeCount,sum(cost) as totalCost,count(1) as count,toDecimal32(invokeCount/" + request.getInterval() + ",3) as tps,toDecimal32((totalCost/count),3) as rt,concat(appName,parsedServiceName,parsedMethod,parsedMiddlewareName) as key from t_trace_all where");
            metricQuerySql.append(condition);
            metricQuerySql.append(" group by appName,parsedServiceName,parsedMethod,parsedMiddlewareName");
            if (request.getInvokeCount() > 0) {
                metricQuerySql.append(" having invokeCount > " + request.getInvokeCount());
            }
            List<Map<String, Object>> metricsList = traceDao.queryForList(metricQuerySql.toString());

            //取成功调用次数
            StringBuilder successCountQuerySql = new StringBuilder();
            successCountQuerySql.append("select appName,parsedServiceName,parsedMethod,parsedMiddlewareName,count(1) as count,concat(appName,parsedServiceName,parsedMethod,parsedMiddlewareName) as key from t_trace_all where");
            successCountQuerySql.append(condition);
            //和surge-deploy中的计算逻辑保持一致,resultCode为空、00或者200的都认为成功
            successCountQuerySql.append(" and resultCode in ('','00','200') ");
            successCountQuerySql.append(" group by appName,parsedServiceName,parsedMethod,parsedMiddlewareName");
            List<Map<String, Object>> successCountList = traceDao.queryForList(successCountQuerySql.toString());

            List<StoMetric> result = new ArrayList<>();
            metricsList.forEach(tmp -> {
                StoMetric stoMetric = new StoMetric();
                stoMetric.setServiceName(StringUtil.parseStr(tmp.get("parsedServiceName")));
                stoMetric.setMethodName(StringUtil.parseStr(tmp.get("parsedMethod")));
                stoMetric.setMiddleWareType(StringUtil.parseStr(tmp.get("parsedMiddlewareName")));
                stoMetric.setInvokeCount(Long.parseLong(StringUtil.parseStr(tmp.get("invokeCount"))));
                stoMetric.setTps(Double.parseDouble(StringUtil.parseStr(tmp.get("tps"))));
                stoMetric.setRt(Double.parseDouble(StringUtil.parseStr(tmp.get("rt"))));

                String key = StringUtil.parseStr(tmp.get("key"));
                AtomicReference<Double> successCount = new AtomicReference<>(0.0);
                successCountList.forEach(value -> {
                    if (value.containsValue(key)) {
                        successCount.set(Double.parseDouble(StringUtil.parseStr(value.get("count"))));
                    }
                });
                Double totalCount = Double.parseDouble(StringUtil.parseStr(tmp.get("count")));
                //计算成功率
                stoMetric.setSuccessRate(successCount.get() / totalCount);
                result.add(stoMetric);
            });
            stoQueryDTO.setAppName(result);
        } catch (Exception e) {
            log.error("查询申通服务指标发生异常{},异常堆栈:{}", e, e.getStackTrace());
        }
        return new Response<>(stoQueryDTO);
    }
}
