package io.shulie.amdb.common.dto.waterline;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TendencyChart extends WaterlineMetrics {
    private double totalCount;
    private String disk = "0";
    private String net = "0";
    private String hostIp;
    private double totalTps;
}
