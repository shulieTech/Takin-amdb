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

package io.shulie.amdb.convert;


import io.shulie.amdb.entity.MachineMetricsReportDO;
import io.shulie.amdb.request.submit.MachineMetricsReportSubmitRequest;

public class MachineMetricsReportConvert {
    /**
     * MachineMetricsReportSubmitRequest -> MachineMetricsReportDO
     * @param request
     * @return
     */
    public static MachineMetricsReportDO convertRelationDO(MachineMetricsReportSubmitRequest request) {
        MachineMetricsReportDO machineMetricsReportDO = new MachineMetricsReportDO();
        machineMetricsReportDO.setCpuUs(request.getCpuUs()==null?0f:request.getCpuUs());
        machineMetricsReportDO.setIoRt(request.getIoRt()==null?0f:request.getIoRt());
        machineMetricsReportDO.setIoUs(request.getIoUs()==null?0f:request.getIoUs());
        machineMetricsReportDO.setIpAddress(request.getIpAddress());
        machineMetricsReportDO.setMemUs(request.getMemUs()==null?0f:request.getMemUs());
        machineMetricsReportDO.setTimestamp(request.getTimestamp());
        return machineMetricsReportDO;
    }
}
