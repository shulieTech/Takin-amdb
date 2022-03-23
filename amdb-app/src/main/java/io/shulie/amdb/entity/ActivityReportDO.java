package io.shulie.amdb.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务活动metrics
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "`t_amdb_report_activity`")
public class ActivityReportDO extends ActivityTaskBased {

    @Id
    @Column(name = "`id`")
    private Long id;
}
