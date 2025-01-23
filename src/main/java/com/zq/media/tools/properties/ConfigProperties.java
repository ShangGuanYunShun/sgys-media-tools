package com.zq.media.tools.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-11 16:48
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    /**
     * alist 配置
     */
    private Alist alist;

    /**
     * cd2 配置
     */
    private CloudDrive cloudDrive;

    /**
     * 服务器信息
     */
    private Server server;

    /**
     * 115网盘配置
     */
    private Client115 client115;

    /**
     * tinyMediaManager 配置
     */
    private Ttm ttm;

    @Getter
    @Setter
    @ToString
    public static class Alist {

        private Boolean enabled;
        /**
         * 令 牌
         */
        private String token;
        /**
         * 间隔分钟
         */
        private Integer intervalMinutes;
        /**
         * url
         */
        private String url;
        /**
         * 媒体 URL
         */
        private String mediaUrl;
        /**
         * 基本监控路径（用于刷新最新文件）
         */
        private List<String> baseMonitorPath;

        /**
         * 监控路径
         */
        private List<String> monitorPath;

        /**
         * 刮削路径
         */
        private Map<String, String> scrapPath;

        /**
         * 连续电视节目到目标文件夹的映射，比如
         * 遮天 (2023): /动漫/国产动漫/遮天 (2023)
         */
        private Map<String, String> serializedTvShow = new HashMap<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Server {
        /**
         * 文件存放基本路径
         */
        private String basePath;
    }

    @Getter
    @Setter
    @ToString
    public static class Client115 {

        private Boolean enabled;

        /**
         * 监听间隔分钟
         */
        private Integer intervalMinutes = 5;

        private String cookie;

        private String userAgent = "";

        private Integer limit = 1000;

        /**
         * 忽略文件夹
         */
        private List<String> ignoreFolders = new ArrayList<>();

    }

    @Getter
    @Setter
    @ToString
    public static class Ttm {
        private String apiKey;
        private String url;
    }

    @Getter
    @Setter
    @ToString
    public static class CloudDrive {

        private String url;

        private String username;

        private String password;

    }
}
