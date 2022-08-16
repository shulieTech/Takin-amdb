package io.shulie.amdb.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.CountDownLatch;

import io.shulie.amdb.service.log.PushLogService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 压测日志上传任务
 *
 * @author -
 */
@Data
@Slf4j
public class PressureTraceCompensateTask implements Runnable {

    private PushLogService pushLogService;
    private File file;
    private String version;
    private String address;
    private final CountDownLatch latch;
    private static final int MAX_WAIT_TIME = 1500;

    public PressureTraceCompensateTask(File file, PushLogService pushLogService, String version, String address, CountDownLatch latch) {
        this.pushLogService = pushLogService;
        this.file = file;
        this.version = version;
        this.address = address;
        this.latch = latch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        uploadPtlFile();
        latch.countDown();
    }

    /**
     * 上传PTL文件
     */
    private void uploadPtlFile() {
        if (file == null) {
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.lines().forEach(line -> pushLogService.pushLogToAmdb(file.getAbsolutePath(), line, version, address));
        } catch (Exception ignore) {
        }
    }
}
