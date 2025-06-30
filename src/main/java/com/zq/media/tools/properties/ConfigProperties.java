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
     * API 速率限制
     */
    private Integer apiRateLimit = 1;

    /**
     * 是否下载媒体文件
     */
    private Boolean downloadMediaFile = true;

    /**
     * 对 strm 路径进行编码
     */
    private Boolean encodeStrmPath = true;

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
    private Driver115 driver115;

    /**
     * 夸克网盘配置
     */
    private DriverQuark driverQuark;

    /**
     * 天翼云盘配置
     */
    private DriverCloud189 driverCloud189;

    /**
     * tinyMediaManager 配置
     */
    private Ttm ttm;

    /**
     * emby配置
     */
    private Emby emby;

    /**
     * 剧集组
     */
    private List<String> episodeGroup = new ArrayList<>();

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
         * 115网盘路径
         */
        private String driver115Path;

        /**
         * 媒体路径
         */
        private List<String> mediaPath = new ArrayList<>();

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

        /**
         * 115网盘本地路径
         */
        private String driver115Path;
    }

    @Getter
    @Setter
    @ToString
    public static class Driver115 {

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
    public static class DriverQuark {

        /**
         * 处理文件夹
         */
        private List<String> handleFolders = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class DriverCloud189 {

        /**
         * 处理文件夹
         */
        private List<String> handleFolders = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Ttm {

        private Boolean enabled;

        private String apiKey;

        private String url;

        /**
         * 刮削时间：秒
         */
        private Integer scrapTime = 300;

        /**
         * 是否启用了重命名
         */
        private Boolean enableRename = true;
    }

    @Getter
    @Setter
    @ToString
    public static class CloudDrive {

        private Boolean enabled;

        private String url;

        private String username;

        private String password;

    }

    @Getter
    @Setter
    @ToString
    public static class Emby {

        private String url = "http://127.0.0.1:8096";

        private String apiKey;

        private String userId;
    }
}
