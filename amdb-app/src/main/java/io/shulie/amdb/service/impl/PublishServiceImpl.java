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

import io.shulie.amdb.entity.TAmdbPublishInfo;
import io.shulie.amdb.mapper.PublishInfoMapper;
import io.shulie.amdb.service.PublishService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PublishServiceImpl implements PublishService {
    @Resource
    private PublishInfoMapper publishInfoMapper;

    @Override
    public int insert(TAmdbPublishInfo tAmdbApp) {
        return publishInfoMapper.insert(tAmdbApp);
    }

    @Override
    public int insertBatch(List<TAmdbPublishInfo> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            insert(tAmdbApp);
        });
        return 1;
    }

    @Override
    public int update(TAmdbPublishInfo tAmdbApp) {
        return publishInfoMapper.updateByPrimaryKey(tAmdbApp);
    }

    @Override
    public int updateBatch(List<TAmdbPublishInfo> tAmdbApps) {
        tAmdbApps.forEach(tAmdbApp -> {
            update(tAmdbApp);
        });
        return 1;
    }

    @Override
    public int delete(TAmdbPublishInfo tAmdbApp) {
        return publishInfoMapper.deleteByPrimaryKey(tAmdbApp.getId());
    }

    @Override
    public TAmdbPublishInfo select(TAmdbPublishInfo tAmdbApp) {
        return publishInfoMapper.selectByPrimaryKey(tAmdbApp.getId());
    }
}
