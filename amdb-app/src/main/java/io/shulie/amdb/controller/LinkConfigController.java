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
import io.shulie.amdb.common.request.link.CalculateParam;
import io.shulie.amdb.entity.TAMDBPradarLinkConfigDO;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.service.LinkConfigService;
import io.shulie.amdb.utils.StringUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: xingchen
 * @ClassName: LinkConfigController
 * @Package: io.shulie.amdb.controller
 * @Date: 2020/12/1022:41
 * @Description:
 */
@RestController
@Api(value = "链路配置管理")
@RequestMapping(value = "/amdb/linkConfig")
/**
 * 链路配置管理（开启关闭链路采集）
 * @Author: xingchen
 * @Date: 2020/11/419:51
 * @Description:
 */
public class LinkConfigController {
    private static Logger logger = LoggerFactory.getLogger(LinkConfigController.class);

    @Autowired
    private LinkConfigService linkConfigService;

    /**
     * 开始链路梳理配置
     *
     * @param calculateParam
     * @return
     */
    @RequestMapping(value = "/openLinkConfig", method = RequestMethod.POST)
    Response<String> openLinkConfig(@RequestBody CalculateParam calculateParam) {
        logger.info("链路梳理配置参数：{}", JSON.toJSONString(calculateParam));
        if (StringUtil.isBlank(calculateParam.getAppName()) || StringUtil.isBlank(calculateParam.getRpcType()) || StringUtil.isBlank(calculateParam.getMethod()) || StringUtil.isBlank(calculateParam.getServiceName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return linkConfigService.buildLinkConfig(calculateParam);
        } catch (Exception e) {
            logger.error("链路梳理配置失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_CONFIG_UPDATE);
        }
    }

    /**
     * 删除链路梳理刷新配置
     *
     * @param calculateParam
     * @return
     */
    @RequestMapping(value = "/closeLinkConfig", method = RequestMethod.POST)
    Response<String> closeLinkConfig(@RequestBody CalculateParam calculateParam) {
        logger.info("删除链路梳理配置 参数：{}", JSON.toJSONString(calculateParam));
        if (StringUtil.isBlank(calculateParam.getAppName()) || StringUtil.isBlank(calculateParam.getRpcType()) || StringUtil.isBlank(calculateParam.getMethod()) | StringUtil.isBlank(calculateParam.getServiceName())) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return linkConfigService.deleteLinkConfig(calculateParam);
        } catch (Exception e) {
            logger.error("删除链路配置失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_CONFIG_UPDATE);
        }

    }

    /**
     * 查询链路梳理是否已配置
     *
     * @param calculateParam
     * @return
     */
    @RequestMapping(value = "/isLinkConfigOpen", method = RequestMethod.POST)
    Response<Boolean> isLinkConfigOpen(@RequestBody CalculateParam calculateParam) {
        try {
            return Response.success(linkConfigService.isLinkConfigOpen(calculateParam));
        } catch (Exception e) {
            logger.error("查询链路配置失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_CONFIG_QUERY);
        }
    }

    /**
     * 查询所有链路梳理配置
     * getAll
     */
    @RequestMapping(value = "/queryAll", method = RequestMethod.GET)
    Response<List<TAMDBPradarLinkConfigDO>> queryAll() {
        try {
            return Response.success(linkConfigService.selectAll());
        } catch (Exception e) {
            logger.error("查询链路配置失败", e);
            return Response.fail(AmdbExceptionEnums.LINK_CONFIG_QUERY);
        }
    }
}
