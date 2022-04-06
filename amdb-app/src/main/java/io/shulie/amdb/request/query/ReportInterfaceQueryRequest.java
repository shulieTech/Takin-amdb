package io.shulie.amdb.request.query;

import java.util.List;

import lombok.Data;

@Data
public class ReportInterfaceQueryRequest {

    private String reportId;
    private List<InterfaceParam> params;

    @Data
    public static class InterfaceParam {
        private String entranceAppName;
        private String entranceServiceName;
        private String entranceMethodName;
        private String entranceRpcType;
        private String appName;
        private String serviceName;
        private String methodName;
        private String rpcType;
    }
}
