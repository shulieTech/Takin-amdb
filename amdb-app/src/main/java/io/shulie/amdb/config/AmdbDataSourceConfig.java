package io.shulie.amdb.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

/**
 * @author sunshiyu
 * @description AMDB查询主数据源
 * @datetime 2021-09-27 8:09 下午
 */
@Configuration
@MapperScan(basePackages = AmdbDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "amdbSqlSessionFactory")
class AmdbDataSourceConfig {
    static final String PACKAGE = "io.shulie.amdb.mapper";
    static final String MAPPER_LOCATION = "classpath:generator/*.xml";

    /**
     * 连接数据库信息
     */
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean("amdbDataSource")
    @Primary
    public DataSource amdbDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "amdbTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(amdbDataSource());
    }

    @Bean(name = "amdbSqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("amdbDataSource") DataSource masterDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(masterDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(AmdbDataSourceConfig.MAPPER_LOCATION));

        return sessionFactory.getObject();
    }
}