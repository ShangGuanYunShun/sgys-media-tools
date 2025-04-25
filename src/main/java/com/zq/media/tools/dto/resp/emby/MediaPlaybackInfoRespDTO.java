package com.zq.media.tools.dto.resp.emby;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 媒体播放信息
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-24 14:17
 */
@Getter
@Setter
@ToString
public class MediaPlaybackInfoRespDTO {

    /**
     * 媒体源列表
     */
    @JsonProperty("MediaSources")
    private List<MediaSource> mediaSources;

    /**
     * 播放会话 ID
     */
    @JsonProperty("PlaySessionId")
    private String playSessionId;

    /**
     * 媒体源实体类
     */
    @Getter
    @Setter
    @ToString
    public static class MediaSource {

        /**
         * 章节列表
         */
        @JsonProperty("Chapters")
        private List<Chapter> chapters;

        /**
         * 协议类型
         */
        @JsonProperty("Protocol")
        private String protocol;

        /**
         * 媒体源 ID
         */
        @JsonProperty("Id")
        private String id;

        /**
         * 媒体路径
         */
        @JsonProperty("Path")
        private String path;

        /**
         * 媒体类型
         */
        @JsonProperty("Type")
        private String type;

        /**
         * 容器格式
         */
        @JsonProperty("Container")
        private String container;

        /**
         * 文件大小
         */
        @JsonProperty("Size")
        private Long size;

        /**
         * 媒体名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * 是否为远程媒体
         */
        @JsonProperty("IsRemote")
        private boolean isRemote;

        /**
         * 是否包含混合协议
         */
        @JsonProperty("HasMixedProtocols")
        private boolean hasMixedProtocols;

        /**
         * 总时长（以 ticks 为单位）
         */
        @JsonProperty("RunTimeTicks")
        private Long runTimeTicks;

        /**
         * 是否支持转码
         */
        @JsonProperty("SupportsTranscoding")
        private boolean supportsTranscoding;

        /**
         * 是否支持直接流媒体
         */
        @JsonProperty("SupportsDirectStream")
        private boolean supportsDirectStream;

        /**
         * 是否支持直接播放
         */
        @JsonProperty("SupportsDirectPlay")
        private boolean supportsDirectPlay;

        /**
         * 是否为无限流
         */
        @JsonProperty("IsInfiniteStream")
        private boolean isInfiniteStream;

        /**
         * 是否需要打开操作
         */
        @JsonProperty("RequiresOpening")
        private boolean requiresOpening;

        /**
         * 是否需要关闭操作
         */
        @JsonProperty("RequiresClosing")
        private boolean requiresClosing;

        /**
         * 是否需要循环
         */
        @JsonProperty("RequiresLooping")
        private boolean requiresLooping;

        /**
         * 是否支持探测
         */
        @JsonProperty("SupportsProbing")
        private boolean supportsProbing;

        /**
         * 媒体流列表
         */
        @JsonProperty("MediaStreams")
        private List<MediaStream> mediaStreams;

        /**
         * 格式列表
         */
        @JsonProperty("Formats")
        private List<String> formats;

        /**
         * 比特率
         */
        @JsonProperty("Bitrate")
        private Integer bitrate;

        /**
         * 必需的 HTTP 头部信息
         */
        @JsonProperty("RequiredHttpHeaders")
        private Object requiredHttpHeaders;

        /**
         * 直接流媒体 URL
         */
        @JsonProperty("DirectStreamUrl")
        private String directStreamUrl;

        /**
         * 是否在直接流媒体 URL 中添加 API 密钥
         */
        @JsonProperty("AddApiKeyToDirectStreamUrl")
        private boolean addApiKeyToDirectStreamUrl;

        /**
         * 转码 URL
         */
        @JsonProperty("TranscodingUrl")
        private String transcodingUrl;

        /**
         * 转码子协议
         */
        @JsonProperty("TranscodingSubProtocol")
        private String transcodingSubProtocol;

        /**
         * 转码容器格式
         */
        @JsonProperty("TranscodingContainer")
        private String transcodingContainer;

        /**
         * 是否以原始帧率读取
         */
        @JsonProperty("ReadAtNativeFramerate")
        private boolean readAtNativeFramerate;

        /**
         * 默认音频流索引
         */
        @JsonProperty("DefaultAudioStreamIndex")
        private Integer defaultAudioStreamIndex;

