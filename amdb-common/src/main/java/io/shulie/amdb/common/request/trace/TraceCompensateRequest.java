package io.shulie.amdb.common.request.trace;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Sunsy
 * @date 2022/5/19
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class TraceCompensateRequest {
    @ApiModelProperty("场景ID")
    private Long resourceId;
    @ApiModelProperty("报告ID")
    private Long jobId;
    @ApiModelProperty("回调地址")
    private String callbackUrl;

}
