package io.shulie.amdb.entity;

import javax.persistence.Column;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_amdb_interface_metrics")
public class ReportInterfaceMetricsDO extends ReportBased {

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

    @Column(name = "time_window")
    private String timeWindow;

    @Column(name = "count_after_simp")
    private Long countAfterSimp;
}
