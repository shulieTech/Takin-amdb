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

import io.shulie.amdb.entity.AppServerMetricsReportDO;
import io.shulie.amdb.request.submit.AppServerMetricsReportSubmitRequest;
import io.shulie.amdb.response.metrics.AppServerMetricsReportResponse;
import org.springframework.beans.BeanUtils;

/**
 * @Author: xingchen
 * @ClassName: AppServerMetricsConvert
 * @Package: io.shulie.amdb.convert
 * @Date: 2020/11/420:00
 * @Description:
 */
public class AppServerMetricsConvert {
    /**
     * 转换DO
     *
     * @param appServerMetricsReportSubmitRequest
     * @return
     */
    public static AppServerMetricsReportDO convertAppServerMetricsReportDO(AppServerMetricsReportSubmitRequest appServerMetricsReportSubmitRequest) {
        final AppServerMetricsReportDO tmpDO = new AppServerMetricsReportDO();
        BeanUtils.copyProperties(appServerMetricsReportSubmitRequest, tmpDO);
        return tmpDO;
    }

    /**
     * 转换DTO
     *
     * @param tmpDO
     * @return
     */
    public static AppServerMetricsReportResponse convertAppServerMetricsReportResponseDTO(AppServerMetricsReportDO tmpDO) {
        final AppServerMetricsReportResponse tmpDTO = new AppServerMetricsReportResponse();
        BeanUtils.copyProperties(tmpDO, tmpDTO);
        return tmpDTO;
    }
}
