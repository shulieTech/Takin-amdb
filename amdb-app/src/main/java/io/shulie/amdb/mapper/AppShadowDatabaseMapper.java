package io.shulie.amdb.mapper;

import java.util.List;

import io.shulie.amdb.entity.AppShadowBizTableDO;
import io.shulie.amdb.entity.AppShadowDatabaseDO;
import io.shulie.amdb.request.query.AppShadowBizTableRequest;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface AppShadowDatabaseMapper extends Mapper<AppShadowDatabaseDO>, MySqlMapper<AppShadowDatabaseDO> {

    @Select("<script>" +
        "select id, app_name as appName, `table_name` as tableName, biz_database as bizDatabase,table_user as tableUser, "
        + " unique_key as uniqueKey, gmt_create as gmtCreate, gmt_modify as gmtModify "
        + " from t_amdb_app_shadowbiztable where app_name = #{appName} and data_source = #{dataSource} and table_user = #{tableUser}"
        + " <if test='tenantAppKey != null'> and user_app_key = #{tenantAppKey} </if>"
        + " <if test='envCode != null'> and env_code = #{envCode} </if>"
        + " </script>")
    List<AppShadowBizTableDO> selectShadowBizTables(AppShadowBizTableRequest request);
}