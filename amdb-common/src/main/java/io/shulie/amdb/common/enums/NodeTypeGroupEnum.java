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
public enum NodeTypeGroupEnum {
    /**
     * APP
     */
    APP("APP"),
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
     * OTHER
     */
    OTHER("OTHER");

    String type;

    NodeTypeGroupEnum(String type) {
        this.type = type;
    }

    public static NodeTypeGroupEnum getNodeType(String middlewareName) {
        if (middlewareName == null || "".equals(middlewareName.trim())) {
            return NodeTypeGroupEnum.APP;
        }
        switch (middlewareName.toLowerCase()) {
            case "app":
            case "dubbo":
            case "apache-dubbo":
            case "http":
            case "undertow":
            case "weblogic":
            case "tomcat":
            case "jetty":
            case "grpc":
                return NodeTypeGroupEnum.APP;
            case "apache-rocketmq":
            case "rocketmq":
            case "ons":
            case "apache-kafka":
            case "kafka":
            case "sf-kafka":
            case "apache-activemq":
            case "activemq":
            case "ibmmq":
            case "rabbitmq":
            case "sto-event":
                return NodeTypeGroupEnum.MQ;
            case "hbase":
            case "aliyun-hbase":
            case "hessian":
            case "mysql":
            case "oracle":
            case "sqlserver":
            case "cassandra":
            case "postgresql":
            case "mongodb":
                return NodeTypeGroupEnum.DB;
            case "tfs":
            case "oss":
                return NodeTypeGroupEnum.OSS;
            case "elasticsearch":
            case "search":
                return NodeTypeGroupEnum.SEARCH;
            case "es":
                return NodeTypeGroupEnum.SEARCH;
            case "redis":
            case "memcache":
            case "cache":
            case "google-guava":
                return NodeTypeGroupEnum.CACHE;
            default:
                return NodeTypeGroupEnum.OTHER;
        }
    }
}
