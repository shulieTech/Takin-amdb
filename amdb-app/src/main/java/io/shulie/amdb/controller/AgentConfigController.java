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
package io.shulie.amdb.controller;

import com.github.pagehelper.PageInfo;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.agent.AgentConfigDTO;
import io.shulie.amdb.common.dto.agent.AgentStatInfoDTO;
import io.shulie.amdb.common.request.agent.AgentConfigQueryRequest;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.service.AgentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * agent配置
 *
 * @author anjone
 * @date 2021/8/18
 */
@Slf4j
@RestController
@RequestMapping("amdb/db/api/agentConfig")
public class AgentConfigController {
    @Autowired
    private AgentConfigService agentConfigService;

    /**
     * agent配置查询
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/queryConfig", method = RequestMethod.POST)
    public Response<List<AgentConfigDTO>> queryConfig(@RequestBody AgentConfigQueryRequest param) {
        try {
            PageInfo<AgentConfigDTO> agentConfigDTOPageInfo = agentConfigService.selectByParams(param);
            return Response.success(agentConfigDTOPageInfo);
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

    /**
     * agent配置统计
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/count", method = RequestMethod.POST)
    public Response<AgentStatInfoDTO> count(@RequestBody AgentConfigQueryRequest param) {
        try {
            if (param == null || param.getConfigKey() == null) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "configKey");
            }
            return Response.success(agentConfigService.count(param));
        } catch (Exception e) {
            log.error("查询应用实例状态失败", e);
            return Response.fail(AmdbExceptionEnums.APP_INSTANCE_STATUS_SELECT);
        }
    }

}
