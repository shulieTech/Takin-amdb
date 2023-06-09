package io.shulie.amdb.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/9 15:50
 */
@Data
public class AppStatLogDO implements Serializable {
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
     * 错误类型
     */
    private String errorType;

    /**
     * tenant key
     */
    private String tenantKey;

    /**
     * 环境
     */
    private String envCode;

    /**
     * 错误
     */
    private String errorContent;

    /**
     * 错误次数
     */
    private Integer errorCount;
}
