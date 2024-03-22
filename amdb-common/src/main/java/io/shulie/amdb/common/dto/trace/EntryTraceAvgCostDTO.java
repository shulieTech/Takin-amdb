package io.shulie.amdb.common.dto.trace;

import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangz
 * Created on 2024/3/13 19:17
 * Email: zz052831@163.com
 */

@Data
public class EntryTraceAvgCostDTO {
    private String traceId;
    private String appName;
    private String serviceName;
    private String methodName;
    private String middlewareName;
    private String rpcId;
    private Integer logType;
    private String rpcType;
    private BigDecimal avgCost;
    private BigDecimal failureCount;
    private BigDecimal successCount;
    private BigDecimal totalCount;
    private Integer samplingInterval;
    private BigDecimal successRate;
}