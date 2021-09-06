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

import io.shulie.amdb.dto.LinkDTO;
import io.shulie.amdb.entity.LinkDO;

public class LinkConvert {
    /**
     * 转换DO
     *
     * @param linkDTO
     * @return
     */
    public static LinkDO convertLinkDO(LinkDTO linkDTO) {
        final LinkDO linkDO = new LinkDO();
        linkDO.setLinkName(linkDTO.getLinkName());
        linkDO.setEntrance(linkDTO.getEntrance());
        linkDO.setEntranceType(linkDTO.getEntranceType());
        linkDO.setExtInfo(linkDTO.getExtInfo());
        linkDO.setRemark(linkDTO.getRemark());

        return linkDO;
    }

    /**
     * 转换DTO
     *
     * @param linkDO
     * @return
     */
    public static LinkDTO convertLinkDTO(LinkDO linkDO) {
        final LinkDTO linkDTO = new LinkDTO();
        linkDTO.setLinkName(linkDO.getLinkName());
        linkDTO.setEntrance(linkDO.getEntrance());
        linkDTO.setEntranceType(linkDO.getEntranceType());
        linkDTO.setExtInfo(linkDO.getExtInfo());
        linkDTO.setType(linkDO.getType());
        linkDTO.setId(linkDO.getId());
        linkDTO.setRemark(linkDO.getRemark());
        return linkDTO;
    }
}