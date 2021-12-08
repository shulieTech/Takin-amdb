package io.shulie.amdb.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
@Table(name = "`t_amdb_app_shadowdatabase`")
public class AppShadowDatabaseDO extends BaseDatabaseDO {

    private static final long serialVersionUID = 7223051889499533896L;

    @Id
    @Column(name = "`id`")
    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("应用名称")
    @Column(name = "`app_name`")
    private String appName;

    @ApiModelProperty("业务数据源")
    @Column(name = "`data_source`")
    private String dataSource;

    @ApiModelProperty("业务数据源")
    @Column(name = "`shadow_data_source`")
    private String shadowDataSource;

    @ApiModelProperty("数据库类型名称")
    @Column(name = "`db_name`")
    private String dbName;

    @ApiModelProperty("用户名")
    @Column(name = "`table_user`")
    private String tableUser;

    @ApiModelProperty("用户密码")
    @Column(name = "`password`")
    private String password;

    @ApiModelProperty("中间件类型")
    @Column(name = "`middleware_type`")
    private String middlewareType;

    @ApiModelProperty("连接池名称")
    @Column(name = "`connection_pool`")
    private String connectionPool;

    @ApiModelProperty("附加信息")
    @Column(name = "`ext_info`")
    private String extInfo;

    @ApiModelProperty("类型")
    @Column(name = "`type`")
    private String type;

    @ApiModelProperty("动态配置")
    @Column(name = "`attachment`")
    private String attachment;

    @ApiModelProperty("unique_key")
    @Column(name = "`unique_key`")
    private String uniqueKey;

    @Column(name = "`gmt_create`")
    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    @Column(name = "`gmt_modify`")
    @ApiModelProperty("更新时间")
    private Date gmtModify;
}
