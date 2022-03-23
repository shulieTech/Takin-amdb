package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.ActivityServiceDO;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface ActivityServiceMapper extends Mapper<ActivityServiceDO>, MySqlMapper<ActivityServiceDO> {

    int batchInsert(List<ActivityServiceDO> records);

    void batchUpdateCostIndicators(@Param("serviceList") List<ActivityServiceDO> doList);
}
