package com.zq.media.tools.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.FileCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 行为详情
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-3-31 13:56
 */
@Getter
@Setter
@ToString
public class BehaviorDetailRespDTO {

    /**
     * 条目 ID
     */
    private String id;

    /**
     * 文件或目录 ID
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * 父目录 ID
     */
    @JsonProperty("parent_id")
    private String parentId;

    /**
     * 选取代码
     */
    @JsonProperty("pick_code")
    private String pickCode;

    /**
     * SHA1
     */
    private String sha1;

    /**
     * 文件或目录名称
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 类别（0 表示目录，1 表示文件）
     */
    @JsonProperty("file_category")
    private FileCategory fileCategory;

    /**
     * 原文件或目录名称
     */
    @JsonProperty("old_file_name")
    private String oldFileName;

    /**
     * 文件扩展名
     */
    @JsonProperty("ico")
    private String ext;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    private Long updateTime;

}
