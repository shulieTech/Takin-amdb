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

package io.shulie.amdb.service.impl;

import io.shulie.amdb.common.request.trace.EntryTraceQueryParam;
import io.shulie.amdb.service.impl.TraceServiceImpl;
import io.shulie.surge.data.common.utils.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

class TraceServiceImplTest {

    @Test
    void getFilters() {
        TraceServiceImpl traceService = new TraceServiceImpl();
        EntryTraceQueryParam param = new EntryTraceQueryParam();
        param.setQueryType(2);
        param.setEntranceList("#serviceName##rpcType");
        Pair<List<String>, List<String>> pair =  traceService.getFilters(param,false);
        assertEquals(pair.getSecond().get(0),"(parsedServiceName like '%serviceName%' and rpcType='rpcType')");
        param.setQueryType(2);
        param.setEntranceList("#serviceName#methodName#rpcType");
        pair =  traceService.getFilters(param,false);
        assertEquals(pair.getSecond().get(0),"(parsedServiceName like '%serviceName%' and rpcType='rpcType' and parsedMethod='methodName' )");

        param.setQueryType(2);
        param.setEntranceList("#serviceName##rpcType,#serviceName##rpcType");
        pair =  traceService.getFilters(param,false);
        assertEquals(pair.getSecond().get(0),"(parsedServiceName like '%serviceName%' and rpcType='rpcType')");

    }
}