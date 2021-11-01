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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.link.entrance.ExitInfoDTO;
import io.shulie.amdb.common.dto.link.entrance.ServiceInfoDTO;
import io.shulie.amdb.common.dto.link.topology.LinkTopologyDTO;
import io.shulie.amdb.common.request.link.ExitQueryParam;
import io.shulie.amdb.common.request.link.ServiceQueryParam;
import io.shulie.amdb.common.request.link.TopologyQueryParam;
import io.shulie.amdb.common.request.trace.TraceStackQueryParam;
import io.shulie.amdb.dto.LinkDTO;
import io.shulie.amdb.request.LinkRequest;
import io.shulie.amdb.utils.PageInfo;

import java.io.IOException;
import java.util.List;

/**
 * @Author: xingchen
 * @ClassName: LinkService
 * @Package: io.shulie.amdb.service
 * @Date: 2020/10/1914:11
 * @Description:
 */
public interface LinkService {
    /**
     * 新增
     *
     * @param linkDTO
     */
    Long insert(LinkDTO linkDTO);

    /**
     * 修改链路
     *
     * @param linkDTO
     */
    void update(LinkDTO linkDTO);

    /**
     * 查询链路
     *
     * @param request
     */
    PageInfo<LinkDTO> list(LinkRequest request);

    /**
     * 删除
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 修改节点-链路关系
     *
     * @param linkDTO
     */
    void operateLinkAll(LinkDTO linkDTO);

    /**
     * 查询链路-按类型查询(自定义链路-接口拓扑图)
     *
     * @param linkDTO
     */
    List<LinkDTO> queryCustomLinkAll(LinkDTO linkDTO);

    /**
     * 查询链路-按类型查询(自定义链路-接口拓扑图)
     *
     * @param linkDTO
     */
    //List<LinkDTO> queryInterfaceLinkAll(LinkDTO linkDTO);

    /**
     * 查询链路-按类型查询(应用链路)
     *
     * @param linkDTO
     */
    List<LinkDTO> queryAppLinkAll(LinkDTO linkDTO);

    /**
     * 查询入口列表
     *
     * @param param
     * @return
     */
    Response<List<ServiceInfoDTO>> getServiceListByMysql(ServiceQueryParam param);

    /**
     * 查询出口列表
     *
     * @param param
     * @return
     */
    Response<List<ExitInfoDTO>> getExitList(ExitQueryParam param);

    /**
     * 查询拓扑图
     *
     * @param param
     * @return
     */
    Response<LinkTopologyDTO> getLinkTopology(TopologyQueryParam param);

    Response<String> calculateTopology(TraceStackQueryParam param) throws IOException;
}
