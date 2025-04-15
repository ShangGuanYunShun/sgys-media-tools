package com.zq.media.tools.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * emby通知类型
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 9:42
 */
@Getter
@AllArgsConstructor
public enum EmbyNotifyType {

    EMBY_DEFAULT("emby默认"),
    SHENYI("神医助手");

    private final String desc;

}
