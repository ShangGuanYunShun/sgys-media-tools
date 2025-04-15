package com.zq.media.tools.service;

import com.zq.media.tools.params.EmbyNotifyParam;

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

    /**
     * 接收 Emby 的神医通知
     *
     * @param embyNotifyParam emby 通知参数
     */
    void receiveEmbyFromShenYi(EmbyNotifyParam embyNotifyParam);
}
