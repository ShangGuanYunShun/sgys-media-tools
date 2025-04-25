package com.zq.media.tools.service;

import com.zq.media.tools.dto.HandleFileDTO;

import java.util.Set;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 13:03
 */
public interface IAlistService {

    /**
     * 处理云盘自动保存
     *
     * @param handleFile 处理文件
     */
    void handleCloudAutoSave(HandleFileDTO handleFile);

    /**
     * 处理目录（创建 STRM 并下载文件）
     *
     * @param mediaPath 媒体路径
     */
    void processDic(String mediaPath);

    /**
     * 查询列表文件通过目录
     *
     * @param folderPath 文件夹路径
     * @return {@link Set }<{@link String }>
     */
    Set<String> queryListFileByDic(String folderPath);
}
