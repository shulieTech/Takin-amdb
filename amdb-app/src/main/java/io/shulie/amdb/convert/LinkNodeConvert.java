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

import io.shulie.amdb.dto.LinkNodeDTO;
import io.shulie.amdb.entity.LinkNodeDO;

public class LinkNodeConvert {
    public static LinkNodeDO convertLinkNodeDO(LinkNodeDTO dto) {
        final LinkNodeDO linkNodeDO = new LinkNodeDO();
        linkNodeDO.setAppName(dto.getAppName());
        linkNodeDO.setNodeId(dto.getNodeId());
        linkNodeDO.setNodeName(dto.getNodeName());
        linkNodeDO.setNodeLevel(dto.getNodeLevel());
        linkNodeDO.setEntrance(dto.getEntrance());
        linkNodeDO.setEntranceType(dto.getEntranceType());
        linkNodeDO.setExtInfo(dto.getExtInfo());
        linkNodeDO.setParent(dto.getParent());
        return linkNodeDO;
    }

    public static LinkNodeDTO convertLinkNodeDTO(LinkNodeDO linkNodeDO) {
        final LinkNodeDTO linkNodeDTO = new LinkNodeDTO();
        linkNodeDTO.setAppName(linkNodeDO.getAppName());
        linkNodeDTO.setNodeId(linkNodeDO.getNodeId());
        linkNodeDTO.setNodeName(linkNodeDO.getNodeName());
        linkNodeDTO.setId(linkNodeDO.getId());
        linkNodeDTO.setLinkId(linkNodeDO.getLinkId());
        linkNodeDTO.setEntrance(linkNodeDO.getEntrance());
        linkNodeDTO.setEntranceType(linkNodeDO.getEntranceType());
        linkNodeDTO.setNodeLevel(linkNodeDO.getNodeLevel());
        linkNodeDTO.setExtInfo(linkNodeDO.getExtInfo());
        linkNodeDTO.setParent(linkNodeDO.getParent());
        return linkNodeDTO;
    }

    public static String genKey(LinkNodeDO nodeDO) {
        return nodeDO.getLinkId() + "|" + nodeDO.getAppName() + "|" + nodeDO.getEntranceType() + "|" + nodeDO.getEntrance();
    }

    public static String genUk(LinkNodeDTO nodeDO) {
        return nodeDO.getLinkId() + "|" + nodeDO.getAppName() + "|" + nodeDO.getEntranceType() + "|" + nodeDO.getEntrance();
    }
}