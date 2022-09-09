package io.shulie.amdb.common.dto.link.topology;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingchen
 * @description: TODO
 * @date 2022/9/9 10:24 AM
 */
@Data
public class AppShadowDatabaseDTO {
    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("业务数据源")
    private String dataSource;

    @ApiModelProperty("业务数据源")
    private String shadowDataSource;

    @ApiModelProperty("数据库类型名称")
    private String dbName;

    @ApiModelProperty("用户名")
    private String tableUser;

    @ApiModelProperty("用户密码")
    private String password;

    @ApiModelProperty("中间件类型")
    private String middlewareType;

    @ApiModelProperty("连接池名称")
    private String connectionPool;

    @ApiModelProperty("附加信息")
    private String extInfo;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("动态配置")
    private String attachment;

    @ApiModelProperty("unique_key")
    private String uniqueKey;
}
