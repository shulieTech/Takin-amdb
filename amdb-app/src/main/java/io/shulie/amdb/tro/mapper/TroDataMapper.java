package io.shulie.amdb.tro.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author sunshiyu
 * @description 应用探针配置查询mapper
 * @datetime 2021-09-27 7:49 下午
 */
public interface TroDataMapper {
    @Select("select default_value from t_agent_config where user_app_key = #{tenantAppKey} and env_code = #{envCode} and project_name=#{appName} and en_key=#{configKey} order by type desc limit 1")
    String queryConfigValueByParams(@Param("tenantAppKey") String tenantAppKey, @Param("envCode") String envCode, @Param("appName") String appName, @Param("configKey") String configKey);

    @Select("select default_value from t_agent_config where type=6 and user_app_key = #{tenantAppKey} and env_code = #{envCode} and en_key=#{configKey} limit 1")
    String queryTenantConfigValueByParams(@Param("tenantAppKey") String tenantAppKey, @Param("envCode") String envCode, @Param("configKey") String configKey);

    @Select("select default_value from t_agent_config where type=0 and en_key=#{configKey} limit 1")
    String queryGlobalConfigValueByParams(@Param("configKey") String configKey);

}
