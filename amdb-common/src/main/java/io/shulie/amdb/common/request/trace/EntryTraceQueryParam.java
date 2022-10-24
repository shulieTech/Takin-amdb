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

package io.shulie.amdb.common.request.trace;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@ApiModel
public class EntryTraceQueryParam extends AbstractAmdbBaseRequest {
    @ApiModelProperty("查询数据类型(1-agent上报trace明细,2-压测报告请求trace明细)")
    int queryType = 1;
    @ApiModelProperty("调用类型(指定中间件名称)")
    String middlewareName;
    @ApiModelProperty("排序字段")
    String sortField = "cost";
    @ApiModelProperty("排序方式")
    String sortType = "desc";
    @ApiModelProperty("调用参数")
    String request;
    @ApiModelProperty("是否压测流量")
    String clusterTest;
    @ApiModelProperty("结果类型(1代表成功请求,0代表失败请求)")
    String resultType;
    @ApiModelProperty("调用类型")
    String rpcType;
    @ApiModelProperty("调用类型")
    String logType;
    @ApiModelProperty("接口名称")
    String serviceName;
    @ApiModelProperty("方法名称")
    String methodName;
    @ApiModelProperty("方法名称模糊")
    String queryMethodName;
    @ApiModelProperty("入口列表(多个入口列表用逗号进行分隔)")
    String entranceList;
    @ApiModelProperty("应用名称")
    String appName;
    @ApiModelProperty("结果字段(多个结果字段用逗号进行分隔)")
    String fieldNames;
    @ApiModelProperty("查询开始时间")
    Long startTime;
    @ApiModelProperty("查询结束时间")
    Long endTime;
    @ApiModelProperty("压测报告ID")
    String taskId;
    @ApiModelProperty("页数")
    Integer pageSize;
    @ApiModelProperty("页码")
    Integer currentPage;
    @ApiModelProperty("traceId列表(支持多个traceid,用逗号分隔)")
    Set<String> traceIdList;
    @ApiModelProperty("最小耗时(最大最小耗时都传0时默认查询所有)")
    long minCost;
    @ApiModelProperty("最大耗时最大最小耗时都传0时默认查询所有")
    long maxCost;
    @ApiModelProperty("调用来源(tro/e2e/dau)")
    String querySource;
    @ApiModelProperty("租户下应用列表")
    List<String> appNames;

}