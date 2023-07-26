package io.shulie.amdb.common.request.trace;

import lombok.Data;

import java.io.Serializable;

@Data
public class TraceCostCountQueryParam implements Serializable {

    private Long startTime;

    private Long endTime;

    private Long jobId;

    private Integer minCost;

    private Integer maxCost;

    private String transaction;

}
