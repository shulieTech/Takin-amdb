package io.shulie.amdb.service;

import io.shulie.amdb.entity.ScriptDO;

/**
 * 清洗脚本服务
 *
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 15:41
 */
public interface ScriptService {

    /**
     * 通过编码获取脚本
     *
     * @param scriptCode 脚本编码
     * @return
     */
    ScriptDO getScriptByCode(String scriptCode);
}
