package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.hutool.core.array.ArrayUtil;

/**
 * 文件类型
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 11:44
 */
@Getter
@AllArgsConstructor
public enum FileCategory implements IEnum<Integer> {

    CATALOG(0, "目录"),
    FILE(1, "文件");

    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator
    public static FileCategory of(Integer code) {
        return ArrayUtil.firstMatch(fileCategory -> fileCategory.getCode().equals(code), values());
    }

}
