package io.shulie.amdb.common.request.trodata;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sunshiyu
 * @description 应用探针配置查询入参
 * @datetime 2021-09-27 7:39 下午
 */
@Data
@ApiModel
public class TrodataQueryParam {

    @ApiModelProperty("租户标识")
    private String userAppKey;

    @ApiModelProperty("环境标识")
    private String envCode;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("配置key")
    private String configKey;

}
