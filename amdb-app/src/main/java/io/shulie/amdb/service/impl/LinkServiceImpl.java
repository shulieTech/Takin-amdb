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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.link.entrance.ExitInfoDTO;
import io.shulie.amdb.common.dto.link.entrance.ServiceInfoDTO;
import io.shulie.amdb.common.dto.link.topology.LinkEdgeDTO;
import io.shulie.amdb.common.dto.link.topology.LinkTopologyDTO;
import io.shulie.amdb.common.enums.EdgeTypeEnum;
import io.shulie.amdb.common.enums.EdgeTypeGroupEnum;
import io.shulie.amdb.common.enums.NodeTypeEnum;
import io.shulie.amdb.common.enums.NodeTypeGroupEnum;
import io.shulie.amdb.common.request.link.ExitQueryParam;
import io.shulie.amdb.common.request.link.ServiceQueryParam;
import io.shulie.amdb.common.request.link.TopologyQueryParam;
import io.shulie.amdb.common.request.trace.TraceStackQueryParam;
import io.shulie.amdb.convert.LinkConvert;
import io.shulie.amdb.convert.LinkNodeConvert;
import io.shulie.amdb.convert.LinkNodeRelationConvert;
import io.shulie.amdb.dto.LinkDTO;
import io.shulie.amdb.dto.LinkNodeDTO;
import io.shulie.amdb.dto.LinkNodeRelationDTO;
import io.shulie.amdb.entity.*;
import io.shulie.amdb.enums.LinkTypeEnum;
import io.shulie.amdb.exception.AmdbException;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.mapper.*;
import io.shulie.amdb.request.LinkRequest;
import io.shulie.amdb.service.AppService;
import io.shulie.amdb.service.LinkConfigService;
import io.shulie.amdb.service.LinkService;
import io.shulie.amdb.utils.PageInfo;
import io.shulie.amdb.utils.PagingUtils;
import io.shulie.amdb.utils.StringUtil;
import io.shulie.surge.data.deploy.pradar.link.enums.TraceLogQueryScopeEnum;
import io.shulie.surge.data.deploy.pradar.link.model.LinkEdgeModel;
import io.shulie.surge.data.deploy.pradar.link.model.LinkNodeModel;
import io.shulie.surge.data.deploy.pradar.link.processor.LinkProcessor;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * @Author: xingchen
 * @ClassName: LinkServiceImpl
 * @Package: io.shulie.amdb.service.impl
 * @Date: 2020/10/1914:19
 * @Description:
 */
@Service
public class LinkServiceImpl implements LinkService {
    private static Logger logger = LoggerFactory.getLogger(LinkServiceImpl.class);

    @Resource
    private LinkMapper linkMapper;

    @Resource
    private LinkNodeMapper linkNodeMapper;

    @Resource
    private LinkNodeRelationMapper linkNodeRelationMapper;

    @Autowired
    private AppService appService;

    @Resource
    private AppRelationMapper appRelationMapper;

    @Resource
    private LinkEntranceMapper linkEntranceMapper;

    @Autowired
    LinkConfigService linkConfigdsService;

    @Resource
    PradarLinkEdgeMapper pradarLinkEdgeMapper;

    @Resource
    PradarLinkNodeMapper pradarLinkNodeMapper;

    @Autowired
    LinkProcessor linkProcessor;

    private List<String> fields = Arrays.asList(new String[]{"appName", "serviceName", "methodName", "middlewareName", "rpcType", "extend", "upAppName"});

    /**
     * 新增
     *
     * @param linkDTO
     */
    @Override
    public Long insert(LinkDTO linkDTO) {
        LinkDO record = LinkConvert.convertLinkDO(linkDTO);
        record.setGmtCreate(new Date());
        record.setType(LinkTypeEnum.CUSTOM.getType());
        linkMapper.insertSelective(record);
        return record.getId();
    }

    @Override
    public void update(LinkDTO linkDTO) {
        LinkDO updateDO = LinkConvert.convertLinkDO(linkDTO);

        updateDO.setId(linkDTO.getId());
        updateDO.setGmtModify(new Date());
        updateDO.setModifier(linkDTO.getModifier());
        updateDO.setModifierName(linkDTO.getModifierName());
        linkMapper.updateByPrimaryKeySelective(updateDO);
    }

