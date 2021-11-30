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
import io.shulie.amdb.common.request.link.TopologyQueryParam;
import io.shulie.amdb.entity.TAmdbPradarLinkNodeDO;
import io.shulie.amdb.mapper.PradarLinkEdgeMapper;
import io.shulie.amdb.mapper.PradarLinkNodeMapper;
import io.shulie.amdb.service.LinkUnKnowService;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * @Author: xingchen
 * @ClassName: LinkServiceImpl
 * @Package: io.shulie.amdb.service.impl
 * @Date: 2020/10/1914:19
 * @Description:
 */
@Service
public class LinkUnknowServiceImpl implements LinkUnKnowService {
    private static Logger logger = LoggerFactory.getLogger(LinkUnknowServiceImpl.class);

    private static final String UNKNOW_APP = "UNKNOWN";
    private static final String EXT_SERVER = "OUTSERVICE";
    // 1个小时
    private static final Long UNKNOWN_CLEAR_TIME = 60 * 60 * 1000L;

    @Autowired
    PradarLinkEdgeMapper pradarLinkEdgeMapper;

    @Autowired
    PradarLinkNodeMapper pradarLinkNodeMapper;

    /**
     * 修改未知应用为外部服务
     *
     * @param param
     */
    @Override
    public Response update(TopologyQueryParam param) {
        try {
            StringBuffer tags = new StringBuffer();
            tags.append(ObjectUtils.toString(param.getServiceName(), ""))
                    .append("|")
                    .append(ObjectUtils.toString(param.getMethod(), ""))
                    .append("|")
                    .append(ObjectUtils.toString(param.getAppName(), ""))
                    .append("|")
                    .append(ObjectUtils.toString(param.getRpcType(), ""))
                    .append("|")
                    .append(ObjectUtils.toString(param.getExtend(), ""));
            String linkId = Md5Utils.md5(tags.toString());
            Example example = new Example(TAmdbPradarLinkNodeDO.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("linkId", linkId);
            criteria.andEqualTo("appId", param.getId());
            if (StringUtils.isNotBlank(param.getTenantAppKey())) {
                criteria.andEqualTo("userAppKey", param.getTenantAppKey());
            }
            if (StringUtils.isNotBlank(param.getEnvCode())) {
                criteria.andEqualTo("envCode", param.getEnvCode());
            }
            TAmdbPradarLinkNodeDO update = new TAmdbPradarLinkNodeDO();
            update.setAppName(EXT_SERVER);
            pradarLinkNodeMapper.updateByExampleSelective(update, example);
            return Response.emptySuccess();
        } catch (Exception e) {
            logger.error("更新未知节点为外部服务失败", e);
            return Response.fail();
        }
    }

}
