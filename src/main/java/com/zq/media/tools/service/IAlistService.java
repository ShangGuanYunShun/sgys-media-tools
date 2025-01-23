package com.zq.media.tools.service;

import com.zq.media.tools.dto.HandleFileDTO;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 13:03
 */
public interface IAlistService {

    /**
     * 将文件夹文件从夸克复制到115
     * 刮削115
     *
     * @param handleFile 处理文件
     */
    void copyFileQuarkTo115(HandleFileDTO handleFile);
}
