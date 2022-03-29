package io.shulie.amdb.common.dto.waterline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterlineMetrics {
    private String applicationName;
    private String cpuLoad;
    private String memory;
}
