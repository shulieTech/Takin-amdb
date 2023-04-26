package io.shulie.amdb.common.dto.trace;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TracePressureOneTraceDTO implements Serializable {

    @ApiModelProperty("traceId")
    private String traceId;

    @ApiModelProperty("请求耗时，单位ms")
    private String cost;

    @ApiModelProperty("请求时间")
    private String startDate;

    @ApiModelProperty("请求")
    private String request;

    @ApiModelProperty("响应")
    private String response;
}
