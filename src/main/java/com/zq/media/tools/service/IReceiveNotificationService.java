package com.zq.media.tools.service;

/**
 * 接收通知service
 *
 * @author zhaoqiang
 * @since V1.0.0 2025/1/8
 */
public interface IReceiveNotificationService {
    /**
     * 接收夸克自动保存
     *
     * @param content 内容
     */
    void receiveQuarkAutoSave(String content);
}
