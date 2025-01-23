package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 17:26
 */
@Getter
@AllArgsConstructor
public enum TtmScopeName implements IEnum<String> {

    NEW("new", "未处理的媒体"),
    PATH("path", "指定路径的媒体"),
    SINGLE("single", "指定单个数据源的媒体"),
    DATASOURCE("dataSource", "指定数据源的媒体"),
    UN_SCRAPED("unscraped", "未刮削的媒体"),
    ALL("all", "所有媒体");

    @JsonValue
    private final String code;
    private final String desc;
}
