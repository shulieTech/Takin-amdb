package io.shulie.amdb.response.app.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ShadowBizTable {

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("业务表")
    private String tableName;

    @ApiModelProperty("影子表")
    private String shadowTableName;
}