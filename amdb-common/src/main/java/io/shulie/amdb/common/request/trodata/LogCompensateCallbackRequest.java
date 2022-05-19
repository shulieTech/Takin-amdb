package io.shulie.amdb.common.request.trodata;

import lombok.Data;

/**
 * @author Sunsy
 * @date 2022/5/19
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
public class LogCompensateCallbackRequest {
    private Integer type = 302; // 固定 302
    private String source = "amdb"; // 来源：amdb、cloud [ 不传默认cloud ]
    private String resourceId;
    private Long jobId;
    private String content;
    private Boolean completed;
}
