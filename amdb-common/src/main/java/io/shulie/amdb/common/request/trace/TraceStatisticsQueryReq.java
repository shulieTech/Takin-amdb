package io.shulie.amdb.common.request.trace;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhangz
 * Created on 2024/3/13 15:44
 * Email: zz052831@163.com
 */

@Data
public class TraceStatisticsQueryReq extends AbstractAmdbBaseRequest {
    @ApiModelProperty("接口名称")
    String serviceName;
    @ApiModelProperty("方法名称")
    String methodName;
    @ApiModelProperty("应用名称")
    String appName;
    @ApiModelProperty("查询开始时间")
    private String startTime;
    @ApiModelProperty("查询结束时间")
    private String endTime;
}
