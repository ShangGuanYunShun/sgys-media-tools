package com.zq.media.tools.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.BehaviorType;
import com.zq.media.tools.enums.FileCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 表示 life_list 接口的响应结构
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:07
 */
@Getter
@Setter
@ToString
public class LifeListRespDTO {

    /**
     * 请求状态
     */
    private boolean state;

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 包含实际事件列表数据的对象
     */
    private LifeListDataDTO data;

    /**
     * LifeListDataDTO 表示 life_list 数据字段的结构。
     */
    @Getter
    @Setter
    @ToString
    public static class LifeListDataDTO {

        /**
         * 事件数量
         */
        private int count;

        /**
         * 事件行为列表
         */
        private List<BehaviorDTO> list;

        /**
         * 上一条数据，主要用于分页请求
         */
        @JsonProperty("last_data")
        private String lastData;
    }

    /**
     * BehaviorDTO 表示单个行为事件的结构。
     */
    @Getter
    @Setter
    @ToString
    public static class BehaviorDTO {

        /**
         * 总条目数
         */
        private int total;

        /**
         * 更新时间
         */
        @JsonProperty("update_time")
        private long updateTime;

        /**
         * 行为类型
         */
        @JsonProperty("behavior_type")
        private BehaviorType behaviorType;

        /**
         * 行为条目列表
         */
        private List<ItemDTO> items;
    }

    /**
     * ItemDTO 表示行为条目中的具体文件或目录信息。
     */
    @Getter
    @Setter
    @ToString
    public static class ItemDTO {

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
         * 原文件或目录名称
         */
        @JsonProperty("old_file_name")
        private String oldFileName;


        /**
         * 类别（0 表示目录，1 表示文件）
         */
        @JsonProperty("file_category")
        private FileCategory fileCategory;

        /**
         * 文件扩展名
         */
        @JsonProperty("ico")
        private String ext;
    }
}
