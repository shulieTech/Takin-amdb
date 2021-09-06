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


import io.shulie.amdb.dto.LinkNodeRelationDTO;
import io.shulie.amdb.entity.LinkNodeRelationDO;

public class LinkNodeRelationConvert {
    public static LinkNodeRelationDO convertRelationDO(LinkNodeRelationDTO dto) {
        final LinkNodeRelationDO relationDO = new LinkNodeRelationDO();
        relationDO.setSourceId(dto.getSourceId());
        relationDO.setSourceAppName(dto.getSourceAppName());
        relationDO.setTargetId(dto.getTargetId());
        relationDO.setTargetAppName(dto.getTargetAppName());
        relationDO.setExtInfo(dto.getExtInfo());

        return relationDO;
    }

    public static LinkNodeRelationDTO convertRelationDTO(LinkNodeRelationDO relationDO) {
        final LinkNodeRelationDTO relationDTO = new LinkNodeRelationDTO();
        relationDTO.setId(relationDO.getId());
        relationDTO.setSourceId(relationDO.getSourceId());
        relationDTO.setSourceAppName(relationDO.getSourceAppName());
        relationDTO.setTargetId(relationDO.getTargetId());
        relationDTO.setTargetAppName(relationDO.getTargetAppName());
        relationDTO.setExtInfo(relationDO.getExtInfo());
        relationDTO.setLinkId(relationDO.getLinkId());

        return relationDTO;
    }

    public static String genKey(LinkNodeRelationDO relationDO) {
        return relationDO.getSourceId() + "|" + relationDO.getTargetId();
    }

    public static String genUk(LinkNodeRelationDTO relationDO) {
        return relationDO.getSourceId() + "|" + relationDO.getTargetId();
    }
}