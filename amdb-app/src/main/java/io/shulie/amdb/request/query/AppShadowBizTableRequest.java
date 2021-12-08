package io.shulie.amdb.request.query;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class AppShadowBizTableRequest extends AbstractAmdbBaseRequest {

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("业务数据源")
    private String dataSource;

    @ApiModelProperty("用户名")
    private String tableUser;
}
