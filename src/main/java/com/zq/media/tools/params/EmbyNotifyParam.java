package com.zq.media.tools.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.dto.resp.emby.ItemRespDTO;
import com.zq.media.tools.enums.EmbyEvent;
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
    @ToString(callSuper = true)
    public static class Item extends ItemRespDTO {

        /**
         * 剧集id
         */
        @JsonProperty("SeriesId")
        private String seriesId;

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
         * 季索引号
         */
        @JsonProperty("ParentIndexNumber")
        private Integer parentIndexNumber;

        /**
         * 索引号
         */
        @JsonProperty("IndexNumber")
        private Integer indexNumber;
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