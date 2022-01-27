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

package io.shulie.amdb.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmdbExceptionEnums {

    /**
     * 通用异常 AMDB_100_UXXXX
     */
    COMMON_UNDEFINE("AMDB_9999_U0000", "系统异常"),
    COMMON_UNDEFINE_STRING_DESC("AMDB_9999_U0001", "系统异常 : %s"),//未定义异常
    COMMON_EMPTY_PARAM("AMDB_9999_U0011", "请求参数为空"),
    COMMON_EMPTY_PARAM_STRING_DESC("AMDB_9999_U0012", "请求参数为空: %s"),

    /**
     * 应用模块异常
     * <p>
     * 应用模块      AMDB_0201_UXXXX
     * 应用实例模块   AMDB_0202_UXXXX
     * 应用状态      AMDB_0203_UXXXX
     */
    APP_SELECT("AMDB_0201_U00000", "应用信息数据查询异常"),
    APP_UPDATE("AMDB_0201_U00001", "应用信息数据更新异常"),
    APP_INSTANCE_SELECT("AMDB_0202_U00000", "应用实例查询异常"),
    APP_INSTANCE_UPDATE("AMDB_0202_U00001", "应用实例更新异常"),
    APP_INSTANCE_INFO_SELECT("AMDB_0202_U00002", "应用实例信息查询异常"),
    APP_INSTANCE_STATUS_SELECT("AMDB_0203_U00000", "应用状态查询异常"),
    APP_SHADOW_DATABASE_SELECT("AMDB_0204_U00000", "应用影子库表查询异常"),
    APP_SHADOW_BIZ_TABLE_SELECT("AMDB_0204_U00001", "应用业务表查询异常"),
    APP_INFO_SELECT("AMDB_0205_U00000", "应用信息查询异常"),


    /**
     * 链路配置 AMDB_0301_UXXXX
     * 链路梳理 AMDB_0302_UXXXX
     */
    LINK_CONFIG_QUERY("AMDB_0301_U00000", "链路配置数据查询异常"),
    LINK_CONFIG_UPDATE("AMDB_0301_U00001", "链路配置数据更新异常"),
    LINK_QUERY("AMDB_0302_U00000", "链路数据查询异常"),
    LINK_UPDATE("AMDB_0302_U00001", "链路数据更新异常"),
    LINK_UNDEFINE("AMDB_0302_U00002", "链路不存在"),
    LINK_ENTRANCE_DUPLICATION("AMDB_0302_U00003", "链路入口不唯一"),
    LINK_PARENT_NODE_UNDEFINE("AMDB_0302_U00004", "父节点为空"),


    /**
     * trace日志异常 AMDB_0400_UXXXX
     */
    TRACE_QUERY("AMDB_0$00_U0000", "trace数据查询异常"),
    TRACE_DETAIL_QUERY("AMDB_0400_U0001", "trace详情查询异常"),
    TRACE_EMPTY_SELECT_FILED("AMDB_0400_U0003", "trace查询失败,参数错误，未指定查询结果字段列表"),
    TRACE_QUERY_WARN("AMDB_0400_U0004", "警告: %s"),

    /**
     * E2E业务异常 AMDB_0500_UXXXX
     */
    E2E_NODE_QUERY("AMDB_0500_U0000", "E2E节点数据查询异常"),
    E2E_NODE_UPDATE("AMDB_0500_U0001", "E2E节点数据更新异常"),
    E2E_ASSERT_QUERY("AMDB_0500_U0002", "E2E断言数据查询异常"),
    E2E_ASSERT_UPDATE("AMDB_0500_U0003", "E2E断言数据跟更新异常"),

    /**
     * trace_metrics指标查询异常 AMDB_0600_UXXXX
     */
    METRICS_NODE_QUERY("AMDB_0600_U0000", "METRICS指标数据查询异常"),

    /**
     * 应用自定义探针配置查询异常 AMDB_0700_UXXXX
     */
    TRODATA_QUERY("AMDB_0700_U0000", "应用自定义探针配置查询异常"),
    APISLIST_QUERY("AMDB_0700_U0001", "入口规则查询异常"),

    /**
     * 申通服务指标查询鉴权不通过 AMDB_0800_UXXXX
     */
    STO_QUERY_NOT_AUTH("AMDB_0800_U0000", "鉴权不通过,请传入正确的userAppKey"),
    STO_QUERY_ILLEGAL_PARAM("AMDB_0800_U0001", "参数不合法,请检查"),
    ;

    private final String code;
    private final String msg;

}
