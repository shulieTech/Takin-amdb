package io.shulie.amdb.service.impl;

import io.shulie.amdb.entity.ScriptDO;
import io.shulie.amdb.service.ScriptManager;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 16:16
 */
public class NoActionScriptManager implements ScriptManager {
    public final static NoActionScriptManager INSTANCE = new NoActionScriptManager();

    @Override
    public <S, T> T executeScript(ScriptDO scriptDO, S source) {
        return null;
    }
}
