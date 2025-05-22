package com.zq.media.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zq.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 115生活操作类型
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 15:28
 */
@Getter
@AllArgsConstructor
public enum BehaviorType implements IEnum<String> {

    ACCOUNT_SECURITY("account_security", "账号安全"),

    /** 浏览文档 */
    BROWSE_DOCUMENT("browse_document", "浏览文档"),

    /** 浏览图片 */
    BROWSE_IMAGE("browse_image", "浏览图片"),

    /** 浏览音频 */
    BROWSE_AUDIO("browse_audio", "浏览音频"),

    /** 浏览视频 */
    BROWSE_VIDEO("browse_video", "浏览视频"),

    /** 新增目录 */
    NEW_FOLDER("new_folder", "新增目录"),

    /** 复制目录 */
    COPY_FOLDER("copy_folder", "复制目录"),

    /** 目录改名 */
    FOLDER_RENAME("folder_rename", "目录改名"),

    /** 目录设置标签 */
    FOLDER_LABEL("folder_label", "目录设置标签"),

    /** 设置星标 */
    STAR_FILE("star_file", "设置星标"),

    /** 移动文件或目录（不包括图片） */
    MOVE_FILE("move_file", "移动文件或目录"),

    /** 移动图片 */
    MOVE_IMAGE_FILE("move_image_file", "移动图片"),

    /** 删除文件或目录 */
    DELETE_FILE("delete_file", "删除文件或目录"),

    /** 上传文件 */
    UPLOAD_FILE("upload_file", "上传文件"),

    /** 上传图片 */
    UPLOAD_IMAGE_FILE("upload_image_file", "上传图片"),

    /** 接收文件 */
    RECEIVE_FILES("receive_files", "接收文件"),

    /** 文件改名（未实现） */
    RENAME_FILE("rename_file", "文件改名"),

    /** 复制文件（未实现） */
    COPY_FILE("copy_file", "复制文件");

    @JsonValue
    private final String code;
    private final String desc;

    @JsonCreator
    public static BehaviorType of(String code) {
        for (BehaviorType type : BehaviorType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

