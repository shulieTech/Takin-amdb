package io.shulie.amdb.service.log;

import io.shulie.amdb.common.request.trodata.LogCompensateCallbackRequest;

/**
 * @author Sunsy
 * @date 2022/5/19
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public interface PushLogService {


    /**
     * 初始化
     *
     * @return 初始化是否成功
     */
    boolean init();


    /**
     * 停止
     */
    void stop();

    /**
     * 向AMDB推送数据
     *
     * @param filePath 文件路径
     * @param traceLine    数据
     * @param version 版本
     */
    void pushLogToAmdb(String filePath, String traceLine, String version, String address);


    void callbackTakin(String callbackUrl, LogCompensateCallbackRequest request);

}
