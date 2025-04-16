package com.zq.media.tools.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.EmbyEvent;
import com.zq.media.tools.enums.EmbyMediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * emby通知请求参数
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-14 15:59
 */
@Getter
@Setter
@ToString
@Schema(description = "emby通知请求参数")
public class EmbyNotifyParam {

    /**
     * 标题
     */
    @JsonProperty("Title")
    private String title;

    /**
     * 描述信息
     */
    @JsonProperty("Description")
    private String description;

    /**
     * 事件日期
     */
    @JsonProperty("Date")
    private String date;

    /**
     * 事件类型
     */
    @JsonProperty("Event")
    private EmbyEvent event;

    /**
     * 严重性
     */
    @JsonProperty("Severity")
    private String severity;

    /**
     * 用户信息
     */
    @JsonProperty("User")
    private User user;

    /**
     * 项目信息
     */
    @JsonProperty("Item")
    private Item item;

    /**
     * 服务器信息
     */
    @JsonProperty("Server")
    private Server server;

    /**
     * 用户信息静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class User {
        /**
         * 用户名
         */
        @JsonProperty("Name")
        private String name;

        /**
         * 用户ID
         */
        @JsonProperty("Id")
        private String id;
    }

    /**
     * 项目信息静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class Item {
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
         * 剧集名称
         */
        @JsonProperty("SeriesName")
        private String seriesName;

        /**
         * 季名称
         */
        @JsonProperty("SeasonName")
        private String seasonName;

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
         * 索引号
         */
        @JsonProperty("IndexNumber")
        private Integer indexNumber;
    }

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

    /**
     * 服务器信息静态内部类
     */
    @Getter
    @Setter
    @ToString
    public static class Server {
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

        /**
         * 版本
         */
        @JsonProperty("Version")
        private String version;
    }
}