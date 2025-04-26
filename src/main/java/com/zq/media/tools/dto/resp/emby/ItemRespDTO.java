package com.zq.media.tools.dto.resp.emby;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.EmbyMediaType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/4/26 12:08
 */
@Getter
@Setter
@ToString
public class ItemRespDTO {

    /**
     * 名称
     */
    @JsonProperty("Name")
    private String name;

    /**
     * 原始标题
     */
    @JsonProperty("OriginalTitle")
    private String originalTitle;

    /**
     * 服务器ID
     */
    @JsonProperty("ServerId")
    private String serverId;

    /**
     * ID
     */
    @JsonProperty("Id")
    private String id;

    /**
     * 创建日期
     */
    @JsonProperty("DateCreated")
    private String dateCreated;

    /**
     * 排序名称
     */
    @JsonProperty("SortName")
    private String sortName;

    /**
     * 首映日期
     */
    @JsonProperty("PremiereDate")
    private String premiereDate;

    /**
     * 外部链接
     */
    @JsonProperty("ExternalUrls")
    private ExternalUrl[] externalUrls;

    /**
     * 路径
     */
    @JsonProperty("Path")
    private String path;

    /**
     * 简介
     */
    @JsonProperty("Overview")
    private String overview;

    /**
     * 标签行
     */
    @JsonProperty("Taglines")
    private String[] taglines;

    /**
     * 类型
     */
    @JsonProperty("Genres")
    private String[] genres;

    /**
     * 社区评分
     */
    @JsonProperty("CommunityRating")
    private Double communityRating;

    /**
     * 运行时间（以毫秒为单位）
     */
    @JsonProperty("RunTimeTicks")
    private Long runTimeTicks;

    /**
     * 文件名
     */
    @JsonProperty("FileName")
    private String fileName;

    /**
     * 生产年份
     */
    @JsonProperty("ProductionYear")
    private Integer productionYear;

    /**
     * 远程预告片
     */
    @JsonProperty("RemoteTrailers")
    private String[] remoteTrailers;

    /**
     * 提供商ID
     */
    @JsonProperty("ProviderIds")
    private ProviderIds providerIds;

    /**
     * 是否为文件夹
     */
    @JsonProperty("IsFolder")
    private Boolean isFolder;

    /**
     * 父级ID
     */
    @JsonProperty("ParentId")
    private String parentId;

    /**
     * 类型
     */
    @JsonProperty("Type")
    private EmbyMediaType type;

    /**
     * 主图像标签
     */
    @JsonProperty("SeriesPrimaryImageTag")
    private String seriesPrimaryImageTag;

    /**
     * 工作室
     */
    @JsonProperty("Studios")
    private Studio[] studios;

    /**
     * 类型项
     */
    @JsonProperty("GenreItems")
    private GenreItem[] genreItems;

    /**
     * 标签项
     */
    @JsonProperty("TagItems")
    private TagItem[] tagItems;

    /**
     * 子项数量
     */
    @JsonProperty("ChildCount")
    private Integer childCount;

    /**
     * 状态
     */
    @JsonProperty("Status")
    private String status;

    /**
     * 播放日
     */
    @JsonProperty("AirDays")
    private String[] airDays;

    /**
     * 主图宽高比
     */
    @JsonProperty("PrimaryImageAspectRatio")
    private Double primaryImageAspectRatio;

    /**
     * 图像标签
     */
    @JsonProperty("ImageTags")
    private ImageTags imageTags;

    /**
     * 背景图像标签
     */
    @JsonProperty("BackdropImageTags")
    private String[] backdropImageTags;

    /**
     * 外部链接静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class ExternalUrl {
        /**
         * 名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * URL
         */
        @JsonProperty("Url")
        private String url;
    }

    /**
     * 提供商ID静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class ProviderIds {
        /**
         * TheTVDB ID
         */
        @JsonProperty("Tvdb")
        private String tvdb;

        /**
         * IMDb ID
         */
        @JsonProperty("Imdb")
        private String imdb;

        /**
         * TMDB ID
         */
        @JsonProperty("Tmdb")
        private String tmdb;
    }

    /**
     * 工作室静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class Studio {
        /**
         * 名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * ID
         */
        @JsonProperty("Id")
        private String id;
    }

    /**
     * 类型项静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class GenreItem {
        /**
         * 名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * ID
         */
        @JsonProperty("Id")
        private String id;
    }

    /**
     * 标签项静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class TagItem {
        /**
         * 名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * ID
         */
        @JsonProperty("Id")
        private String id;
    }

    /**
     * 图像标签静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class ImageTags {
        /**
         * 主图标签
         */
        @JsonProperty("Primary")
        private String primary;

        /**
         * 背景标签
         */
        @JsonProperty("Banner")
        private String banner;

        /**
         * Logo标签
         */
        @JsonProperty("Logo")
        private String logo;
    }
    
}
