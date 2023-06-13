package io.shulie.amdb.request.query;

import java.util.List;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/13 10:59
 */
public class CommonMetricsQueryRequest {
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 环境编码
     */
    private String envCode;
    /**
     * 间隔时长
     */
    private Long intervalSec;
    /**
     * 指标名称
     */
    private String metric;

    /**
     * 筛选条件
     */
    private List<QueryMetricsFilters> filters;

    /**
     * 类型，SUM,AVG
     */
    private String calcType;

    /**
     * 查询指标对应的测量维度
     */
    private List<String> dimensions;

    /**
     * 查询指标对应的测量数据
     */
    private List<String> measures;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    public String getCalcType() {
        return calcType;
    }

    public void setCalcType(String calcType) {
        this.calcType = calcType;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public Long getIntervalSec() {
        return intervalSec;
    }

    public void setIntervalSec(Long intervalSec) {
        this.intervalSec = intervalSec;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public List<QueryMetricsFilters> getFilters() {
        return filters;
    }

    public void setFilters(List<QueryMetricsFilters> filters) {
        this.filters = filters;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getMeasures() {
        return measures;
    }

    public void setMeasures(List<String> measures) {
        this.measures = measures;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
