package io.shulie.amdb.task;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.shulie.amdb.service.log.PushLogService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private Cache<String, Long> positionCache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(10, TimeUnit.MINUTES).build();
    private static final Long MAX_PUSH_SIZE = 1024L * 1024L;
    private static final int MAX_WAIT_TIME = 1500;
    private final CountDownLatch latch;

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
        uploadPtlFile(file);
        latch.countDown();
    }

    /**
     * 上传PTL文件
     */
    private void uploadPtlFile(File ptlFile) {
        if (ptlFile == null) {
            return;
        }
        //去掉特殊字符
        String subFileName = ptlFile.getName().replaceAll("\\.", "");
        byte[] data;
        FileFetcher fileFetcher;
        try {
            fileFetcher = new FileFetcher(ptlFile);
        } catch (FileNotFoundException e) {
            return;
        }

        AtomicInteger i = new AtomicInteger(0);
        while (true) {
            try {
                //+1;
                i.getAndIncrement();
                Long position = getPosition(subFileName);
                data = readFile(ptlFile, subFileName, position, ptlFile.getAbsolutePath(), fileFetcher, MAX_PUSH_SIZE);
                // 如果没有读到数据需要判断是不是报告已经完成，如果报告已经完成说明任务已经结束，并且日志都已经推送完成，这时就可以结束这个文件的推送任务
                //否则下一次继续读取
                if (data == null || data.length == 0) {
                    if (true) {
                        long fileSize = getFileSize(ptlFile);
                        long lastSize = Math.max(fileSize - position, MAX_PUSH_SIZE);
                        data = readFile(ptlFile, subFileName, position, ptlFile.getAbsolutePath(), fileFetcher,
                                lastSize);
                        if (data != null && data.length > 0) {
                            pushLogService.pushLogToAmdb(data, version, address);
                        } else if (lastSize > 0) {
                            TimeUnit.SECONDS.sleep(10);
                            data = readFile(ptlFile, subFileName, position, ptlFile.getAbsolutePath(), fileFetcher,
                                    lastSize);
                            pushLogService.pushLogToAmdb(data, version, address);
                        }
                        position = getPosition(subFileName);
                        fileFetcher.close();
                        break;
                    }
                }
                pushLogService.pushLogToAmdb(data, version, address);
                //每次推成功 i还原0
                i.set(0);
            } catch (Throwable e) {
                //重试次数最多3次
                if (i.get() == 3) {
                    cleanCache();
                    return;
                }
            }
        }
    }

    private void cleanCache() {
        //help gc
        positionCache = null;
    }


    /**
     * 获取文件位点
     *
     * @param fileName 文件名称
     * @return -
     */
    private Long getPosition(String fileName) {
        Long position = positionCache.getIfPresent(fileName);
        //从本地缓存获取位点
        if (Objects.isNull(position)) {
            return 0L;
        } else {
            return position;
        }
    }

    /**
     * 获取文件大小
     *
     * @param file 文件路径
     * @return 除非文件存在且是一个文件(不是文件夹), 否则返回0
     */

    private long getFileSize(File file) {
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }


    /**
     * 从指定定行数开始，读取文件的剩余内容
     *
     * @param position 点位信息
     * @param filePath 文件路径
     * @return -
     * @throws IOException IO异常
     */
    private byte[] readFile(File file, String subFileName, Long position, String filePath, FileFetcher fileFetcher,
                            long pushSize)
            throws IOException {
        if (!file.exists() || !file.isFile()) {
            log.warn("上传压测明细日志--读取文件【{}】失败：文件不存在或非文件", filePath);
            return null;
        }
        byte[] data = fileFetcher.read(position, pushSize);
        //已经读到当前行，等待文件继续写入
        if (data == null || data.length == 0) {
            return data;
        }
        log.debug("上传压测明细日志--读取到文件大小:【{}】", data.length);
        position += data.length;
        cacheFileUploadedPosition(subFileName, position);
        return data;
    }

    private void cacheFileUploadedPosition(String subFileName, Long position) {
        positionCache.put(subFileName, position);
    }


}
