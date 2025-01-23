package com.zq.media.tools.dto.resp.alist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 16:31
 */
@Getter
@Setter
@ToString
public class listFileRespDTO {

    /**
     * 内容
     */
    private List<Content> content;
    private String header;
    private String provider;
    /**
     * 说明
     */
    private String readme;
    /**
     * 总数
     */
    private long total;
    /**
     * 是否可写入
     */
    private boolean write;

    @Getter
    @Setter
    @ToString
    public static class Content {
        /**
         * 创建时间
         */
        private String created;

        /**
         * 哈希信息
         */
        private HashInfo hashInfo;
        /**
         * 是否是文件夹
         */
        private boolean isDir;
        /**
         * 修改时间
         */
        private String modified;
        /**
         * 文件名
         */
        private String name;
        /**
         * 签名
         */
        private String sign;
        /**
         * 大小
         */
        private long size;
        /**
         * 缩略图
         */
        private String thumb;
        /**
         * 类型
         */
        private long type;

        @Getter
        @Setter
        @ToString
        public static class HashInfo {

            /**
             * SHA1
             */
            private String sha1;
        }
    }
}
