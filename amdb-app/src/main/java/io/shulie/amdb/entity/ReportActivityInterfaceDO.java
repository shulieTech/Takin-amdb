package io.shulie.amdb.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_amdb_report_interface")
public class ReportActivityInterfaceDO extends ReportBased {

    /**
     * 入口应用名称
     */
    @Column(name = "entrance_app_name")
    private String entranceAppName;
    /**
     * 入口接口名称
     */
    @Column(name = "entrance_service_name")
    private String entranceServiceName;
    /**
     * 入口方法名称
     */
    @Column(name = "entrance_method_name")
    private String entranceMethodName;
    /**
     * 入口rpcType
     */
    @Column(name = "entrance_rpc_type")
    private String entranceRpcType;
    /**
     * 耗时占比
     */
    @Column(name = "cost_percent")
    private BigDecimal costPercent;
    /**
     * 业务活动平均耗时
     */
    @Column(name = "service_avg_cost")
    private BigDecimal serviceAvgCost;

    /**
     * 处理状态：0-未处理完成，1-处理完成
     */
    @Column(name = "state")
    private Integer state;

    @Column(name = "`gmt_update`")

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtUpdate;
}
