package io.shulie.amdb.service.impl;

import com.google.common.collect.Maps;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.InfluxDbQueryRequest;
import io.shulie.amdb.service.InfluxDbQueryService;
import io.shulie.amdb.utils.InfluxDBManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/28
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Service("influxDbQueryServiceImpl")
@Slf4j
public class InfluxDbQueryServiceImpl implements InfluxDbQueryService {
    private static String BLANK = " ";
    private static String TIME_SUFFIX = "000000";

    @Autowired
    private InfluxDBManager influxDbManager;

    @Override
    public Response<List<Map<String, Object>>> queryObjectByConditions(InfluxDbQueryRequest request) {
        List<Map<String, Object>> resultList = Lists.newArrayList();
        String querySql = buildQuerySql(request);
        if (StringUtils.isNotBlank(querySql)) {
            log.info("influxdb query sql:{}", querySql);
            List<QueryResult.Result> queryResult;
            try {
                queryResult = influxDbManager.query(querySql, request.getDatabase());
                List<QueryResult.Series> list = queryResult.get(0).getSeries();
                if (list != null) {
                    for (QueryResult.Series result : list) {
                        List columns = result.getColumns();
                        List values = result.getValues();
                        resultList = getQueryData(columns, values);
                    }
                }
            } catch (Exception e) {
                log.error("query influxdb catch exception:{},{}", e, e.getStackTrace());
                return Response.fail(AmdbExceptionEnums.INFLUXDB_QUERY_SQL_EXECUTE_FAILED, e.getCause());
            }

        }
        return Response.success(resultList);
    }

    /***整理列名、行数据***/
    private List<Map<String, Object>> getQueryData(List<String> columns, List<List<Object>> values) {
        List<Map<String, Object>> lists = new ArrayList<>();
        for (List<Object> list : values) {
            HashMap<String, Object> resultMap = Maps.newHashMap();
            for (int i = 0; i < list.size(); i++) {
                String propertyName = columns.get(i);//字段名
                Object value = list.get(i);//相应字段值
                resultMap.put(propertyName, value);
            }
            lists.add(resultMap);
        }
        return lists;
    }

    private String buildQuerySql(InfluxDbQueryRequest request) {
        StringBuilder sbuilder = new StringBuilder();

        sbuilder.append("select").append(BLANK);
        if (MapUtils.isNotEmpty(request.getAggregateStrategy())) {
            //aggregate fields
            sbuilder.append(parseAliasFields(request.getAggregateStrategy())).append(BLANK);
        }
        if (MapUtils.isNotEmpty(request.getFieldAndAlias())) {
            //non-aggregate fields
            sbuilder.append(parseAliasFields(request.getFieldAndAlias())).append(BLANK);
        }
        if (MapUtils.isEmpty(request.getAggregateStrategy()) && MapUtils.isEmpty(request.getFieldAndAlias())) {
            sbuilder.append("*").append(BLANK);
        }
        sbuilder.append("from").append(BLANK);
        sbuilder.append(request.getMeasurement()).append(BLANK);
        sbuilder.append("where 1=1").append(BLANK);
        if (request.getStartTime() > 0) {
            sbuilder.append("and time >=").append(BLANK);
            sbuilder.append(request.getStartTime()).append(TIME_SUFFIX).append(BLANK);
        }
        if (request.getEndTime() > 0) {
            sbuilder.append("and time <=").append(BLANK);
            sbuilder.append(request.getEndTime()).append(TIME_SUFFIX).append(BLANK);
        }
        if (MapUtils.isNotEmpty(request.getWhereFilter())) {
            sbuilder.append("and").append(BLANK);
            sbuilder.append(parseWhereFilter(request.getWhereFilter())).append(BLANK);
        }
        if (CollectionUtils.isNotEmpty(request.getGroupByTags())) {
            sbuilder.append(parseGroupBy(request.getGroupByTags())).append(BLANK);
        }
        if (request.getOrderByStrategy() != null && request.getOrderByStrategy() == 0) {
            sbuilder.append("order by time asc").append(BLANK);
        } else if (request.getOrderByStrategy() != null && request.getOrderByStrategy() == 1) {
            sbuilder.append("order by time desc").append(BLANK);
        }
        if (request.getLimitRows() > 0) {
            sbuilder.append("limit").append(BLANK).append(request.getLimitRows()).append(BLANK);
        }
        if (request.getLimitRows() > 0) {
            sbuilder.append("offset").append(BLANK).append(request.getOffset()).append(BLANK);
        }
        return sbuilder.toString();
    }

    private String parseGroupBy(List<String> groupFields) {
        return "group by " + StringUtils.join(groupFields, ",");
    }

    private String parseWhereFilter(Map<String, Object> tagMap) {
        List<String> inFilterList = new ArrayList<>();
        List<String> orFilterList = new ArrayList<>();
        tagMap.forEach((k, v) -> {
            if (v instanceof List) {
                if (((List<?>) v).size() <= 1) {
                    inFilterList.add(k + "='" + ((List<?>) v).get(0) + "'");
                } else {
                    //rpc服务的method含有形参,亦是用逗号分割,暂时过滤其他字段的or查询,只支持edgeId
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (Object single : (ArrayList) v) {
                        stringBuilder.append(k + "='" + single + "'").append(" or ");
                    }
                    stringBuilder.delete(stringBuilder.lastIndexOf(" or "), stringBuilder.length());
                    stringBuilder.append(")");
                    orFilterList.add(stringBuilder.toString());
                }
            } else {
                inFilterList.add(k + "='" + v + "'");
            }
        });
        if (orFilterList.isEmpty()) {
            return StringUtils.join(inFilterList, " and ");
        }
        return StringUtils.join(inFilterList, " and ") + " and " + StringUtils.join(orFilterList, " and ");
    }


    public static String parseAliasFields(Map<String, String> fieldsMap) {
        List<String> aliasList = new ArrayList<>();
        fieldsMap.forEach((k, v) -> {
            if (StringUtils.isBlank(v)) {
                aliasList.add(k + " as " + k);
            } else {
                aliasList.add(k + " as " + v);
            }
        });
        if (CollectionUtils.isEmpty(aliasList)) {
            return "*";
        }
        return StringUtils.join(aliasList, ",");
    }

}