    /**
     * 查询自定义链路
     *
     * @param request
     * @return
     */
    @Override
    public PageInfo<LinkDTO> list(LinkRequest request) {
        Example example = new Example(LinkDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (request.getLinkId() != null) {
            criteria.andEqualTo("id", request.getLinkId());
        }
        Page<LinkDO> linkDoPage = PageHelper.startPage(request.getCurrentPage(), request.getPageSize()).doSelectPage(
                () ->
                        linkMapper.selectByExample(example));
        return new PageInfo<>(linkDoPage, c -> LinkConvert.convertLinkDTO(c));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long linkId) {
        linkMapper.deleteByPrimaryKey(linkId);

        Example nodeExample = new Example(LinkNodeDO.class);
        Example.Criteria nodeCriteria = nodeExample.createCriteria();
        nodeCriteria.andEqualTo("linkId", linkId);
        linkNodeMapper.deleteByExample(nodeExample);

        Example nodeRelationExample = new Example(LinkNodeRelationDO.class);
        Example.Criteria nodeRelationCriteria = nodeRelationExample.createCriteria();
        nodeRelationCriteria.andEqualTo("linkId", linkId);
        linkNodeRelationMapper.deleteByExample(nodeRelationExample);
    }

    /**
     * 新增-节点-关系
     *
     * @param linkDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operateLinkAll(LinkDTO linkDTO) {
        LinkDO linkDO = Optional.ofNullable(linkMapper.selectByPrimaryKey(linkDTO.getId())).orElse(new LinkDO());
        if (StringUtils.isBlank(linkDO.getLinkName())) {
            throw new AmdbException(AmdbExceptionEnums.LINK_UNDEFINE);
        }
        long parentCount = linkDTO.getLinkNodeDTOList().stream().filter(node -> node.getParent()).count();
        if (parentCount != 1) {
            throw new AmdbException(AmdbExceptionEnums.LINK_ENTRANCE_DUPLICATION);
        }
        LinkNodeDTO parentNode = linkDTO.getLinkNodeDTOList().stream().filter(node -> node.getParent()).findFirst()
                .orElse(new LinkNodeDTO());
        if (StringUtils.isBlank(parentNode.getAppName())) {
            throw new AmdbException(AmdbExceptionEnums.LINK_PARENT_NODE_UNDEFINE);
        }
        Map<String, String> globalIdMap = Maps.newHashMap();
        Map<String, String> globalNameMap = Maps.newHashMap();
        List<LinkNodeDO> linkNodeDOList = linkDTO.getLinkNodeDTOList().stream().map(node -> {
            LinkNodeDO record = LinkNodeConvert.convertLinkNodeDO(node);
            record.setLinkId(linkDTO.getId());
            node.setLinkId(linkDTO.getId());
            String uk = Md5Utils.md5(LinkNodeConvert.genUk(node));
            globalIdMap.put(node.getNodeId(), uk);
            globalNameMap.put(node.getNodeId(), record.getAppName());

            record.setNodeId(uk);
            record.setGmtCreate(new Date());
            record.setCreator(linkDTO.getCreator());
            record.setCreatorName(linkDTO.getCreatorName());
            return record;
        }).collect(Collectors.toList());

        List<LinkNodeRelationDO> relationDOList = linkDTO.getRelationDTOList().stream().map(relationDO -> {
            LinkNodeRelationDO record = LinkNodeRelationConvert.convertRelationDO(relationDO);
            record.setLinkId(linkDTO.getId());
            // 转换下sourceID，targetId
            record.setSourceAppName(globalNameMap.get(record.getSourceId()));
            record.setSourceId(globalIdMap.get(record.getSourceId()));
            record.setTargetAppName(globalNameMap.get(record.getTargetId()));
            record.setTargetId(globalIdMap.get(record.getTargetId()));
            record.setGmtCreate(new Date());
            record.setCreator(linkDTO.getCreator());
            record.setCreatorName(linkDTO.getCreatorName());
            return record;
        }).collect(Collectors.toList());
        Example nodeExample = new Example(LinkNodeDO.class);
        Example.Criteria nodeCriteria = nodeExample.createCriteria();
        nodeCriteria.andEqualTo("linkId", linkDTO.getId());
        linkNodeMapper.deleteByExample(nodeExample);

        Example nodeRelationExample = new Example(LinkNodeRelationDO.class);
        Example.Criteria nodeRelationCriteria = nodeRelationExample.createCriteria();
        nodeRelationCriteria.andEqualTo("linkId", linkDTO.getId());
        linkNodeRelationMapper.deleteByExample(nodeRelationExample);

        if (CollectionUtils.isNotEmpty(linkNodeDOList)) {
            linkNodeMapper.insertList(linkNodeDOList);
        }
        if (CollectionUtils.isNotEmpty(relationDOList)) {
            linkNodeRelationMapper.insertList(relationDOList);
        }
        /**
         * 更新链路入口到链路表
         */
        LinkDO updateLinkDO = new LinkDO();
        updateLinkDO.setId(linkDTO.getId());
        updateLinkDO.setEntranceType(parentNode.getEntranceType());
        updateLinkDO.setEntrance(parentNode.getEntrance());

        updateLinkDO.setGmtModify(new Date());
        updateLinkDO.setModifier(linkDTO.getModifier());
        updateLinkDO.setModifierName(linkDTO.getModifierName());
        linkMapper.updateByPrimaryKeySelective(updateLinkDO);
    }

