package com.zq.media.tools.dto.resp.emby;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.EmbyMediaType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 最爱item
 *
 * @author zhaoqiang
 * @since 1.0.0 2025-5-26
 */
@Data
public class FavoriteItemsDTO {

    @JsonProperty("Items")
    private List<ItemDTO> items;

    @JsonProperty("TotalRecordCount")
    private Integer totalRecordCount;

    @Data
    public static class ItemDTO {
        /**
         * 节目名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * 服务器唯一ID
         */
        @JsonProperty("ServerId")
        private String serverId;

        /**
         * 节目唯一ID
         */
        @JsonProperty("Id")
        private String id;

        /**
         * 播放时长（单位：Ticks）
         */
        @JsonProperty("RunTimeTicks")
        private Long runTimeTicks;

        /**
         * 是否为文件夹（系列）
         */
        @JsonProperty("IsFolder")
        private Boolean isFolder;

        /**
         * 媒体类型
         */
        @JsonProperty("Type")
        private EmbyMediaType type;

        /**
         * 用户播放数据
         */
        @JsonProperty("UserData")
        private UserData userData;

        /**
         * 播出日期（如每周几播出）
         */
        @JsonProperty("AirDays")
        private List<String> airDays;

        /**
         * 图片标签（主图、Logo 等）
         */
        @JsonProperty("ImageTags")
        private Map<String, String> imageTags;

        /**
         * 背景图标签
         */
        @JsonProperty("BackdropImageTags")
        private List<String> backdropImageTags;

    }

    @Data
    public static class UserData {

        /**
         * 未播放的项目数量
         */
        @JsonProperty("UnplayedItemCount")
        private Integer unplayedItemCount;

        /**
         * 当前播放进度（单位：Ticks）
         */
        @JsonProperty("PlaybackPositionTicks")
        private Long playbackPositionTicks;

        /**
         * 播放次数
         */
        @JsonProperty("PlayCount")
        private Integer playCount;

        /**
         * 是否收藏
         */
        @JsonProperty("IsFavorite")
        private Boolean isFavorite;

        /**
         * 是否已播放
         */
        @JsonProperty("Played")
        private Boolean played;

    }
}
