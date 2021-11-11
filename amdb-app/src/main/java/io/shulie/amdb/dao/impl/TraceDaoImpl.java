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
import io.shulie.surge.config.clickhouse.ClickhouseTemplateManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("traceDaoImpl")
public class TraceDaoImpl implements ITraceDao {

    /**
     * 查询map
     *
     * @param sql
     * @return
     */
    @Override
    public Map<String, Object> queryForMap(String sql) {
        Map<String, Object> result = ClickhouseTemplateManager.HOLDER.get().getTemplate().queryForMap(sql);
        ClickhouseTemplateManager.HOLDER.remove();
        return result;
    }

    @Override
    public void execute(String sql) {
        ClickhouseTemplateManager.HOLDER.get().getTemplate().execute(sql);
        ClickhouseTemplateManager.HOLDER.remove();
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
     * 查询map
     *
     * @param sql
     * @return
     */
    @Override
    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        List<Map<String, Object>> resultList = queryForList(sql);
        if (resultList == null) {
            return null;
        }
        if (resultList.size() == 0) {
            return new ArrayList<>();
        }
        return resultList.stream().map(result -> JSONObject.parseObject(JSON.toJSON(result).toString(), clazz)).collect(Collectors.toList());
    }


    /**
     * 查询list
     *
     * @param sql
     * @return
     */
    @Override
    public List<Map<String, Object>> queryForList(String sql) {
        List<Map<String, Object>> result = ClickhouseTemplateManager.HOLDER.get().getTemplate().queryForList(sql);
        ClickhouseTemplateManager.HOLDER.remove();
        return result;
    }
}
