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

    @Value("${tro.datasource.druid.initialSize:1}")
    private int initialSize;
    @Value("${tro.datasource.druid.minIdle:1}")
    private int minIdle;
    @Value("${tro.datasource.druid.maxActive:20}")
    private int maxActive;
    @Value("${spring.datasource.druid.maxWait}")
    private long maxWait;
    @Value("${spring.datasource.druid.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${spring.datasource.druid.validationQuery}")
    private String validationQuery;
    @Value("${spring.datasource.druid.testWhileIdle}")
    private boolean testWhileIdle;
    @Value("${spring.datasource.druid.testOnReturn}")
    private boolean testOnReturn;
    @Value("${spring.datasource.druid.testOnBorrow}")
    private boolean testOnBorrow;
    @Value("${spring.datasource.druid.poolPreparedStatements}")
    private boolean poolPreparedStatements;

    @Bean("troDataSource")
    public DataSource troDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setPoolPreparedStatements(poolPreparedStatements);
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