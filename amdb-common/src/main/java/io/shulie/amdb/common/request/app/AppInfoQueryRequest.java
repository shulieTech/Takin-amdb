package io.shulie.amdb.common.request.app;

import io.shulie.amdb.common.request.PagingRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Sunsy
 * @date 2022/1/24
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@ApiModel
@Data
public class AppInfoQueryRequest extends PagingRequest implements Serializable {
    /**
     * 应用名称(在线)
     */
    @ApiModelProperty("应用名")
    private String appName;

    /**
     * 应用状态(正常/异常)
     */
    @ApiModelProperty("应用状态:true[正常],false[异常]")
    private Boolean appStatus;

    /**
     * 应用在线节点数
     */
    @ApiModelProperty("应用在线节点数")
    private long onlineInstanceCount;

    /**
     * 应用接入时间
     */
    @ApiModelProperty("应用接入时间")
    private long accessTime;
}