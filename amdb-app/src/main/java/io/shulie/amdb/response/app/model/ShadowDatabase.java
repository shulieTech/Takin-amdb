package io.shulie.amdb.response.app.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ShadowDatabase implements Serializable {
    private static final long serialVersionUID = -7067490465362830898L;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("业务数据源")
    private String dataSource;

    @ApiModelProperty("中间件类型")
    private String middleType;

    @ApiModelProperty("连接池名称")
    private String connectionPool;

    @ApiModelProperty("附加信息")
    private String extInfo;

    @ApiModelProperty("动态配置")
    private String attachment;
}
