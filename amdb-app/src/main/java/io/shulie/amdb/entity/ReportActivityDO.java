package io.shulie.amdb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_amdb_report_activity")
public class ReportActivityDO extends ReportBased {

    /**
     * 处理状态：0-未处理完成，1-处理完成
     */
    @Column(name = "`state`")
    private Integer state;

    @Column(name = "`gmt_update`")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtUpdate;
}
