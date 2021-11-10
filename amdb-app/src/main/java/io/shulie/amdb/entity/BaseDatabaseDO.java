package io.shulie.amdb.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class BaseDatabaseDO implements Serializable {

    /**
     * 租户标识
     */
    @ApiModelProperty("租户标识")
    @Column(name = "`user_app_key`")
    private String userAppKey;

    /**
     * 环境标识
     */
    @ApiModelProperty("环境标识")
    @Column(name = "`env_code`")
    private String envCode;
    /**
     * 用户Id
     */
    @ApiModelProperty("用户Id")
    @Column(name = "`user_id`")
    private String userId;
}
