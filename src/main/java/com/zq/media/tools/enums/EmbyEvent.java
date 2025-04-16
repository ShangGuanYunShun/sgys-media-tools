package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * emby事件
 *
 * @author zhaoqiang
 * @since V1.0.0 2025-4-14
 */
@Getter
@AllArgsConstructor
public enum EmbyEvent implements IEnum<String> {

    DEEP_DELETE("deep.delete", "媒体深度删除", EmbyNotifyType.SHENYI),
    FAVORITES_UPDATE("favorites.update", "追更模式更新", EmbyNotifyType.SHENYI),
    INTRO_SKIP_UPDATE("introskip.update", "片头片尾更新", EmbyNotifyType.SHENYI);

    @JsonValue
    private final String code;
    private final String desc;
    private final EmbyNotifyType notifyType;

    @JsonCreator
    public static EmbyEvent of(String code) {
        for (EmbyEvent embyEvent : values()) {
            if (embyEvent.getCode().equals(code)) {
                return embyEvent;
            }
        }
        return null;
    }

}
