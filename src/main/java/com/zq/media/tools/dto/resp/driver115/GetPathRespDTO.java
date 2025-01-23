package com.zq.media.tools.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 返回的文件或目录路径信息
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:07
 */
@Getter
@Setter
@ToString
public class GetPathRespDTO {

    /**
     * 文件或目录名称
     */
    @JsonProperty("file_name")
    private String fileName;


    /**
     * 路径列表，用于表示完整路径
     */
    private List<PathDTO> paths;

    /**
     * PathDTO 表示路径中的单个路径节点。
     */
    @Getter
    @Setter
    @ToString
    public static class PathDTO {


        /**
         * 路径节点的文件或目录 ID
         */
        @JsonProperty("file_id")
        private String fileId;

        /**
         * 路径节点的名称
         */
        @JsonProperty("file_name")
        private String fileName;
    }
}
