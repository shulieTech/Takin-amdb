package io.shulie.amdb.service;

import io.shulie.amdb.entity.ScriptDO;

/**
 * 脚本管理器
 *
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 16:09
 */
public interface ScriptManager {

    /**
     * 执行脚本
     *
     * @param scriptDO 脚本
     * @param source   源数据
     * @param <S>      源数据
     * @param <T>      结果数据
     * @return
     */
    <S, T> T executeScript(ScriptDO scriptDO, S source);
}
