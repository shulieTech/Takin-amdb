package io.shulie.amdb.common.request.trace;

import lombok.Data;

@Data
public class TraceCostCountQueryParam extends TracePressureQueryParam {

    private Integer minCost;

    private Integer maxCost;

}
