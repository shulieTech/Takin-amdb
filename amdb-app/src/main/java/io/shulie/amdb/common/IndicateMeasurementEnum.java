package io.shulie.amdb.common;

import org.apache.commons.lang.StringUtils;

/**
 * 指标名与 measurement 的对应关系枚举
 *
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/9 15:40
 */
public enum IndicateMeasurementEnum {

    /**
     * 应用 gc 指标
     */
    APP_STAT_GC("appstat.gc", "app_stat_gc"),
    /**
     * 应用的 thread 指标
     */
    APP_STAT_THREAD("appstat.thread", "app_stat_thread"),

    /**
     * 应用的节点调用指标
     */
    APP_STAT_INCALL("appstat.incall", "trace_metrics"),

    /**
     * 应用的日志指标
     */
    APP_STAT_LOG("appstat.log", "app_stat_log");
    private String indName;
    private String measurementName;

    IndicateMeasurementEnum(String indName, String measurementName) {
        this.indName = indName;
        this.measurementName = measurementName;
    }

    public String getIndName() {
        return indName;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public static IndicateMeasurementEnum get(String indName) {
        for (IndicateMeasurementEnum indicateMeasurementEnum : IndicateMeasurementEnum.values()) {
            if (StringUtils.equals(indicateMeasurementEnum.getIndName(), indName)) {
                return indicateMeasurementEnum;
            }
        }
        return null;
    }
}
