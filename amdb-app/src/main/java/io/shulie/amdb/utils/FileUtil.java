package io.shulie.amdb.utils;

/**
 * @author Sunsy
 * @date 2022/5/19
 * @apiNode
 * @email sunshiyu@shulie.io
 */

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

public class FileUtil {

    public static List<File> getFileList(String path, String keyword) {
        File dir = new File(path);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        List<File> filelist = Lists.newArrayList();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    continue;
                } else {
                    // 判断文件名
                    if (fileName.contains(keyword)) {
                        filelist.add(files[i]);
                    }
                }
            }
        }
        return filelist;
    }

    public static Boolean checkFileKeywordExists(String path, String keyword) {
        File dir = new File(path);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    continue;
                } else {
                    // 判断文件名
                    if (fileName.contains(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}

