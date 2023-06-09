package io.shulie.amdb.service.impl;

import io.shulie.amdb.service.ScriptManager;
import io.shulie.amdb.service.ScriptManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 16:10
 */
@Component
@Slf4j
public class ScriptManagerServiceImpl implements ScriptManagerService {
    @Resource
    private Map<String, ScriptManager> scriptManagers;


    @Override

    public ScriptManager getScriptManager(String scriptType) {
        ScriptManager scriptManager = scriptManagers.get(scriptType);
        if (scriptManager == null) {
            log.error("can't found script manager from {}", scriptType);
        }
        return scriptManager == null ? NoActionScriptManager.INSTANCE : scriptManager;
    }
}
