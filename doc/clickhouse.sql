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

CREATE TABLE default.t_trace_all (`appName` String,`entranceId` Nullable(String),`entranceNodeId` Nullable(String),`traceId` String,`level` Nullable(Int8),`parentIndex` Nullable(Int8),`index` Nullable(Int8),`rpcId` String,`rpcType` Int8,`logType` Nullable(Int8),`traceAppName` Nullable(String),`upAppName` Nullable(String),`startTime` Int64,`cost` Int32,`middlewareName` Nullable(String),`serviceName` Nullable(String),`methodName` Nullable(String),`remoteIp` Nullable(String),`port` Nullable(Int32),`resultCode` Nullable(String),`requestSize` Nullable(String),`responseSize` Nullable(String),`request` Nullable(String),`response` Nullable(String),`clusterTest` Nullable(String),`callbackMsg` Nullable(String),`samplingInterval` Nullable(String),`localId` Nullable(String),`attributes` Nullable(String),`localAttributes` Nullable(String),`async` Nullable(String),`version` Nullable(String),`hostIp` Nullable(String),`agentId` Nullable(String),`startDate` DateTime,`createDate` DateTime DEFAULT toDateTime(now()),`timeMin` Nullable(Int64) DEFAULT 0,`entranceServiceType` Nullable(String),`parsedServiceName` String,`parsedMethod` String,`parsedAppName` Nullable(String),`parsedMiddlewareName` Nullable(String),`parsedExtend` Nullable(String),`dateToMin` Int64,INDEX ix_traceid traceId TYPE minmax GRANULARITY 5,taskId Nullable(String),flag Nullable(String),flagMessage Nullable(String),receiveTime Nullable(Int64),processTime Nullable(Int64),saveCkTime DateTime DEFAULT now())ENGINE = MergeTree PARTITION BY toYYYYMMDD(startDate) ORDER BY (appName,startDate,parsedServiceName,parsedMethod,rpcType) TTL startDate + toIntervalDay(3) SETTINGS index_granularity = 8192;
