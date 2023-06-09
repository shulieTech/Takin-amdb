package io.shulie.amdb.service.impl;

import io.shulie.amdb.entity.ScriptDO;
import io.shulie.amdb.mapper.ScriptMapper;
import io.shulie.amdb.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 15:42
 */
@Component
@Slf4j
public class ScriptServiceImpl implements ScriptService {
    @Resource
    private ScriptMapper scriptMapper;

    @Override
    public ScriptDO getScriptByCode(String scriptCode) {
        return scriptMapper.getScriptByCode(scriptCode);
    }
}
