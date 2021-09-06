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

package io.shulie.amdb.common.enums;

import lombok.Getter;

@Getter
@Deprecated
public enum EdgeTypeGroupEnum {
    /**
     * DUBBO
     */
    DUBBO("DUBBO"),
    /**
     * DUBBO
     */
    GRPC("GRPC"),
    /**
     * HTTP
     */
    HTTP("HTTP"),
    /**
     * DB
     */
    DB("DB"),
    /**
     * CACHE
     */
    CACHE("CACHE"),
    /**
     * MQ
     */
    MQ("MQ"),
    /**
     * SEARCH
     */
    SEARCH("SEARCH"),
    /**
     * OSS
     */
    OSS("OSS"),
    /**
     * JOB
     */
    JOB("JOB"),
    /**
     * UNKNOWN
     */
    UNKNOWN("UNKNOWN");

    String type;

    EdgeTypeGroupEnum(String type) {
        this.type = type;
    }

    public static EdgeTypeGroupEnum getEdgeTypeEnum(String middlewareName) {
        if (middlewareName == null || "".equals(middlewareName.trim())) {
            return EdgeTypeGroupEnum.UNKNOWN;
        }
        switch (middlewareName.toLowerCase()) {
            case "dubbo":
            case "apache-dubbo":
                return EdgeTypeGroupEnum.DUBBO;
            case "apache-rocketmq":
            case "rocketmq":
            case "ons":
            case "apache-kafka":
            case "kafka":
            case "apache-activemq":
            case "activemq":
            case "ibmmq":
            case "rabbitmq":
            case "sf-kafka":
                return EdgeTypeGroupEnum.MQ;
            case "hbase":
            case "aliyun-hbase":
            case "hessian":
            case "mysql":
            case "oracle":
            case "sqlserver":
            case "cassandra":
            case "mongodb":
                return EdgeTypeGroupEnum.DB;
            case "tfs":
            case "oss":
                return EdgeTypeGroupEnum.OSS;
            case "http":
            case "undertow":
            case "tomcat":
            case "virtual":
            case "jetty":
            case "jdk-http":
            case "weblogic":
            case "okhttp":
                return EdgeTypeGroupEnum.HTTP;
            case "elasticsearch":
            case "search":
                return EdgeTypeGroupEnum.SEARCH;
            case "redis":
            case "memcache":
            case "cache":
            case "google-guava":
                return EdgeTypeGroupEnum.CACHE;
            case "elastic-job":
                return EdgeTypeGroupEnum.JOB;
            case "grpc":
                return EdgeTypeGroupEnum.GRPC;
            default:
                if (middlewareName.toLowerCase().contains("http")) {
                    return EdgeTypeGroupEnum.HTTP;
                }
                return EdgeTypeGroupEnum.UNKNOWN;
        }
    }

}
