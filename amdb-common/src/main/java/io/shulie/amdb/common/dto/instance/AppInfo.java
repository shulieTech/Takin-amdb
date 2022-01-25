package io.shulie.amdb.common.dto.instance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Sunsy
 * @date 2022/1/24
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Data
@ApiModel("应用信息")
public class AppInfo {
    /**
     * 应用名称(在线)
     */
    @ApiModelProperty("应用名")
    private String appName;

    /**
     * 异常应用实例个数
     */
    @ApiModelProperty("应用异常实例个数,为0代表应用状态正常,反之则为异常")
    private long exceptionCount;

    /**
     * 应用在线节点数
     */
    @ApiModelProperty("应用在线节点个数")
    private long onlineInstanceCount;

    /**
     * 应用接入时间
     */
    @ApiModelProperty("应用接入时间")
    private String accessTime;

}
