package com.zq.util.strm.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    private final Integer code;
    private final String desc;

    @JsonCreator
    public static String of(Integer code) {
        for (FileCategory item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDesc();
            }
        }
        return null;
    }
}
