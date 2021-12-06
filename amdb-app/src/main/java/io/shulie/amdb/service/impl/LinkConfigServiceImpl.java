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

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.request.link.CalculateParam;
import io.shulie.amdb.entity.TAMDBPradarLinkConfigDO;
import io.shulie.amdb.entity.TAmdbPradarLinkEdgeDO;
import io.shulie.amdb.entity.TAmdbPradarLinkNodeDO;
import io.shulie.amdb.mapper.PradarLinkConfigMapper;
import io.shulie.amdb.mapper.PradarLinkEdgeMapper;
import io.shulie.amdb.mapper.PradarLinkNodeMapper;
import io.shulie.amdb.service.LinkConfigService;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class LinkConfigServiceImpl implements LinkConfigService {

    @Resource
    PradarLinkConfigMapper pradarLinkConfigMapper;

    @Resource
    PradarLinkNodeMapper pradarLinkNodeMapper;

    @Resource
    PradarLinkEdgeMapper pradarLinkEdgeMapper;

    @Override
    public Response<String> buildLinkConfig(CalculateParam calculateParam) {
        String linkId = Md5Utils.md5(buildLinkTag(calculateParam));
        //配置写入Hbase
        TAMDBPradarLinkConfigDO configDO = new TAMDBPradarLinkConfigDO();
        configDO.setLinkId(linkId);
        configDO.setAppName(calculateParam.getAppName());
        configDO.setService(calculateParam.getServiceName());
        configDO.setExtend(calculateParam.getExtend());
        configDO.setMethod(calculateParam.getMethod());
        configDO.setRpcType(calculateParam.getRpcType());
        if (StringUtils.isNotBlank(calculateParam.getTenantAppKey())) {
            configDO.setUserAppKey(calculateParam.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(calculateParam.getEnvCode())) {
            configDO.setEnvCode(calculateParam.getEnvCode());
        }
        pradarLinkConfigMapper.insert(configDO);
        return Response.success(linkId);
    }

    @Override
    public boolean isLinkConfigOpen(CalculateParam calculateParam) {
        String linkId = Md5Utils.md5(buildLinkTag(calculateParam));
        Example example = new Example(TAMDBPradarLinkConfigDO.class);
        Example.Criteria nodeCriteria = example.createCriteria();
        nodeCriteria.andEqualTo("linkId", linkId);
        return pradarLinkConfigMapper.selectCountByExample(example) > 0;
    }

    @Override
    public List<TAMDBPradarLinkConfigDO> selectAll() {
        return pradarLinkConfigMapper.selectAll();
    }

    @Override
    public Response<String> deleteLinkConfig(CalculateParam calculateParam) {
        String linkId = Md5Utils.md5(buildLinkTag(calculateParam));
        Example example = new Example(TAMDBPradarLinkConfigDO.class);
        Example.Criteria nodeCriteria = example.createCriteria();
        nodeCriteria.andEqualTo("linkId", linkId);
        if (StringUtils.isNotBlank(calculateParam.getTenantAppKey())) {
            nodeCriteria.andEqualTo("userAppKey", calculateParam.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(calculateParam.getEnvCode())) {
            nodeCriteria.andEqualTo("envCode", calculateParam.getEnvCode());
        }
        pradarLinkConfigMapper.deleteByExample(example);

        // 删除点跟边信息
        Example exampleNode = new Example(TAmdbPradarLinkNodeDO.class);
        Example.Criteria nodeCriteriaNode = exampleNode.createCriteria();
        nodeCriteriaNode.andEqualTo("linkId", linkId);
        if (StringUtils.isNotBlank(calculateParam.getTenantAppKey())) {
            nodeCriteriaNode.andEqualTo("userAppKey", calculateParam.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(calculateParam.getEnvCode())) {
            nodeCriteriaNode.andEqualTo("envCode", calculateParam.getEnvCode());
        }
        pradarLinkNodeMapper.deleteByExample(exampleNode);

        Example exampleEdge = new Example(TAmdbPradarLinkEdgeDO.class);
        Example.Criteria nodeCriteriaEdge = exampleEdge.createCriteria();
        nodeCriteriaEdge.andEqualTo("linkId", linkId);
        if (StringUtils.isNotBlank(calculateParam.getTenantAppKey())) {
            nodeCriteriaEdge.andEqualTo("userAppKey", calculateParam.getTenantAppKey());
        }
        if (StringUtils.isNotBlank(calculateParam.getEnvCode())) {
            nodeCriteriaEdge.andEqualTo("envCode", calculateParam.getEnvCode());
        }
        pradarLinkEdgeMapper.deleteByExample(exampleNode);
        return Response.success(linkId);
    }

    private String buildLinkTag(CalculateParam calculateParam) {
        if (calculateParam.getExtend() == null) {
            calculateParam.setExtend("");
        }
        return calculateParam.getServiceName() + "|" + calculateParam.getMethod() + "|" + calculateParam.getAppName() + "|" + calculateParam.getRpcType() + "|" + calculateParam.getExtend();
    }

}
