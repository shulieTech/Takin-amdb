/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.amdb.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.shulie.amdb.dao.ITraceDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service("traceDaoImpl")
public class TraceDaoImpl implements ITraceDao, ApplicationContextAware, InitializingBean {

    private transient ApplicationContext applicationContext;

    private volatile JdbcTemplate jdbcTemplate;

    /**
     * 查询map
     *
     * @param sql
     * @return
     */
    @Override
    public Map<String, Object> queryForMap(String sql) {
        return jdbcTemplate.queryForMap(sql);
    }

    @Override
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }

    /**
     * 查询map
     *
     * @param sql
     * @return
     */
    @Override
    public <T> T queryForObject(String sql, Class<T> clazz) {
        Map<String, Object> result = queryForMap(sql);
        return JSONObject.parseObject(JSON.toJSON(result).toString(), clazz);
    }


    /**
     * 创建对象工厂
     *
     * @param clazz 目标类
     * @param <T>   泛型
     * @return Supplier
     */
    public static <T> Supplier<T> createFactory(Class<T> clazz) {
        try {
            // 提前验证无参构造器是否存在
            clazz.getDeclaredConstructor();
            return () -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create instance for class: " + clazz.getName(), e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Class must have a no-args constructor: " + clazz.getName(), e);
        }
    }

    /**
     * 查询map
     *
     * @param sql
     * @return
     */
    @Override
    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        // 获取查询结果
        List<Map<String, Object>> resultList = queryForList(sql);
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建对象工厂
        Supplier<T> factory = createFactory(clazz);

        // 转换为目标对象列表
        return resultList.stream().map(result -> {
                    T target = factory.get();
                    BeanUtils.copyProperties(result, target);
                    return target;
                }).collect(Collectors.toList());
    }


    /**
     * 查询list
     *
     * @param sql
     * @return
     */
    @Override
    public List<Map<String, Object>> queryForList(String sql) {
        return jdbcTemplate.queryForList(sql);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String datasource = applicationContext.getEnvironment()
                .getProperty("datasource.traceAll", String.class);

        Objects.requireNonNull(datasource);

        switch (datasource.toLowerCase()) {
            case "mysql":
                getSetMysqlAmdbJdbcTemplate();
                break;
            case "clickhouse":
                getSetClickhouseJdbcTemplate();
                break;
            default:
                throw new RuntimeException("unknown datasource " + datasource + " for t_trace_all");
        }
    }

    private void getSetMysqlAmdbJdbcTemplate() {
        this.jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    }

    private void getSetClickhouseJdbcTemplate() {
        this.jdbcTemplate = applicationContext.getBean("clickhouseJdbcTemplate",JdbcTemplate.class);
    }
}
