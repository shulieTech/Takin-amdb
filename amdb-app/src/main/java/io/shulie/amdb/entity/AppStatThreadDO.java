package io.shulie.amdb.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/9 15:50
 */
@Data
public class AppStatThreadDO implements Serializable {
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
     * 线程次数
     */
    private Integer threadCount;

    /**
     * 新建线程数
     */
    private Integer threadNewCount;

    /**
     * 死锁线程个数
     */
    private Integer threadDeadlockCount;

    /**
     * 运行线程个数
     */
    private Integer threadRunnableCount;

    /**
     * 终结线程个数
     */
    private Integer threadTerminatedCount;

    /**
     * timed_waiting 线程个数
     */
    private Integer threadTimedWaitCount;

    /**
     * 等待线程个数
     */
    private Integer threadWaitCount;

    /**
     * 阻塞线程个数
     */
    private Integer threadBlockedCount;
}
