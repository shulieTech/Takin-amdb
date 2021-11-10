package io.shulie.amdb.entity;

import java.io.Serializable;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseDatabaseDO implements Serializable {

    /**
     * 租户标识
     */
    @ApiModelProperty("租户标识")
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
    @ApiModelProperty("环境标识")
    @Column(name = "`user_id`")
    private String userId;
}
