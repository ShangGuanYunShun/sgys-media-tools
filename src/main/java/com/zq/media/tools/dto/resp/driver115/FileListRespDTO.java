package com.zq.media.tools.dto.resp.driver115;

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
     * 路径列表，用于表示完整路径
     */
    private List<PathDTO> path;

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

    /**
     * PathDTO 表示路径中的单个路径节点。
     */
    @Getter
    @Setter
    @ToString
    public static class PathDTO {


        /**
         * 目录 ID
         */
        private String cid;

        /**
         * 目录名
         */
        private String name;
    }
}
