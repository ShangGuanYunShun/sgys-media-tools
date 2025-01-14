package com.zq.util.strm.service;

import com.zq.util.strm.dto.HandleFileDTO;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 13:03
 */
public interface IAlistService {

    /**
     * 将文件夹文件从夸克复制到115
     * 刮削夸克
     *
     * @param dicPath 文件夹路径
     */
    void copyFileQuarkTo115(String dicPath);

    /**
     * 将文件夹文件从夸克复制到115
     * 刮削115
     *
     * @param handleFile 处理文件
     */
    void copyFileQuarkTo115(HandleFileDTO handleFile);
}
