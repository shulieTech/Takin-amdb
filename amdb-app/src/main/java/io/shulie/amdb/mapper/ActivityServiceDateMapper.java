package io.shulie.amdb.mapper;

import java.util.Collection;
import java.util.List;

import io.shulie.amdb.entity.ActivityServiceDO;
import io.shulie.amdb.entity.ActivityServiceDateDO;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface ActivityServiceDateMapper extends Mapper<ActivityServiceDateDO>, MySqlMapper<ActivityServiceDateDO> {

    @Options(useGeneratedKeys = true)
    int insertList(@Param("serviceList") List<? extends ActivityServiceDateDO> records);

    List<ActivityServiceDO> statisticalIndicators(@Param("serviceIds") Collection<Long> values);
}
