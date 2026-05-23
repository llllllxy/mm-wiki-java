package org.tinycloud.mmwiki.util;

import java.util.Locale;

/**
 * 文件名和文件路径处理工具类。
 */
public final class FileUtils {

    /**
     * 阻止工具类被实例化。
     */
    private FileUtils() {
    }

    /**
     * 获取文件名中的扩展名，返回值包含点号并统一转成小写。
     *
     * @param fileName 文件名
     * @return 文件扩展名；没有扩展名时返回空字符串
     */
    public static String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }
}
