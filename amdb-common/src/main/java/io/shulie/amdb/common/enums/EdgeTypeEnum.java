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
public enum EdgeTypeEnum {
    /**
     * DUBBO
     */
    DUBBO("DUBBO"),
    /**
     * HTTP
     */
    HTTP("HTTP"),
    /**
     * DB
     */
    DB("DB"),
    /**
     * MYSQL
     */
    MYSQL("MYSQL"),
    /**
     * ORACLE
     */
    ORACLE("ORACLE"),
    /**
     * SQLSERVER
     */
    SQLSERVER("SQLSERVER"),
    /**
     * CASSANDRA
     */
    CASSANDRA("CASSANDRA"),
    /**
     * MONGODB
     */
    MONGODB("MONGODB"),
    /**
     * HBASE
     */
    HBASE("HBASE"),
    /**
     * HESSIAN
     */
    HESSIAN("HESSIAN"),
    /**
     * CACHE
     */
    CACHE("CACHE"),
    /**
     * RDS
     */
    RDS("RDS"),
    /**
     * REDIS
     */
    REDIS("REDIS"),
    /**
     * MEMCACHE
     */
    MEMCACHE("MEMCACHE"),
    /**
     * MQ
     */
    MQ("MQ"),
    /**
     * ROCKETMQ
     */
    ROCKETMQ("ROCKETMQ"),
    /**
     * KAFKA
     */
    KAFKA("KAFKA"),
    /**
     * ACTIVEMQ
     */
    ACTIVEMQ("ACTIVEMQ"),
    /**
     * IBMMQ
     */
    IBMMQ("IBMMQ"),
    /**
     * RABBITMQ
     */
    RABBITMQ("RABBITMQ"),
    /**
     * ES
     */
    ES("ES"),
    /**
     * OSS
     */
    OSS("OSS"),
    /**
     * VIRTUAL
     */
    VIRTUAL("VIRTUAL"),
    /**
     * ELASTIC-JOB
     */
    ELASTICJOB("ELASTIC-JOB"),
    /**
     * GRPC
     */
    GRPC("GRPC"),
    /**
     * UNKNOWN
     */
    UNKNOWN("UNKNOWN");

    String type;

    EdgeTypeEnum(String type) {
        this.type = type;
    }

    public static EdgeTypeEnum getEdgeTypeEnum(String middlewareName) {
        if (middlewareName == null || "".equals(middlewareName.trim())) {
            return EdgeTypeEnum.UNKNOWN;
        }
        if (middlewareName.toLowerCase().contains("http")
                || middlewareName.equalsIgnoreCase("jetty")) {
            middlewareName = "http";
        }
        switch (middlewareName.toLowerCase()) {
            case "dubbo":
            case "apache-dubbo":
                return EdgeTypeEnum.DUBBO;
            case "apache-rocketmq":
            case "rocketmq":
            case "ons":
                return EdgeTypeEnum.ROCKETMQ;
            case "apache-kafka":
            case "sf-kafka":
            case "kafka":
                return EdgeTypeEnum.KAFKA;
            case "apache-activemq":
            case "activemq":
                return EdgeTypeEnum.ACTIVEMQ;
            case "ibmmq":
                return EdgeTypeEnum.IBMMQ;
            case "rabbitmq":
                return EdgeTypeEnum.RABBITMQ;
            case "hbase":
            case "aliyun-hbase":
                return EdgeTypeEnum.HBASE;
            case "hessian":
                return EdgeTypeEnum.HESSIAN;
            case "tfs":
                return EdgeTypeEnum.OSS;
            case "http":
            case "undertow":
            case "tomcat":
            case "jetty":
            case "jdk-http":
            case "weblogic":
            case "okhttp":
                return EdgeTypeEnum.HTTP;
            case "virtual":
                return EdgeTypeEnum.VIRTUAL;
            case "oss":
                return EdgeTypeEnum.OSS;
            case "mysql":
                return EdgeTypeEnum.MYSQL;
            case "oracle":
                return EdgeTypeEnum.ORACLE;
            case "sqlserver":
                return EdgeTypeEnum.SQLSERVER;
            case "cassandra":
                return EdgeTypeEnum.CASSANDRA;
            case "mongodb":
                return EdgeTypeEnum.MONGODB;
            case "elasticsearch":
                return EdgeTypeEnum.ES;
            case "redis":
                return EdgeTypeEnum.REDIS;
            case "memcache":
                return EdgeTypeEnum.MEMCACHE;
            case "cache":
            case "google-guava":
                return EdgeTypeEnum.CACHE;
            case "search":
                return EdgeTypeEnum.ES;
            case "elastic-job":
                return EdgeTypeEnum.ELASTICJOB;
            case "grpc":
                return EdgeTypeEnum.GRPC;
            default:
                if (middlewareName.toLowerCase().contains("http")) {
                    return EdgeTypeEnum.HTTP;
                }
                return EdgeTypeEnum.UNKNOWN;
        }
    }

    public static String convertMiddlewareName(String oldMiddlewareName) {
        if (oldMiddlewareName.toLowerCase().contains("netty")) {
            return "http";
        }
        if (oldMiddlewareName.toLowerCase().contains("jetty")) {
            return "http";
        }
        if (oldMiddlewareName.toLowerCase().contains("http")) {
            return "http";
        }
        if (oldMiddlewareName.toLowerCase().contains("dubbo")) {
            return "dubbo";
        }
        if (oldMiddlewareName.toLowerCase().contains("kafka")) {
            return "kafka";
        }
        if (oldMiddlewareName.toLowerCase().contains("rocketmq")) {
            return "rocketmq";
        }
        if (oldMiddlewareName.toLowerCase().contains("cache")) {
            return "cache";
        }
        return oldMiddlewareName;
    }

}
