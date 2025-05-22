package com.zq.media.tools.util;

import org.dromara.hutool.core.array.ArrayUtil;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 17:28
 */
public class FileUtil {

    /**
     * 判断指定文件是否图像
     *
     * @param fileType 文件类型
     * @return boolean
     */
    public static boolean isImage(String fileType) {
        String[] imageSuffix = new String[]{"jpg", ".peg", "png", "gif", "bmp", "webp"};
        return ArrayUtil.contains(imageSuffix, fileType);
    }
}
