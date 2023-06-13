package io.shulie.amdb.request.query;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/13 11:05
 */
public class QueryMetricsFilters {
    /**
     * 过滤字段名称
     */
    private String key;
    /**
     * 过滤字段值
     */
    private Object value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
