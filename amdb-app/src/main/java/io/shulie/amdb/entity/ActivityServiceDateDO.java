package io.shulie.amdb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务活动服务接口5s metrics
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_amdb_report_service_metrics")
public class ActivityServiceDateDO extends ActivityTaskBased {

    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 起始时间
     */
    @Column(name = "start_date")
    private Date startDate;

    /**
     * 结束时间
     */
    @Column(name = "end_date")
    private Date endDate;

    /**
     * 最小自耗时 traceId
     */
    @Column(name = "min_cost_traceId")
    private String minCostTraceId;

    /**
     * 最大自耗时 traceId
     */
    @Column(name = "max_cost_traceId")
    private String maxCostTraceId;

    /**
     * 业务活动服务接口Id
     */
    @Column(name = "service_id")
    private Long serviceId;

    /**
     * 探针Id
     */
    @Column(name = "agent_id")
    private String agentId;
}
