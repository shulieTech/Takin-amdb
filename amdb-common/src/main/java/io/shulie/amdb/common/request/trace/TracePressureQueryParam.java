package io.shulie.amdb.common.request.trace;

import lombok.Data;

import java.io.Serializable;

@Data
public class TracePressureQueryParam implements Serializable {

    private String startTime;

    private String endTime;

    private String jobId;

    private String serviceName;

    private String requestMethod;

    private String resultCode;
}
