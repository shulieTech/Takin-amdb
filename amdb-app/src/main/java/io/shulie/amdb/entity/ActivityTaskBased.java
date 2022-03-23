package io.shulie.amdb.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ActivityTaskBased implements Serializable {

    @Column(name = "`task_id`")
    private String taskId;

    /**
     * 应用名称
     */
    @Column(name = "`app_name`")
    private String appName;

    /**
     * 服务名称
     */
    @Column(name = "`service_name`")
    private String serviceName;

    /**
     * 方法名称
     */
    @Column(name = "`method_name`")
    private String methodName;

    /**
     * rpcType
     */
    @Column(name = "`rpc_type`")
    private String rpcType;

    /**
     * 最小耗时
     */
    @Column(name = "`min_cost`")
    private Long minCost;

    /**
     * 最大耗时
     */
    @Column(name = "`max_cost`")
    private Long maxCost;

    /**
     * 总耗时
     */
    @Column(name = "`sum_cost`")
    private Long sumCost;


    /**
     * 请求数
     */
    @Column(name = "`req_cnt`")
    private Long reqCnt;

    /**
     * 平均自耗时
     */
    @Column(name = "`avg_cost`")
    private BigDecimal avgCost;

    @Column(name = "gmt_create")
    private Date gmtCreate;
}
