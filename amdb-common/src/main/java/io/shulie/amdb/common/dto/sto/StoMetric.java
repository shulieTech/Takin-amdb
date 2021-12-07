package io.shulie.amdb.common.dto.sto;

import lombok.Data;

@Data
public class StoMetric {
    private String serviceName;
    private String methodName;
    private String middleWareType;
    private long invokeCount;
    private double tps;
    private double rt;
    private double successRate;
}
