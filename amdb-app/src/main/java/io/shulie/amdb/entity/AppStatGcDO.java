package io.shulie.amdb.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/9 15:50
 */
@Data
public class AppStatGcDO implements Serializable {
    private final static long serialVersionUID = 1L;
    /**
     * 应用名称
     */
    private String appName;

    /**
     * ip 名称
     */
    private String ip;

    /**
     * tenant key
     */
    private String tenantKey;

    /**
     * 环境
     */
    private String envCode;

    /**
     * younggc 次数
     */
    private Integer youngGcCount;

    /**
     * old gc 次数
     */
    private Integer oldGcCount;

    /**
     * younggc 时间
     */
    private Long youngGcTime;

    /**
     * old gc 时间
     */
    private Long oldGcTime;


}