    /**
     * 查询链路节点-关系
     *
     * @param query
     * @return
     */
    @Override
    public List<LinkDTO> queryCustomLinkAll(LinkDTO query) {
        List<LinkDTO> linkDTOList = Lists.newArrayList();
        Example linkExample = new Example(LinkDO.class);
        Example.Criteria linkCriteria = linkExample.createCriteria();
        if (CollectionUtils.isNotEmpty(query.getIds())) {
            linkCriteria.andIn("id", query.getIds());
        }
        if (query.getId() != null) {
            linkCriteria.andEqualTo("id", query.getId());
        }
        List<LinkDO> linkDOList = linkMapper.selectByExample(linkExample);
        linkDOList.stream().forEach(link -> {
            Example linkNodeExample = new Example(LinkNodeDO.class);
            Example.Criteria linkNodeCriteria = linkNodeExample.createCriteria();
            linkNodeCriteria.andEqualTo("linkId", link.getId());
            List<LinkNodeDO> linkNodeDOList = linkNodeMapper.selectByExample(linkNodeExample);

            Example linkNodeRelationExample = new Example(LinkNodeDO.class);
            Example.Criteria linkNodeRelationCriteria = linkNodeRelationExample.createCriteria();
            linkNodeRelationCriteria.andEqualTo("linkId", link.getId());
            List<LinkNodeRelationDO> linkNodeRelationDOList = linkNodeRelationMapper.selectByExample(
                    linkNodeRelationExample);

            // 转换
            LinkDTO linkDTO = LinkConvert.convertLinkDTO(link);
            List<LinkNodeDTO> linkNodeDTOList = linkNodeDOList.stream().map(node -> {
                final LinkNodeDTO linkNodeDTO = LinkNodeConvert.convertLinkNodeDTO(node);
                return linkNodeDTO;
            }).collect(Collectors.toList());

            List<LinkNodeRelationDTO> linkNodeRelationDTOList = linkNodeRelationDOList.stream().map(relation -> {
                final LinkNodeRelationDTO relationDTO = LinkNodeRelationConvert.convertRelationDTO(relation);
                return relationDTO;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(linkNodeDTOList) &&
                    CollectionUtils.isNotEmpty(linkNodeRelationDTOList)) {
                linkDTO.setLinkNodeDTOList(linkNodeDTOList);
                linkDTO.setRelationDTOList(linkNodeRelationDTOList);
            }
            linkDTOList.add(linkDTO);
        });
        return linkDTOList;
    }

    /**
     * 应用链路
     *
     * @param query
     * @return
     */
    @Override
    public List<LinkDTO> queryAppLinkAll(LinkDTO query) {
        LinkDTO linkDTO = new LinkDTO();
        linkDTO.setType(query.getType());
        Set<LinkNodeDTO> linkNodeDtoSet = new HashSet<>();
        Set<LinkNodeRelationDTO> relationDTOList = new HashSet<>();
        queryNodes(query.getUpLevel(), 1, "toAppName", Arrays.asList(query.getAppName()), linkNodeDtoSet,
                relationDTOList, new ArrayList<>());
        queryNodes(query.getDownLevel(), 1, "fromAppName", Arrays.asList(query.getAppName()), linkNodeDtoSet,
                relationDTOList, new ArrayList<>());
        // 查询应用
        List<String> appNameList = linkNodeDtoSet.stream().map(LinkNodeDTO::getNodeName).collect(Collectors.toList());
        List<AppDO> appDOList = appService.selectByFilter(
                "app_name in ('" + StringUtils.join(appNameList, "','") + "')");
        // 应用与类型的关系
        Map<String, String> appNameToType = appDOList.stream().collect(
                Collectors.toMap(AppDO::getAppName, AppDO::getAppType));
        // 处理节点类型
        linkNodeDtoSet.forEach(linkNodeDTO -> linkNodeDTO.setNodeType(appNameToType.get(linkNodeDTO.getNodeName())));
        relationDTOList.forEach(
                relationDTO -> relationDTO.setTargetAppType(appNameToType.get(relationDTO.getTargetAppName())));
        linkDTO.setLinkNodeDTOList(linkNodeDtoSet.stream().collect(Collectors.toList()));
        linkDTO.setRelationDTOList(relationDTOList.stream().collect(Collectors.toList()));
        return Arrays.asList(linkDTO);
    }

    private void queryNodes(int level, int currentLevel, String propertyName, List<String> appNames,
                            Set<LinkNodeDTO> linkNodeDtoSet, Set<LinkNodeRelationDTO> relationDtoSet, List<Integer> passedRelation) {
        if (currentLevel > level || CollectionUtils.isEmpty(appNames)) {
            return;
        }
        Example upExample = new Example(AppRelationDO.class);
        Example.Criteria upCriteria = upExample.createCriteria();
        upCriteria.andIn(propertyName, appNames);
        if (CollectionUtils.isNotEmpty(passedRelation)) {
            upCriteria.andNotIn("id", passedRelation);
        }
        List<AppRelationDO> relations = appRelationMapper.selectByExample(upExample);
        List<String> newAppNames = new ArrayList<>();
        relations.stream().forEach(relation -> {
            passedRelation.add(relation.getId());
            LinkNodeDTO from = new LinkNodeDTO();
            from.setNodeId(relation.getFromAppName());
            from.setNodeName(relation.getFromAppName());
            linkNodeDtoSet.add(from);
            LinkNodeDTO to = new LinkNodeDTO();
            to.setNodeId(relation.getToAppName());
            to.setNodeName(relation.getToAppName());
            linkNodeDtoSet.add(to);
            LinkNodeRelationDTO relationDTO = new LinkNodeRelationDTO();
            relationDTO.setSourceId(relation.getFromAppName());
            relationDTO.setTargetId(relation.getToAppName());
            relationDTO.setTargetAppName(relation.getToAppName());
            relationDTO.setEagleId(relation.getFromAppName() + "|" + relation.getToAppName());
            relationDtoSet.add(relationDTO);
            if ("toAppName".equals(propertyName)) {
                newAppNames.add(relation.getFromAppName());
            } else {
                newAppNames.add(relation.getToAppName());
            }
        });
        if (currentLevel < level) {
            queryNodes(level, currentLevel + 1, propertyName, newAppNames, linkNodeDtoSet, relationDtoSet,
                    passedRelation);
        }
    }

    /**
     * 查询服务列表
     *
     * @param param
     * @return
     */
    @Override
    public Response<List<ServiceInfoDTO>> getServiceListByMysql(ServiceQueryParam param) {
        List<ServiceInfoDTO> serviceInfoDTOList = new ArrayList<>();
        Example example = new Example(PradarLinkEntranceDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(param.getAppName())) {
            String appName = param.getAppName();
            if (appName.contains(",")) {
                criteria.andIn("appName", Arrays.asList(appName.split(",")));
            } else {
                criteria.andEqualTo("appName", param.getAppName());
            }
        }

        if (StringUtils.isNotBlank(param.getRpcType())) {
            String rpcType = param.getRpcType();
            if (param.getRpcType().contains(",")) {
                criteria.andIn("rpcType", Arrays.asList(rpcType.split(",")));
            } else {
                criteria.andEqualTo("rpcType", rpcType);
            }
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            String[] serviceNameAry = param.getServiceName().split(",");
            String serviceNameFilter = "";
            //如果为批量查询时不允许采用模糊匹配,否则会导致慢sql把mysql cpu打满-顺丰
            if (serviceNameAry.length > 1) {
                criteria.andIn("serviceName", Arrays.asList(serviceNameAry));
            } else {
                serviceNameFilter += "service_name like '%" + serviceNameAry[0] + "%'";
                criteria.andCondition("(" + serviceNameFilter + ")");
            }
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            criteria.andEqualTo("methodName", param.getMethodName());
        }
        if (StringUtils.isNotBlank(param.getMiddlewareName())) {
            String middlewareName = param.getMiddlewareName();
            if (param.getMiddlewareName().contains(",")) {
                criteria.andIn("middlewareName", Arrays.asList(middlewareName.split(",")));
            } else {
                criteria.andEqualTo("middlewareName", middlewareName);
            }
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }
        //2021-06-01 为了兼容老接口,需要增加link_type!=1 过滤条件,将客户端出口数据过滤掉
        criteria.andNotEqualTo("linkType", "1");

        int page = param.getCurrentPage();
        int pageSize = param.getPageSize();
        PageHelper.startPage(page, pageSize);
        List<PradarLinkEntranceDO> linkEntranceDOList = linkEntranceMapper.selectByExample(example);

        //2021-07-13 由于可能存在多个客户端调用相同服务端的情况,导致相同url入口可能存在多条 需要对查询出来的数据做一个去重
        linkEntranceDOList = linkEntranceDOList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getAppName() + o.getServiceName() + o.getMethodName() + o.getMiddlewareName() + o.getRpcType() + o.getExtend()))), ArrayList::new));

        if (CollectionUtils.isNotEmpty(linkEntranceDOList)) {
            linkEntranceDOList.forEach(data -> {
                ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
                serviceInfoDTO.setServiceName(data.getServiceName());
                serviceInfoDTO.setMethodName(data.getMethodName());
                serviceInfoDTO.setAppName(data.getAppName());
                serviceInfoDTO.setRpcType(String.valueOf(data.getRpcType()));
                serviceInfoDTO.setMiddlewareName(data.getMiddlewareName());
                serviceInfoDTO.setExtend(data.getExtend());
                serviceInfoDTOList.add(serviceInfoDTO);
            });
        }
        return Response.success(PagingUtils.result(linkEntranceDOList, serviceInfoDTOList));
    }

    /**
     * 查询服务列表
     *
     * @param param
     * @return
     */
    @Override
    public Response<List<ExitInfoDTO>> getExitList(ExitQueryParam param) {
        if (StringUtils.isBlank(param.getFieldNames())) {
            return Response.fail("参数错误，未指定查询字段");
        }
        // 拼装查询字段
        List<String> selectFields = getSelectFields(param.getFieldNames());
        if (isEmpty(selectFields)) {
            return Response.fail("参数错误，未指定查询结果字段列表");
        }

        List<ExitInfoDTO> exitInfoDtos = new ArrayList<>();
        Example example = new Example(PradarLinkEntranceDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(param.getAppName())) {
            String appName = param.getAppName();
            if (appName.contains(",")) {
                criteria.andIn("appName", Arrays.asList(appName.split(",")));
            } else {
                criteria.andEqualTo("appName", param.getAppName());
            }
        }

        if (StringUtils.isNotBlank(param.getRpcType())) {
            String rpcType = param.getRpcType();
            if (param.getRpcType().contains(",")) {
                criteria.andIn("rpcType", Arrays.asList(rpcType.split(",")));
            } else {
                criteria.andEqualTo("rpcType", rpcType);
            }
        }
        if (StringUtils.isNotBlank(param.getServiceName())) {
            String[] serviceNameAry = param.getServiceName().split(",");
            String serviceNameFilter = "";
            //如果为批量查询时不允许采用模糊匹配,否则会导致慢sql把mysql cpu打满-顺丰
            if (serviceNameAry.length > 1) {
                criteria.andIn("serviceName", Arrays.asList(serviceNameAry));
            } else {
                serviceNameFilter += "service_name like '%" + serviceNameAry[0] + "%'";
                criteria.andCondition("(" + serviceNameFilter + ")");
            }
        }
        if (StringUtils.isNotBlank(param.getMethodName())) {
            criteria.andEqualTo("methodName", param.getMethodName());
        }
        if (StringUtils.isNotBlank(param.getMiddlewareName())) {
            String middlewareName = param.getMiddlewareName();
            if (middlewareName.contains(",")) {
                criteria.andIn("middlewareName", Arrays.asList(middlewareName.split(",")));
            } else {
                criteria.andEqualTo("middlewareName", middlewareName);
            }
        }

        //如果传了upAppName,则查服务端日志,即入口
        if (StringUtils.isNotBlank(param.getUpAppName())) {
            String upAppName = param.getUpAppName();
            if (upAppName.contains(",")) {
                criteria.andIn("upAppName", Arrays.asList(upAppName.split(",")));
            } else {
                criteria.andEqualTo("upAppName", param.getUpAppName());
            }
        }

        if (StringUtils.isNotBlank(param.getQueryTye())) {
            criteria.andEqualTo("linkType", param.getQueryTye());
        }
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            criteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            criteria.andEqualTo("envCode", param.getEnvCode());
        }

        if (param.isDefaultWhiteFlag()) {
            criteria.andNotEqualTo("defaultWhiteInfo", "");
        }

        int page = param.getCurrentPage();
        int pageSize = param.getPageSize();

        PageHelper.startPage(page, pageSize);
        List<PradarLinkEntranceDO> linkEntranceDOList = linkEntranceMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(linkEntranceDOList)) {
            linkEntranceDOList.forEach(data -> {
                ExitInfoDTO exitInfoDTO = new ExitInfoDTO();
                if (selectFields.contains("appName")) {
                    exitInfoDTO.setAppName(data.getAppName());
                }
                if (selectFields.contains("serviceName")) {
                    exitInfoDTO.setServiceName(data.getServiceName());
                }
                if (selectFields.contains("methodName")) {
                    exitInfoDTO.setMethodName(data.getMethodName());
                }
                if (selectFields.contains("middlewareName")) {
                    exitInfoDTO.setRpcType(String.valueOf(data.getRpcType()));
                }
                if (selectFields.contains("rpcType")) {
                    exitInfoDTO.setMiddlewareName(data.getMiddlewareName());
                }
                if (selectFields.contains("extend")) {
                    exitInfoDTO.setExtend(data.getExtend());
                }
                if (selectFields.contains("upAppName")) {
                    exitInfoDTO.setUpAppName(data.getUpAppName());
                }
                if (selectFields.contains("middlewareDetail")) {
                    exitInfoDTO.setMiddlewareDetail(data.getMiddlewareDetail());
                }
                if (selectFields.contains("downAppName")) {
                    exitInfoDTO.setDownAppName(data.getDownAppName());
                }
                if (selectFields.contains("defaultWhiteInfo")) {
                    String[] defaultWhiteInfo = data.getDefaultWhiteInfo().split("@##");
                    if (defaultWhiteInfo.length == 2) {
                        String reason = StringUtil.parseStr(JSONPath.read(defaultWhiteInfo[1], "$.ext.container"));
                        String isIgnore = StringUtil.parseStr(JSONPath.read(defaultWhiteInfo[1], "$.ext.ignore"));
                        exitInfoDTO.setDefaultWhiteInfo(isIgnore + "#" + reason);
                    } else {
                        exitInfoDTO.setDefaultWhiteInfo(data.getDefaultWhiteInfo());
                    }
                }
                exitInfoDtos.add(exitInfoDTO);
            });
        }
        return Response.success(PagingUtils.result(linkEntranceDOList, exitInfoDtos));
    }

    private List<String> getSelectFields(String fieldNameStr) {
        List<String> selectFields = new ArrayList<>();
        if (StringUtils.isBlank(fieldNameStr)) {
            return null;
        } else {
            List<String> fieldNames = Arrays.asList(fieldNameStr.split(","));
            if (fieldNames.contains("appName")) {
                selectFields.add("appName");
            }
            if (fieldNames.contains("rpcType")) {
                selectFields.add("rpcType");
            }
            if (fieldNames.contains("serviceName")) {
                selectFields.add("serviceName");
            }
            if (fieldNames.contains("methodName")) {
                selectFields.add("methodName");
            }
            if (fieldNames.contains("middlewareName")) {
                selectFields.add("middlewareName");
            }
            if (fieldNames.contains("extend")) {
                selectFields.add("extend");
            }
            if (fieldNames.contains("upAppName")) {
                selectFields.add("upAppName");
            }
            if (fieldNames.contains("middlewareDetail")) {
                selectFields.add("middlewareDetail");
            }
            if (fieldNames.contains("downAppName")) {
                selectFields.add("downAppName");
            }
            if (fieldNames.contains("defaultWhiteInfo")) {
                selectFields.add("defaultWhiteInfo");
            }
        }
        return selectFields;
    }

    /**
     * 查询拓扑图
     *
     * @param param
     * @return
     */
    @Override
    public Response<LinkTopologyDTO> getLinkTopology(TopologyQueryParam param) {
        LinkTopologyDTO linkTopologyDTO = new LinkTopologyDTO();
        String linkId = param.getLinkId();
        if (StringUtils.isBlank(linkId)) {
            StringBuffer tags = new StringBuffer();
            tags.append(objectToString(param.getServiceName(), ""))
                    .append("|")
                    .append(objectToString(param.getMethod(), ""))
                    .append("|")
                    .append(objectToString(param.getAppName(), ""))
                    .append("|")
                    .append(objectToString(param.getRpcType(), ""))
                    .append("|")
                    .append(objectToString(param.getExtend(), ""));
            linkId = Md5Utils.md5(tags.toString());
        }
        Pair<List<TAmdbPradarLinkNodeDO>, List<TAmdbPradarLinkEdgeDO>> pair = getPradarLinkInfo(linkId, param);
        // 从trace构建拓扑图
        if (CollectionUtils.isEmpty(pair.getLeft()) && param.isTrace() && StringUtil.isNotBlank(param.getRpcType())) {
            Map<String, Object> linkConfig = new LinkedHashMap<>();
            linkConfig.put("method", param.getMethod());
            linkConfig.put("appName", param.getAppName());
            linkConfig.put("rpcType", param.getRpcType());
            linkConfig.put("extend", param.getExtend());
            linkConfig.put("service", param.getServiceName());
            if (StringUtils.isNotBlank(param.getTenantAppKey())) {
                linkConfig.put("userAppKey", param.getTenantAppKey());
            }
            if (StringUtils.isNotBlank(param.getEnvCode())) {
                linkConfig.put("envCode", param.getEnvCode());
            }
            pair = getPradarLinkInfoFromTrace(linkId, linkConfig);
        }
        // 处理虚拟节点
        final boolean isVirtual = processVirtualNode(pair, param);
        linkTopologyDTO.setNodes(pair.getLeft().stream().map(nodeDO -> {
            io.shulie.amdb.common.dto.link.topology.LinkNodeDTO linkNodeDTO
                    = new io.shulie.amdb.common.dto.link.topology.LinkNodeDTO();
            linkNodeDTO.setNodeId(nodeDO.getAppId());
            linkNodeDTO.setNodeName(nodeDO.getAppName());
            nodeDO.setMiddlewareName(NodeTypeEnum.convertMiddlewareName(nodeDO.getMiddlewareName()));
            linkNodeDTO.setNodeType(NodeTypeEnum.getNodeType(nodeDO.getMiddlewareName()).getType());
            linkNodeDTO.setNodeTypeGroup(NodeTypeGroupEnum.getNodeType(nodeDO.getMiddlewareName()).getType());
            linkNodeDTO.setExtendInfo(JSONObject.parseObject(nodeDO.getExtend(), Map.class));
            if (isVirtual) {
                linkNodeDTO.setRoot(nodeDO.getAppName().contains("-Virtual"));
            } else {
                linkNodeDTO.setRoot(nodeDO.getAppName().equals(param.getAppName()));
            }
            return linkNodeDTO;
        }).collect(Collectors.toList()));
        linkTopologyDTO.setEdges(pair.getRight().stream().map(edgeDO -> {
            io.shulie.amdb.common.dto.link.topology.LinkEdgeDTO linkEdgeDTO
                    = new io.shulie.amdb.common.dto.link.topology.LinkEdgeDTO();
            linkEdgeDTO.setEagleId(edgeDO.getEdgeId());
            //改为真实的middleWareName
            linkEdgeDTO.setMiddlewareName(edgeDO.getMiddlewareName());
            linkEdgeDTO.setEagleType(EdgeTypeEnum.getEdgeTypeEnum(linkEdgeDTO.getMiddlewareName()).getType());
            linkEdgeDTO.setEagleTypeGroup(EdgeTypeGroupEnum.getEdgeTypeEnum(linkEdgeDTO.getMiddlewareName()).getType());
            linkEdgeDTO.setSourceId(edgeDO.getFromAppId());
            linkEdgeDTO.setTargetId(edgeDO.getToAppId());
            linkEdgeDTO.setService(edgeDO.getService());
            linkEdgeDTO.setAppName(edgeDO.getAppName());
            linkEdgeDTO.setExtend(edgeDO.getExtend());
            linkEdgeDTO.setMethod(edgeDO.getMethod());
            linkEdgeDTO.setLogType(edgeDO.getLogType());
            linkEdgeDTO.setRpcType(edgeDO.getRpcType());
            linkEdgeDTO.setServerAppName(edgeDO.getServerAppName());
            return linkEdgeDTO;
        }).collect(Collectors.toList()));
        String isTemp = LinkProcessor.threadLocal.get();
        if (isTemp != null && "tempLinkTopology".equals(isTemp)) {
            return Response.success(removeUpNodeForLinkTopology(linkTopologyDTO));
        } else {
            return Response.success(linkTopologyDTO);
        }
    }

    private LinkTopologyDTO removeUpNodeForLinkTopology(LinkTopologyDTO linkTopologyDTO) {
        //把上游服务删除，保持和现有链路图风格一致
        String virtualAppId = "";   //虚拟节点ID,即第一个有效节点
        for (io.shulie.amdb.common.dto.link.topology.LinkNodeDTO node : linkTopologyDTO.getNodes()) {
            if (node.getNodeName().endsWith("Virtual")) {
                virtualAppId = node.getNodeId().replace("-Virtual", "");
            }
        }
        //移除边
        List<LinkEdgeDTO> edges = new ArrayList<>();
        List<String> errorNodes = new ArrayList<>();
        for (LinkEdgeDTO edge : linkTopologyDTO.getEdges()) {
            if (virtualAppId.equals(edge.getTargetId()) && !edge.getSourceId().startsWith(virtualAppId)) {
                errorNodes.add(edge.getSourceId());
            } else {
                edges.add(edge);
            }
        }
        //移除点
        List<io.shulie.amdb.common.dto.link.topology.LinkNodeDTO> nodes = new ArrayList<>();
        for (io.shulie.amdb.common.dto.link.topology.LinkNodeDTO node : linkTopologyDTO.getNodes()) {
            if (errorNodes.contains(node.getNodeId())) {
                //移除
            } else {
                nodes.add(node);
            }
        }
        LinkTopologyDTO newLinkTopologyDTO = new LinkTopologyDTO();
        newLinkTopologyDTO.setEdges(edges);
        newLinkTopologyDTO.setNodes(nodes);
        return newLinkTopologyDTO;
    }

    @Override
    public Response<String> calculateTopology(TraceStackQueryParam param) throws IOException {
        String serviceName = param.getServiceName();
        String methodName = param.getMethodName();
        String appName = param.getAppName();
        String rpcType = param.getRpcType();
        String traceId = param.getTraceId();
        String startTime = param.getStartTime();
        String endTime = param.getEndTime();

        StringBuffer tags = new StringBuffer();
        tags.append(objectToString(serviceName, ""))
                .append("|")
                .append(objectToString(methodName, ""))
                .append("|")
                .append(objectToString(appName, ""))
                .append("|")
                .append(objectToString(rpcType, ""))
                .append("|");
        String linkId = Md5Utils.md5(tags.toString());

        Map<String, String> request = new HashMap<>();
        request.put("linkId", linkId);
        request.put("service", serviceName);
        request.put("method", methodName);
        request.put("appName", appName);
        request.put("rpcType", rpcType);
        request.put("traceId", traceId);
        request.put("startTime", startTime);
        request.put("endTime", endTime);
        request.put("userAppKey", param.getTenantAppKey());
        request.put("envCode", param.getEnvCode());
        request.put("userId", param.getUserId());

        //如果没传值则赋默认值
        request.put("rpcId", StringUtils.isBlank(param.getRpcId()) ? "0" : param.getRpcId());
        request.put("logType", StringUtils.isBlank(param.getLogType()) ? "1" : param.getLogType());

        //初始化
        linkProcessor.init();

        //根据traceId计算链路拓扑
        Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> topology = linkProcessor.link(request);
        //保存到表里
        linkProcessor.saveTopology(linkId, null, topology);
        return Response.success(linkId + " save edges:" + topology.getRight().size() + ",nodes:" + topology.getLeft().size());
    }

    /**
     * 虚拟节点处理
     * 1.如果链路本身有虚拟节点，则不需要再单独处理
     * 2.如果链路本身没有虚拟节点，则在所选appName节点前新增虚拟节点和虚拟边
     *
     * @param pair
     * @param param
     * @return
     */
    public boolean processVirtualNode(Pair<List<TAmdbPradarLinkNodeDO>, List<TAmdbPradarLinkEdgeDO>> pair,
                                      TopologyQueryParam param) {
        boolean hasVirtual = pair.getLeft().stream().filter(nodeDO -> nodeDO.getAppName().endsWith("-Virtual")).count()
                > 0;
        hasVirtual = formatEntry(hasVirtual, param, pair);
        return hasVirtual;
    }

    /**
     * 计算链路中间入口的虚拟急节点
     *
     * @param hasVirtual
     * @param param
     * @param pair
     * @return
     */
    private boolean formatEntry(boolean hasVirtual, TopologyQueryParam param,
                                Pair<List<TAmdbPradarLinkNodeDO>, List<TAmdbPradarLinkEdgeDO>> pair) {
        if (hasVirtual) {
            return hasVirtual;
        }
        TAmdbPradarLinkNodeDO tmpNodeDO = pair.getLeft().stream().filter(
                nodeDO -> nodeDO.getAppName().equals(param.getAppName())).findFirst().orElse(new TAmdbPradarLinkNodeDO());
        if (tmpNodeDO == null || StringUtils.isBlank(tmpNodeDO.getAppName())) {
            return hasVirtual;
        }
        String middlewareName = "virtual";
        TAmdbPradarLinkNodeDO nodeDO = new TAmdbPradarLinkNodeDO();
        nodeDO.setAppId(tmpNodeDO.getAppId() + "-Virtual");
        nodeDO.setAppName(tmpNodeDO.getAppName() + "-Virtual");
        nodeDO.setMiddlewareName(middlewareName);

        TAmdbPradarLinkEdgeDO edgeDO = new TAmdbPradarLinkEdgeDO();
        edgeDO.setLinkId(tmpNodeDO.getLinkId());
        edgeDO.setFromAppId(tmpNodeDO.getAppId() + "-Virtual");
        edgeDO.setToAppId(tmpNodeDO.getAppId());
        edgeDO.setRpcType(param.getRpcType());
        edgeDO.setMiddlewareName(middlewareName);
        edgeDO.setService(param.getServiceName());
        edgeDO.setMethod(param.getMethod());
        edgeDO.setEdgeId(tmpNodeDO.getAppId() + "-edge-virtual");

        pair.getLeft().add(nodeDO);
        pair.getRight().add(edgeDO);
        return true;
    }

    /**
     * 从数据库中查询拓扑图信息
     *
     * @param linkId
     * @return
     */
    private Pair<List<TAmdbPradarLinkNodeDO>, List<TAmdbPradarLinkEdgeDO>> getPradarLinkInfo(String linkId, TopologyQueryParam param) {
        Example nodeExample = new Example(TAmdbPradarLinkNodeDO.class);
        Example.Criteria nodeCriteria = nodeExample.createCriteria();
        nodeCriteria.andEqualTo("linkId", linkId);
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            nodeCriteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            nodeCriteria.andEqualTo("envCode", param.getEnvCode());
        }
        List<TAmdbPradarLinkNodeDO> nodeDOList = pradarLinkNodeMapper.selectByExample(nodeExample);
        Example edgeExample = new Example(TAmdbPradarLinkEdgeDO.class);
        Example.Criteria edgeCriteria = edgeExample.createCriteria();
        edgeCriteria.andEqualTo("linkId", linkId);
        if (StringUtils.isNotBlank(param.getTenantAppKey())) {
            edgeCriteria.andEqualTo("userAppKey", param.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(param.getEnvCode())) {
            edgeCriteria.andEqualTo("envCode", param.getEnvCode());
        }
        List<TAmdbPradarLinkEdgeDO> edgeDOList = pradarLinkEdgeMapper.selectByExample(edgeExample);
        return Pair.of(nodeDOList, edgeDOList);
    }

    /**
     * 根据入口信息查询Trace并计算拓扑图
     *
     * @param linkId
     * @param linkConfig
     * @return
     */
    private Pair<List<TAmdbPradarLinkNodeDO>, List<TAmdbPradarLinkEdgeDO>> getPradarLinkInfoFromTrace(String linkId,
                                                                                                      Map<String, Object> linkConfig) {
        List<TAmdbPradarLinkNodeDO> nodeDOList = new ArrayList<>();
        List<TAmdbPradarLinkEdgeDO> edgeDOList = new ArrayList<>();
        try {
            Pair<Set<LinkNodeModel>, Set<LinkEdgeModel>> topology =
                    linkProcessor.link(linkId, linkConfig, TraceLogQueryScopeEnum.build(60));
            nodeDOList = topology.getLeft().stream().map(linkNodeModel -> {
                TAmdbPradarLinkNodeDO linkNodeDTO = new TAmdbPradarLinkNodeDO();
                linkNodeDTO.setAppId(linkNodeModel.getAppId());
                linkNodeDTO.setAppName(linkNodeModel.getAppName());
                linkNodeDTO.setMiddlewareName(linkNodeModel.getMiddlewareName());
                linkNodeDTO.setExtend(linkNodeModel.getExtend());
                return linkNodeDTO;
            }).collect(Collectors.toList());
            edgeDOList = topology.getRight().stream().map(linkEdgeModel -> {
                TAmdbPradarLinkEdgeDO linkEdgeDTO = new TAmdbPradarLinkEdgeDO();
                linkEdgeDTO.setEdgeId(linkEdgeModel.getEdgeId());
                linkEdgeDTO.setMiddlewareName(linkEdgeModel.getMiddlewareName());
                linkEdgeDTO.setFromAppId(linkEdgeModel.getFromAppId());
                linkEdgeDTO.setToAppId(linkEdgeModel.getToAppId());
                linkEdgeDTO.setService(linkEdgeModel.getService());
                linkEdgeDTO.setMethod(linkEdgeModel.getMethod());
                linkEdgeDTO.setExtend(linkEdgeModel.getExtend());
                linkEdgeDTO.setAppName(linkEdgeModel.getAppName());
                linkEdgeDTO.setRpcType(linkEdgeModel.getRpcType());
                linkEdgeDTO.setLogType(linkEdgeModel.getLogType());
                linkEdgeDTO.setServerAppName(linkEdgeDTO.getServerAppName());
                return linkEdgeDTO;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("拓扑计算失败", e);
        }
        return Pair.of(nodeDOList, edgeDOList);
    }

    /**
     * 对象转字符串
     *
     * @param value
     * @param defaultStr
     * @return
     */
    private String objectToString(Object value, String defaultStr) {
        if (value == null || "null".equals(value.toString().toLowerCase())) {
            return defaultStr;
        }
        return ObjectUtils.toString(value);
    }

}