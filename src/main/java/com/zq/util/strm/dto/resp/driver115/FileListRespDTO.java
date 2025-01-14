package com.zq.util.strm.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 接口返回的文件列表
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:08
 */
@Getter
@Setter
@ToString
public class FileListRespDTO {

    /**
     * 文件数据列表
     */
    private List<FileDataDTO> data;

    /**
     * FileDataDTO 表示单个文件的数据。
     */
    @Getter
    @Setter
    @ToString
    public static class FileDataDTO {

        /**
         * 文件 ID
         */
        @JsonProperty("fid")
        private String fileId;

        /**
         * 目录 ID
         */
        @JsonProperty("cid")
        private String catalogId;

        /**
         * 文件名称
         */
        @JsonProperty("n")
        private String fileName;

        /**
         * 选取代码
         */
        @JsonProperty("pc")
        private String pickCode;

        /**
         * sha1
         */
        @JsonProperty("sha")
        private String sha1;

        /**
         * 文件扩展名
         */
        @JsonProperty("ico")
        private String ext;
    }
}
