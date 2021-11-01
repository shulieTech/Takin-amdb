package io.shulie.amdb.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

/**
 * @author sunshiyu
 * @description 新增控制台数据源
 * @datetime 2021-09-27 8:09 下午
 */
@Configuration
@MapperScan(basePackages = TroDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "troSqlSessionFactory")
class TroDataSourceConfig {
    static final String PACKAGE = "io.shulie.amdb.tro.mapper";
    static final String MAPPER_LOCATION = "classpath:generator/tro/*.xml";

    /**
     * 连接数据库信息
     */
    @Value("${tro.datasource.url}")
    private String url;

    @Value("${tro.datasource.username}")
    private String username;

    @Value("${tro.datasource.password}")
    private String password;

    @Value("${tro.datasource.driver-class-name}")
    private String driverClassName;

    @Bean("troDataSource")
    public DataSource troDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "troTransactionManager")
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(troDataSource());
    }

    @Bean(name = "troSqlSessionFactory")
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("troDataSource") DataSource masterDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(masterDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(TroDataSourceConfig.MAPPER_LOCATION));

        return sessionFactory.getObject();
    }
}