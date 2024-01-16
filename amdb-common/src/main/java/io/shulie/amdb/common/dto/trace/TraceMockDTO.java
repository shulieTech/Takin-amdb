package io.shulie.amdb.common.dto.trace;

import lombok.Data;

import java.io.Serializable;

@Data
public class TraceMockDTO implements Serializable {

    private String appName;
    private String serviceName;
    private String methodName;
    private String resultCode;
    private Long count;
    private Double totalCost;
}