        /**
         * 媒体项 ID
         */
        @JsonProperty("ItemId")
        private String itemId;

    }

    @Getter
    @Setter
    @ToString
    public static class Chapter {

        /**
         * 开始位置（以 ticks 为单位）
         */
        @JsonProperty("StartPositionTicks")
        private Long startPositionTicks;

        /**
         * 章节名称
         */
        @JsonProperty("Name")
        private String name;

        /**
         * 标记类型
         */
        @JsonProperty("MarkerType")
        private String markerType;

        /**
         * 章节索引
         */
        @JsonProperty("ChapterIndex")
        private Integer chapterIndex;
    }

    @Getter
    @Setter
    @ToString
    public static class MediaStream {

        /**
         * 编解码器
         */
        @JsonProperty("Codec")
        private String codec;

        /**
         * 编解码器标签
         */
        @JsonProperty("CodecTag")
        private String codecTag;

        /**
         * 语言
         */
        @JsonProperty("Language")
        private String language;

        /**
         * 时间基准
         */
        @JsonProperty("TimeBase")
        private String timeBase;

        /**
         * 视频范围
         */
        @JsonProperty("VideoRange")
        private String videoRange;

        /**
         * 显示标题
         */
        @JsonProperty("DisplayTitle")
        private String displayTitle;

        /**
         * 是否为隔行扫描
         */
        @JsonProperty("IsInterlaced")
        private boolean isInterlaced;

        /**
         * 比特率
         */
        @JsonProperty("BitRate")
        private Integer bitRate;

        /**
         * 比特深度
         */
        @JsonProperty("BitDepth")
        private Integer bitDepth;

        /**
         * 参考帧数
         */
        @JsonProperty("RefFrames")
        private Integer refFrames;

        /**
         * 是否为默认流
         */
        @JsonProperty("IsDefault")
        private boolean isDefault;

        /**
         * 是否为强制流
         */
        @JsonProperty("IsForced")
        private boolean isForced;

        /**
         * 是否为听力障碍辅助流
         */
        @JsonProperty("IsHearingImpaired")
        private boolean isHearingImpaired;

        /**
         * 高度（视频流）
         */
        @JsonProperty("Height")
        private Integer height;

        /**
         * 宽度（视频流）
         */
        @JsonProperty("Width")
        private Integer width;

        /**
         * 平均帧率
         */
        @JsonProperty("AverageFrameRate")
        private Double averageFrameRate;

        /**
         * 实际帧率
         */
        @JsonProperty("RealFrameRate")
        private Double realFrameRate;

        /**
         * 配置文件
         */
        @JsonProperty("Profile")
        private String profile;

        /**
         * 流类型
         */
        @JsonProperty("Type")
        private String type;

        /**
         * 宽高比
         */
        @JsonProperty("AspectRatio")
        private String aspectRatio;

        /**
         * 流索引
         */
        @JsonProperty("Index")
        private Integer index;

        /**
         * 是否为外部流
         */
        @JsonProperty("IsExternal")
        private boolean isExternal;

        /**
         * 是否为文本字幕流
         */
        @JsonProperty("IsTextSubtitleStream")
        private boolean isTextSubtitleStream;

        /**
         * 是否支持外部流
         */
        @JsonProperty("SupportsExternalStream")
        private boolean supportsExternalStream;

        /**
         * 协议类型
         */
        @JsonProperty("Protocol")
        private String protocol;

        /**
         * 像素格式
         */
        @JsonProperty("PixelFormat")
        private String pixelFormat;

        /**
         * 编码级别
         */
        @JsonProperty("Level")
        private Integer level;

        /**
         * 是否为变形宽高比
         */
        @JsonProperty("IsAnamorphic")
        private boolean isAnamorphic;

        /**
         * 扩展视频类型
         */
        @JsonProperty("ExtendedVideoType")
        private String extendedVideoType;

        /**
         * 扩展视频子类型
         */
        @JsonProperty("ExtendedVideoSubType")
        private String extendedVideoSubType;

        /**
         * 扩展视频子类型描述
         */
        @JsonProperty("ExtendedVideoSubTypeDescription")
        private String extendedVideoSubTypeDescription;

        /**
         * 附件大小
         */
        @JsonProperty("AttachmentSize")
        private Integer attachmentSize;
    }
}
