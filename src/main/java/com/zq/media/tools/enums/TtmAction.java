package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ttm触发操作
 *
 * @author zhaoqiang
 * @since V1.0.0 2024-12-23
 */
@Getter
@AllArgsConstructor
public enum TtmAction implements IEnum<String> {

    UPDATE("update", "刷新媒体库"),
    SCRAPE("scrape", "刮削媒体库");

    @JsonValue
    private final String code;
    private final String desc;
}
