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

package io.shulie.amdb.service;

import com.github.pagehelper.PageInfo;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.instance.AgentStatusStatInfo;
import io.shulie.amdb.entity.TAmdbAppInstanceStatusDO;
import io.shulie.amdb.request.query.AppInstanceStatusQueryRequest;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsResponse;
import io.shulie.amdb.response.instance.AmdbAppInstanceStautsSumResponse;

import java.util.List;

public interface AppInstanceStatusService {

    Response insertOrUpdate(TAmdbAppInstanceStatusDO record);

    Response insertBatch(List<TAmdbAppInstanceStatusDO> tAmdbApps);

    TAmdbAppInstanceStatusDO selectOneByParam(TAmdbAppInstanceStatusDO instance);

    int update(TAmdbAppInstanceStatusDO record);

    int updateBatch(List<TAmdbAppInstanceStatusDO> tAmdbApps);

    PageInfo<AmdbAppInstanceStautsResponse> selectByParams(AppInstanceStatusQueryRequest param);

    AmdbAppInstanceStautsSumResponse queryInstanceSumInfo(AppInstanceStatusQueryRequest param);

    /**
     * 获取在线实例列表
     *
     * @param param
     * @return
     */
    Integer getOnlineInstanceCount(AppInstanceStatusQueryRequest param);

    /**
     * 获取特定状态实例列表
     *
     * @param param
     * @return
     */
    Integer getSpecificStatusInstanceCount(AppInstanceStatusQueryRequest param);

    List<String> getAllProbeVersionsByAppName(AppInstanceStatusQueryRequest param);

    void deleteByParams(AppInstanceStatusQueryRequest param);

    void truncateTable();

    /**
     * 状态统计
     * @return
     */
    AgentStatusStatInfo countStatus(AppInstanceStatusQueryRequest param);
}
