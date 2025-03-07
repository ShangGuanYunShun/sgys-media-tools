package com.zq.media.tools.dto.resp.alist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 获取文件信息
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-1-8 10:50
 */
@Getter
@Setter
@ToString
public class GetFileRespDTO {

    /**
     * 创建时间
     */
    private String created;

    @JsonProperty("hash_info")
    private HashInfo hashInfo;

    private String header;
    /**
     * 是否是文件夹
     */
    @JsonProperty("is_dir")
    private Boolean isDir;
    /**
     * 修改时间
     */
    private String modified;
    /**
     * 文件名
     */
    private String name;

    private String provider;
    /**
     * 原始url
     */
    private String rawurl;
    /**
     * 说明
     */
    private String readme;

    private GetFileRespDTO related;
    /**
     * 签名
     */
    private String sign;
    /**
     * 大小
     */
    private Long size;
    /**
     * 缩略图
     */
    private String thumb;
    /**
     * 类型
     */
    private Long type;

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
