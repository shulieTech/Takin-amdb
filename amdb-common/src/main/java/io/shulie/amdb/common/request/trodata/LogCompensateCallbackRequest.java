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
    private final Integer type = 302; // 固定 302
    private final String source = "amdb"; // 来源：amdb、cloud [ 不传默认cloud ]
    private LogCompensateCallbackData data;

    public LogCompensateCallbackRequest(LogCompensateCallbackData data) {
        this.data = data;
    }

    @Data
    public static class LogCompensateCallbackData {
        private String resourceId;
        private Long pressureId;
        private String content;
        private Boolean completed;
    }
}
