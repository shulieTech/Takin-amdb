package io.shulie.amdb.request.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/27
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@ApiModel("InfluxDbQueryRequest")
@Data
public class InfluxDbQueryRequest {

    @ApiModelProperty("数据库名")
    private String database = "engine";
    @ApiModelProperty("表名")
    private String measurement;
    @ApiModelProperty("开始时间")
    private long startTime;
    @ApiModelProperty("结束时间")
    private long endTime;
    @ApiModelProperty("查询字段及其别名")
    private Map<String, String> fieldAndAlias;
    @ApiModelProperty("where过滤条件")
    private Map<String, Object> whereFilter;
    @ApiModelProperty("查询条数")
    private long limitRows;
    @ApiModelProperty("分页开始offset")
    private long offset;
    @ApiModelProperty("排序策略")
    private Integer orderByStrategy;
    @ApiModelProperty("分组tag")
    private List<String> groupByTags;
    @ApiModelProperty("0:升序;1:降序")
    private Map<String, String> aggregateStrategy;
}

