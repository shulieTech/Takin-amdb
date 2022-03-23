package io.shulie.amdb.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务活动服务接口metrics
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_amdb_report_service")
public class ActivityServiceDO extends ActivityTaskBased {

    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 活动Id
     */
    @Column(name = "activity_id")
    private Long activityId;

    /**
     * 自耗时占比
     */
    @Column(name = "cost_percent")
    private BigDecimal costPercent;

    /**
     * 业务活动平均耗时
     */
    @Column(name = "activity_sum_cost")
    private Long activitySumCost;
}
