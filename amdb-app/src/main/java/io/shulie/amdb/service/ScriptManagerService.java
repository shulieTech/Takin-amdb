package io.shulie.amdb.service;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 16:09
 */
public interface ScriptManagerService {
    /**
     * 根据脚本类型获取脚本管理器
     *
     * @param scriptType
     * @return
     */
    ScriptManager getScriptManager(String scriptType);
}
