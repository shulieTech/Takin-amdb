package io.shulie.amdb.common.request.sto;

import lombok.Data;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Data
public class StoQueryRequest {
    private String userAppKey = DigestUtils.md5DigestAsHex("sto".getBytes(StandardCharsets.UTF_8));
    private String appName;
    private String serviceName;
    private String methodName;
    private String startTime;
    private String endTime;
    private long invokeCount;
    private long interval;
}
