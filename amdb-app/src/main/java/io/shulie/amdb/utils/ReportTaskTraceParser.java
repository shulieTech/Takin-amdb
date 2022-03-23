package io.shulie.amdb.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.pamirs.pradar.log.parser.Const;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.amdb.service.impl.ReportTaskServiceImpl.InnerRpcBased;
import io.shulie.amdb.service.impl.ReportTaskServiceImpl.InnerService;
import io.shulie.amdb.service.impl.ReportTaskServiceImpl.ServiceMetrics;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import org.springframework.util.CollectionUtils;

public abstract class ReportTaskTraceParser {

    public static Map<InnerService, ServiceMetrics> parseRpcBased(String traceId, List<InnerRpcBased> rpcBasedList) {
        if (CollectionUtils.isEmpty(rpcBasedList)) {
            return Maps.newHashMap();
        }
        for (InnerRpcBased rpcBased : rpcBasedList) {
            rpcBased.adjust();
        }
        Comparator<RpcBased> comparator = (o1, o2) -> {
            if (o1.getLevel() == o2.getLevel()) {
                if (o1.getIndex() == o2.getIndex()) {
                    if (o1.getLogType() == o2.getLogType()) {
                        return 0;
                    }
                    if (o1.getLogType() == Const.LOG_TYPE_RPC_CLIENT) {
                        return -1;
                    }
                    if (o1.getLogType() == Const.LOG_TYPE_RPC_SERVER) {
                        return 1;
                    }
                    return 0;
                } else {
                    return o1.getIndex() - o2.getIndex();
                }
            }
            return o1.getLevel() - o2.getLevel();
        };
        rpcBasedList.sort(comparator);
        return parse0(traceId, rpcBasedList);
    }

    // 单条trace丽娜路中服务端日志自耗时
    private static Map<InnerService, ServiceMetrics> parse0(String traceId, List<InnerRpcBased> rpcBasedList) {
        Map<InnerService, ServiceMetrics> result = new HashMap<>();
        Map<Integer, List<InnerRpcBased>> indexMap = rpcBasedList.stream().collect(
            Collectors.groupingBy(InnerRpcBased::getLevel));
        for (Entry<Integer, List<InnerRpcBased>> entry : indexMap.entrySet()) {
            Integer level = entry.getKey();
            List<InnerRpcBased> basedList = entry.getValue();
            List<InnerRpcBased> childBasedList = indexMap.get(level + 1);
            if (childBasedList == null) {
                childBasedList = new ArrayList<>(2);
            }
            Map<String, List<InnerRpcBased>> parentRpcIdMap = childBasedList.stream().collect(
                Collectors.groupingBy(InnerRpcBased::getParentRpcId));
            basedList.forEach(based -> {
                int logType = based.getLogType();
                // 只计算入口日志和服务端日志
                if (logType == PradarLogType.LOG_TYPE_RPC_SERVER || logType == PradarLogType.LOG_TYPE_TRACE) {
                    long childCostSum = 0;
                    List<InnerRpcBased> childRpcBasedList = parentRpcIdMap.get(based.getParentRpcId());
                    if (!CollectionUtils.isEmpty(childRpcBasedList)) {
                        for (InnerRpcBased rpcBased : childRpcBasedList) {
                            if (!rpcBased.isAsync()) { // 异步日志不计入耗时
                                childCostSum += rpcBased.getCost();
                            }
                        }
                    }
                    result.put(buildService(based), new ServiceMetrics(based.getCost() - childCostSum, traceId));
                }
            });
        }
        return result;
    }

    private static InnerService buildService(InnerRpcBased based) {
        return new InnerService(based.getAgentId(), based.getAppName(), based.getParsedServiceName(),
            based.getParsedMethod(), String.valueOf(based.getRpcType()));
    }
}
