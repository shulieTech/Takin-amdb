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

import com.alibaba.fastjson.JSON;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.link.entrance.ExitInfoDTO;
import io.shulie.amdb.common.dto.link.entrance.ServiceInfoDTO;
import io.shulie.amdb.common.dto.link.topology.LinkTopologyDTO;
import io.shulie.amdb.common.request.link.ExitQueryParam;
import io.shulie.amdb.common.request.link.ServiceQueryParam;
import io.shulie.amdb.common.request.link.TopologyQueryParam;
import io.shulie.amdb.common.request.trace.TraceStackQueryParam;
import io.shulie.amdb.dto.LinkDTO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.LinkRequest;
import io.shulie.amdb.service.LinkService;
import io.shulie.amdb.service.LinkUnKnowService;
import io.shulie.amdb.utils.PageInfo;
import io.shulie.surge.data.deploy.pradar.link.processor.LinkProcessor;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Api(description = "链路管理")
@RequestMapping(value = "/amdb/link")
/**
 * 查询链路梳理结果
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class LinkController {
    @Autowired
    private LinkService linkService;
    @Autowired
    private LinkUnKnowService linkUnKnowService;


    /**
     * 查询入口列表
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getServiceList", method = RequestMethod.GET)
    public Response<List<ServiceInfoDTO>> getEntranceList(ServiceQueryParam param) {
        log.info("查询入口 param:{}", JSON.toJSONString(param));
        try {
            if (StringUtils.isBlank(param.getAppName())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return linkService.getServiceListByMysql(param);
        } catch (Exception e) {
            log.error("查询入口列表失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 查询入口列表
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getServiceList", method = RequestMethod.POST)
    public Response<List<ServiceInfoDTO>> getEntranceListOfPost(@RequestBody ServiceQueryParam param) {
        log.info("查询入口列表 param:{}", JSON.toJSONString(param));
        try {
            if (StringUtils.isBlank(param.getAppName())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return linkService.getServiceListByMysql(param);
        } catch (Exception e) {
            log.error("查询入口列表失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 查询出口列表
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getExitList", method = RequestMethod.POST)
    public Response<List<ExitInfoDTO>> getExitList(@RequestBody ExitQueryParam param) {
        log.info("查询出口列表 param:{}", JSON.toJSONString(param));
        try {
            if (StringUtils.isBlank(param.getQueryTye())) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "query_type");
            }
            return linkService.getExitList(param);
        } catch (Exception e) {
            log.error("查询出口列表失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 查询拓扑图
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getLinkTopology", method = RequestMethod.GET)
    public Response<LinkTopologyDTO> getLinkTopology(TopologyQueryParam param) {
        log.info("查询拓扑图 param:{}", JSON.toJSONString(param));
        try {
            //移除临时拓扑设置的threadLocal,防止线程复用导致的参数污染问题
            LinkProcessor.threadLocal.remove();
            Response<LinkTopologyDTO> result = linkService.getLinkTopology(param);
            return result;
        } catch (Exception e) {
            log.error("查询拓扑图失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 查询(临时)拓扑图
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/getLinkTopologyForTemp", method = RequestMethod.GET)
    public Response<LinkTopologyDTO> getLinkTopologyForTemp(TopologyQueryParam param) {
        log.info("查询拓扑图 param:{}", JSON.toJSONString(param));
        try {
            LinkProcessor.threadLocal.set("tempLinkTopology");
            Response<LinkTopologyDTO> response = linkService.getLinkTopology(param);
            return response;
        } catch (Exception e) {
            log.error("查询拓扑图失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 指定调用链生成拓扑图
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/calculateLinkTopology", method = RequestMethod.POST)
    public Response<String> calculateLinkTopology(@RequestBody TraceStackQueryParam param) {
        log.info("指定调用链生成拓扑图 param:{}", param);
        try {
            return linkService.calculateTopology(param);
        } catch (Exception e) {
            log.error("指定调用链生成拓扑图失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_QUERY);
        }
    }

    /**
     * 修改未知节点
     *
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/updateUnKnowNode", method = RequestMethod.GET)
    public Response<String> updateUnKnowNode(TopologyQueryParam param) {
        log.info("修改未知节点 param:{}", JSON.toJSONString(param));
        try {
            if (StringUtils.isNotBlank(param.getId())) {
                return linkUnKnowService.update(param);
            }
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        } catch (Exception e) {
            log.error("修改未知节点失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_UPDATE);
        }
    }


    //----- 自定义链路 --->

    /**
     * 添加自定义链路配置 --
     *
     * @param linkDTO
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response<LinkDTO> add(@RequestBody LinkDTO linkDTO) {
        if (linkDTO == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        if (StringUtils.isBlank(linkDTO.getLinkName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "linkName");
        }
        Long id = linkService.insert(linkDTO);
        linkDTO.setId(id);
        return Response.success(linkDTO);
    }

    /**
     * 更新自定义链路配置 --
     *
     * @param linkDTO
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response<PageInfo<LinkDTO>> update(@RequestBody LinkDTO linkDTO) {
        if (linkDTO == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        if (linkDTO.getId() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "link id");
        }
        linkService.update(linkDTO);
        // 加载最新数据
        return Response.success(linkService.list(new LinkRequest()));
    }

    /**
     * 查询自定义链路配置 --
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Response<PageInfo<LinkDTO>> list(@RequestBody LinkRequest request) {
        return Response.success(linkService.list(request));
    }

    /**
     * 删除自定义链路 --
     *
     * @param linkDTO
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Response<PageInfo<LinkDTO>> delete(@RequestBody LinkDTO linkDTO) {
        if (linkDTO.getId() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "link id");
        }
        linkService.delete(linkDTO.getId());
        // 加载最新数据
        return Response.success(linkService.list(new LinkRequest()));
    }

    /**
     * 自定义链路关系调整--
     *
     * @param linkDTO
     * @return
     */
    @RequestMapping(value = "/operateLinkAll", method = RequestMethod.POST)
    public Response operateLinkAll(@RequestBody LinkDTO linkDTO) {
        if (linkDTO == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        if (linkDTO.getId() == null) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "linkID");
        }
        if (CollectionUtils.isEmpty(linkDTO.getLinkNodeDTOList())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "nodeList");
        }
        if (CollectionUtils.isEmpty(linkDTO.getRelationDTOList())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM_STRING_DESC, "relationDTOList");
        }
        linkService.operateLinkAll(linkDTO);
        return Response.success("成功");
    }


    /**
     * 根据链路ID查询链路信息 --
     *
     * @param query
     * @return
     */
    @RequestMapping(value = "/queryLinkAll", method = RequestMethod.POST)
    public Response<List<LinkDTO>> queryLinkAll(@RequestBody LinkDTO query) {
        // 自定义链路查询
        return Response.success(linkService.queryCustomLinkAll(query));
    }

    /**
     * 根据链路ID查询链路信息
     *
     * @param query
     * @return
     */
    @RequestMapping(value = "/queryLinkApp", method = RequestMethod.POST)
    public Response<List<LinkDTO>> queryLinkApp(@RequestBody LinkDTO query) {
        return Response.success(linkService.queryAppLinkAll(query));
    }
}
