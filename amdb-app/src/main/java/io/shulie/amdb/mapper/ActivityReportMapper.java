package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.ActivityReportDO;
import org.apache.ibatis.annotations.Options;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface ActivityReportMapper extends Mapper<ActivityReportDO>, MySqlMapper<ActivityReportDO> {

    int batchInsert(List<ActivityReportDO> record);
}
