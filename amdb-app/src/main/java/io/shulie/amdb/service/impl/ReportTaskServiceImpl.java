package io.shulie.amdb.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.amdb.common.enums.RpcType;
import io.shulie.amdb.dao.ITraceDao;
import io.shulie.amdb.entity.ActivityReportDO;
import io.shulie.amdb.entity.ActivityServiceDO;
import io.shulie.amdb.entity.ActivityServiceDateDO;
import io.shulie.amdb.mapper.ActivityReportMapper;
import io.shulie.amdb.mapper.ActivityServiceDateMapper;
import io.shulie.amdb.mapper.ActivityServiceMapper;
import io.shulie.amdb.service.ReportTaskService;
import io.shulie.amdb.utils.ReportTaskTraceParser;
import io.shulie.surge.data.common.pool.NamedThreadFactory;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ReportTaskServiceImpl implements ReportTaskService {

    @Autowired
    @Qualifier("traceDaoImpl")
    private ITraceDao traceDao;

    @Resource
    private ActivityReportMapper activityReportMapper;

    @Resource
    private ActivityServiceMapper activityServiceMapper;

    @Resource
    private ActivityServiceDateMapper activityServiceDateMapper;

    /**
     * 统计sql
     */
    private static final String ACTIVITY_STATISTICS_SQL =
        "select sum(cost) sumCost, max(cost) maxCost, min(cost) minCost, count(1) reqCnt, divide(sumCost, reqCnt) avgCost, "
            + " appName, '%s' taskId, parsedServiceName serviceName, parsedMethod methodName, rpcType "
            + " from (select appName, parsedServiceName, parsedMethod, rpcType, min(cost) cost from t_trace_all "
            + " where traceId in (select DISTINCT traceId from t_trace_pressure where taskId= '%s'"
            + " ) and logType= '1' group by traceId, appName, parsedServiceName, parsedMethod, rpcType) "
            + " group by appName, parsedServiceName, parsedMethod, rpcType";

    /**
     * 查询压测报告时间范围
     */
    private static final String DATE_RANGE_SQL =
        "select min(startDate) minDate, subtractSeconds(min(startDate), 1) minDateBefore, "
            + " max(startDate) maxDate, addSeconds(max(startDate), 1) maxDateAfter from t_trace_all"
            + " where traceId in (select DISTINCT traceId from t_trace_pressure where taskId= '%s') ";

    /**
     * 查询指定压测报告中对应业务活动中各服务接口的请求数
     */
    private static final String SERVICE_STATISTICS_SQL
        = "select appName, parsedServiceName serviceName, parsedMethod methodName, rpcType, "
        + " sum(toInt32(samplingInterval)) reqCnt from "
        + " (select DISTINCT appName, parsedServiceName, parsedMethod, rpcType, traceId, samplingInterval "
        + " from t_trace_all where traceId in (select DISTINCT traceId from t_trace_all "
        + " where traceId in (select DISTINCT traceId from t_trace_pressure where taskId= '%s') "
        + " AND appName = '%s' AND parsedServiceName = '%s' and parsedMethod = '%s' and rpcType = '%s') "
        + " AND logType in ('" + PradarLogType.LOG_TYPE_TRACE + "', '" + PradarLogType.LOG_TYPE_RPC_SERVER + "') "
        + " AND rpcType in ('" + RpcType.TYPE_WEB_SERVER + "', '" + RpcType.TYPE_RPC + "')) "
        + " group by appName, parsedServiceName, parsedMethod, rpcType";

    /**
     * 查询业务活动对应5s内的所有trace
     */
    private static final String TRACE_SQL =
        "select traceId, appName, parsedServiceName, parsedMethod, rpcType, rpcId, logType, cost, async, agentId"
            + " from t_trace_all where traceId in (select distinct traceId from t_trace_all where traceId in "
            + " (select DISTINCT traceId from t_trace_pressure where taskId = '%s') "
            + " and startDate >= '%s' and startDate < '%s' "
            + " and appName = '%s' and parsedServiceName = '%s' and parsedMethod = '%s' and rpcType = '%s')";

    @Override
    public void startTask(String taskId) {
        // 先计算各业务活动的平均耗时
        List<ActivityReportDO> activities = calcActivityCostAvg(taskId);
        if (CollectionUtils.isEmpty(activities)) {
            return;
        }
        activityReportMapper.batchInsert(activities);
        // 创建线程池进行任务处理
        ExecutorService pool = Executors.newFixedThreadPool(activities.size() + 1,
            new NamedThreadFactory("report-task-" + taskId));
        DateTupleContainer dateTupleContainer = queryDateRange(taskId);
        dateTupleContainer.getDateTuples(); // 触发时间分隔
        activities.forEach(activity -> pool.execute(
            () -> calcActivityServiceMetrics(new ActivityMetricsContext(dateTupleContainer, activity))));
    }

    /**
     * 计算指标
     *
     * @param context 计算上下文
     */
    private void calcActivityServiceMetrics(ActivityMetricsContext context) {
        ActivityReportDO activityDO = context.getActivityDO();
        Long activityId = activityDO.getId();
        String taskId = activityDO.getTaskId();
        Long sumCost = activityDO.getSumCost();
        ActivityParam activityParam = context.getParam();
        List<ActivityServiceDO> serviceDOS = queryActivityService(activityParam);
        if (CollectionUtils.isEmpty(serviceDOS)) {
            return;
        }
        Map<String, Long> serviceMap = new HashMap<>();
        serviceDOS.forEach(service -> {
            service.setActivityId(activityId);
            service.setTaskId(taskId);
            service.setActivitySumCost(sumCost);
        });
        activityServiceMapper.batchInsert(serviceDOS);
        serviceDOS.forEach(service -> serviceMap.putIfAbsent(serviceUniqueKey(service), service.getId()));
        exeMetricsAnalyze(context, serviceMap);
    }

    // 5s的时间区间
    private void exeMetricsAnalyze(ActivityMetricsContext context, Map<String, Long> serviceMap) {
        List<Pair<String, String>> dateTuples = context.getDateTupleContainer().getDateTuples();
        ActivityParam activityParam = context.getParam();
        String taskId = context.getActivityDO().getTaskId();
        List<CompletableFuture<Void>> futures = new ArrayList<>(dateTuples.size());
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        dateTuples.forEach(pair -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Date startDate = DateTime.of(pair.getKey(), "yyyy-MM-dd HH:mm:ss").toJdkDate();
                Date endDate = DateTime.of(pair.getValue(), "yyyy-MM-dd HH:mm:ss").toJdkDate();
                Map<InnerService, ServiceMetrics> metricsMap = dealDateRangeTrace(pair, activityParam);
                List<ActivityServiceDateDO> dateDOList = new ArrayList<>();
                // 保存到数据库
                for (Entry<InnerService, ServiceMetrics> metricsEntry : metricsMap.entrySet()) {
                    InnerService service = metricsEntry.getKey();
                    service.setTaskId(taskId);
                    service.setStartDate(startDate);
                    service.setEndDate(endDate);
                    ServiceMetrics metrics = metricsEntry.getValue();
                    ActivityServiceDateDO dateDO = generateByMetrics(service, metrics, serviceMap);
                    if (Objects.nonNull(dateDO)) {
                        dateDOList.add(dateDO);
                    }
                }
                // 保存到数据库
                activityServiceDateMapper.insertList(dateDOList);
            }, executorService);
            futures.add(future);
        });
        // 更新最大自耗时及平均自耗时
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            List<ActivityServiceDO> doList = activityServiceDateMapper.statisticalIndicators(serviceMap.values());
            activityServiceMapper.batchUpdateCostIndicators(doList);
        });
    }

    private String serviceUniqueKey(ActivityServiceDO service) {
        return service.getAppName() + "@" + service.getServiceName() + "@"
            + service.getMethodName() + "@" + service.getRpcType();
    }

    private String serviceDateUniqueKey(ActivityServiceDateDO service) {
        return service.getAppName() + "@" + service.getServiceName() + "@"
            + service.getMethodName() + "@" + service.getRpcType();
    }

    private ActivityServiceDateDO generateByMetrics(InnerService service, ServiceMetrics metrics,
        Map<String, Long> serviceMap) {
        ActivityServiceDateDO entity = new ActivityServiceDateDO();
        entity.setAppName(service.getAppName());
        entity.setServiceName(service.getServiceName());
        entity.setMethodName(service.getMethodName());
        entity.setRpcType(service.getRpcType());
        String uniqueKey = serviceDateUniqueKey(entity);
        if (serviceMap.containsKey(uniqueKey)) {
            entity.setTaskId(service.getTaskId());
            entity.setServiceId(serviceMap.get(uniqueKey));
            entity.setReqCnt(metrics.getReqCnt());
            entity.setSumCost(metrics.getSumCost());
            entity.setAvgCost(metrics.getAvgCost());
            entity.setMinCost(metrics.getMinCost());
            entity.setMaxCost(metrics.getMaxCost());
            entity.setMinCostTraceId(metrics.getMinTraceId());
            entity.setMaxCostTraceId(metrics.getMaxTraceId());
            entity.setStartDate(service.getStartDate());
            entity.setEndDate(service.getEndDate());
            entity.setAgentId(service.getAgentId());
            return entity;
        }
        return null;
    }

    // 处理5s内的所有trace
    private Map<InnerService, ServiceMetrics> dealDateRangeTrace(Pair<String, String> dateRange, ActivityParam activity) {
        Map<InnerService, ServiceMetrics> result = new HashMap<>();
        String sql = String.format(TRACE_SQL, activity.getTaskId(), dateRange.getKey(), dateRange.getValue()
            , activity.getAppName(), activity.getServiceName(), activity.getMethodName(), activity.getRpcType());
        List<InnerRpcBased> basedList = traceDao.queryForList(sql, InnerRpcBased.class);
        if (CollectionUtils.isEmpty(basedList)) {
            return result;
        }
        Map<String, List<InnerRpcBased>> traceMap = basedList.stream().collect(groupingBy(InnerRpcBased::getTraceId));
        for (Entry<String, List<InnerRpcBased>> entry : traceMap.entrySet()) {
            Map<InnerService, ServiceMetrics> selfCost = calcSelfCost(entry.getKey(), entry.getValue());
            for (Entry<InnerService, ServiceMetrics> metricsEntry : selfCost.entrySet()) {
                result.compute(metricsEntry.getKey(), (key, oldValue) -> {
                    ServiceMetrics metrics = metricsEntry.getValue();
                    if (oldValue == null) {
                        return metrics;
                    }
                    oldValue.merge(metrics);
                    return oldValue;
                });
            }
        }
        return result;
    }

    // 处理单个trace
    private Map<InnerService, ServiceMetrics> calcSelfCost(String traceId, List<InnerRpcBased> data) {
        return ReportTaskTraceParser.parseRpcBased(traceId, data);
    }

    /**
     * 查询业务活动对应的服务接口请求数
     *
     * @param param 业务活动
     * @return 服务接口请求数
     */
    private List<ActivityServiceDO> queryActivityService(ActivityParam param) {
        String sql = String.format(SERVICE_STATISTICS_SQL, param.getTaskId(),
            param.getAppName(), param.getServiceName(), param.getMethodName(), param.getRpcType());
        return traceDao.queryForList(sql, ActivityServiceDO.class);
    }

    /**
     * 查询各业务活动的平均耗时
     *
     * @param taskId 压测报告Id
     * @return 各业务活动的平均耗时
     */
    private List<ActivityReportDO> calcActivityCostAvg(String taskId) {
        return traceDao.queryForList(String.format(ACTIVITY_STATISTICS_SQL, taskId, taskId), ActivityReportDO.class);
    }

    /**
     * 查询压测报告日志的时间范围
     *
     * @param taskId 压测报告Id
     * @return 时间范围
     */
    private DateTupleContainer queryDateRange(String taskId) {
        return traceDao.queryForObject(String.format(DATE_RANGE_SQL, taskId), DateTupleContainer.class);
    }

    @Data
    private static class ActivityMetricsContext {
        private DateTupleContainer dateTupleContainer;
        private ActivityReportDO activityDO;
        private ActivityParam param;

        public ActivityMetricsContext(DateTupleContainer dateTupleContainer, ActivityReportDO activityDO) {
            this.dateTupleContainer = dateTupleContainer;
            this.activityDO = activityDO;
        }

        public ActivityParam getParam() {
            if (param == null) {
                ActivityParam activityParam = new ActivityParam();
                activityParam.setTaskId(activityDO.getTaskId());
                activityParam.setAppName(activityDO.getAppName());
                activityParam.setServiceName(activityDO.getServiceName());
                activityParam.setMethodName(activityDO.getMethodName());
                activityParam.setRpcType(activityDO.getRpcType());
                this.param = activityParam;
            }
            return param;
        }
    }

    @Data
    @ToString(exclude = "dateTuples")
    private static class DateTupleContainer {
        private Long minDateBefore;
        // 开始时间
        private Long minDate;
        // 结束时间
        private Long maxDate;
        private Long maxDateAfter;

        private List<Pair<String, String>> dateTuples;

        private static final int TIME_INTERVAL = 5;

        public List<Pair<String, String>> getDateTuples() {
            if (dateTuples == null) {
                initTuples();
            }
            return dateTuples;
        }

        private synchronized void initTuples() {
            Date end = new Date(maxDate);
            List<DateTime> dateTimes = DateUtil.rangeToList(new Date(minDate), end, DateField.SECOND, TIME_INTERVAL);
            dateTimes.set(0, DateTime.of(minDateBefore));
            DateTime maxAfter = DateTime.of(maxDateAfter);
            int lastIndex = dateTimes.size() - 1;
            if (dateTimes.get(lastIndex).isBefore(end)) {
                dateTimes.add(maxAfter);
            } else {
                dateTimes.set(lastIndex, maxAfter);
            }
            int length = dateTimes.size();
            dateTuples = new ArrayList<>(length);
            String preDate = dateTimes.get(1).toString();
            dateTuples.add(Pair.of(dateTimes.get(0).toString(), preDate));
            for (int i = 2; i < length; i++) {
                String curDate = dateTimes.get(i).toString();
                dateTuples.add(Pair.of(preDate, curDate));
                preDate = curDate;
            }
        }
    }

    @Data
    @EqualsAndHashCode(exclude = {"taskId", "startDate", "endDate"})
    public static class InnerService {
        private String agentId;
        private String appName;
        private String serviceName;
        private String methodName;
        private String rpcType;
        private String taskId;
        private Date startDate;
        private Date endDate;

        public InnerService(String agentId, String appName, String serviceName, String methodName, String rpcType) {
            this.agentId = agentId;
            this.appName = appName;
            this.serviceName = serviceName;
            this.methodName = methodName;
            this.rpcType = rpcType;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ServiceMetrics {
        private long minCost;
        private long maxCost;
        private long sumCost;
        private long reqCnt = 1;
        private String minTraceId;
        private String maxTraceId;

        public ServiceMetrics(long cost, String traceId) {
            this.minCost = cost;
            this.maxCost = cost;
            this.sumCost = cost;
            this.minTraceId = traceId;
            this.maxTraceId = traceId;
        }

        public void merge(ServiceMetrics newMetrics) {
            if (newMetrics.getMinCost() < minCost) {
                this.minCost = newMetrics.getMinCost();
                this.minTraceId = newMetrics.getMinTraceId();
            }
            if (newMetrics.getMaxCost() > maxCost) {
                this.maxCost = newMetrics.getMaxCost();
                this.maxTraceId = newMetrics.getMaxTraceId();
            }
            this.sumCost = Math.addExact(sumCost, newMetrics.getSumCost());
            this.reqCnt++;
        }

        public BigDecimal getAvgCost() {
            return new BigDecimal(String.valueOf(sumCost * 1.0 / reqCnt));
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class InnerRpcBased extends RpcBased {
        private String parentRpcId;
        private String parsedServiceName;
        private String parsedMethod;

        @Override
        public void adjust() {
            super.adjust();
            this.parentRpcId = getParentRpcId(getRpcId());
        }

        private static String getParentRpcId(String rpcId) {
            if (StringUtils.isBlank(rpcId)) {
                return null;
            }
            if (StringUtils.indexOf(rpcId, '.') == -1) {
                return null;
            }
            return StringUtils.substring(rpcId, 0, rpcId.lastIndexOf('.'));
        }
    }

    @Data
    public static class ActivityParam {
        /**
         * 压测报告Id
         */
        private String taskId;
        /**
         * 应用名称
         */
        private String appName;
        /**
         * 服务名称
         */
        private String serviceName;
        /**
         * 方法名称
         */
        private String methodName;
        /**
         * rpcType
         */
        private String rpcType;
    }
}
