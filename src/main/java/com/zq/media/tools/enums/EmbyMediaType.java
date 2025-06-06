package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.hutool.core.array.ArrayUtil;

/**
 * emby媒体类型
 *
 * @author zhaoqiang
 * @since V1.0.0 2025-4-14
 */
@Getter
@AllArgsConstructor
public enum EmbyMediaType implements IEnum<String> {

    SERIES("Series", "电视节目"),
    SEASON("Season", "季"),
    EPISODE("Episode", "剧集"),
    MOVIE("Movie", "电影");

    @JsonValue
    private final String code;
    private final String desc;

    @JsonCreator
    public static EmbyMediaType of(String code) {
        return ArrayUtil.firstMatch(mediaType -> mediaType.getCode().equals(code), values());
    }

}
