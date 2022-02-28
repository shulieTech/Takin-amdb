package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.InfluxDbQueryRequest;
import io.shulie.amdb.service.InfluxDbQueryService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Sunsy
 * @date 2022/2/27
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Api("influxdb指标查询")
@RestController
@RequestMapping("amdb/db/influxdb")
public class InfluxDbQueryController {

    @Autowired
    private InfluxDbQueryService influxDbQueryService;

    @RequestMapping(value = "/queryByConditions", method = RequestMethod.POST)
    public Response queryByConditions(@RequestBody InfluxDbQueryRequest request) {
        //参数校验
        AmdbExceptionEnums responseEnum = checkParams(request);
        if (!responseEnum.getCode().equals(AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_SUCCESS.getCode())) {
            return Response.fail(responseEnum, "");
        }
        //调用服务
        Response<List<Map<String, Object>>> response = influxDbQueryService.queryObjectByConditions(request);
        if (response == null) {
            return Response.success(Lists.newArrayList());
        }
        if (response.getData() != null) {
            response.setTotal(response.getData().size());
        }
        //返回结果
        return response;
    }

    private AmdbExceptionEnums checkParams(InfluxDbQueryRequest request) {
        //没有传入表名
        if (StringUtils.isBlank(request.getMeasurement())) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_LACK_MEASUREMENT;
        }
        //开始时间非法 结束时间非法
        if (request.getStartTime() < 0 || request.getEndTime() < 0 || request.getStartTime() > request.getEndTime()) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_INVALID_TIME;
        }
        if (StringUtils.isBlank(request.getDatabase())) {
            request.setDatabase("engine");
        }
        //查询条数非法
        if (request.getLimitRows() < 0) {
            request.setLimitRows(0);
        }
        //limit offset非法
        if (request.getOffset() < 0) {
            request.setOffset(0);
        }
        //没有where条件且不带分页参数
        if (MapUtils.isEmpty(request.getWhereFilter()) && request.getLimitRows() == 0) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_LACK_LIMIT_ROWS;
        }
        //排序非法
        if (request.getOrderByStrategy() != null && request.getOrderByStrategy() != 0 && request.getOrderByStrategy() != 1) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_NOT_SUPPORTED_ORDERBY;
        }
        //group by 非法
        if (CollectionUtils.isEmpty(request.getGroupByTags()) && MapUtils.isNotEmpty(request.getAggregateStrategy())) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_LACK_GROUPBY_TAGS;
        }
        //group by 非法
        if (MapUtils.isEmpty(request.getAggregateStrategy()) && CollectionUtils.isNotEmpty(request.getGroupByTags())) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_LACK_AGGREGATE_STRATEGY;
        }
        //同时出现聚合和非聚合字段
        if (MapUtils.isNotEmpty(request.getFieldAndAlias()) && MapUtils.isNotEmpty(request.getAggregateStrategy())) {
            return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_NOT_SUPPORTED_FIELDSANDAGGFIELDS;
        }
        //聚合字段没有传别名
        if (MapUtils.isNotEmpty(request.getAggregateStrategy())) {
            Set<Map.Entry<String, String>> entries = request.getAggregateStrategy().entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (StringUtils.isBlank(entry.getValue())) {
                    return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_LACK_AGGREGATE_FIELDS_ALIAS;
                }
            }
        }
        return AmdbExceptionEnums.INFLUXDB_QUERY_PARAM_CHECK_SUCCESS;

    }

}
