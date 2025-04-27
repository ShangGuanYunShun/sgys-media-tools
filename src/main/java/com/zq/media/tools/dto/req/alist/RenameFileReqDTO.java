package com.zq.media.tools.dto.req.alist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-3-24 10:47
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RenameFileReqDTO {

    /**
     * 目录
     */
    @JsonProperty("src_dir")
    private String srcDir;

    /**
     * 重命名文件列表
     */
    @JsonProperty("rename_objects")
    private List<RenameFile> renameFileList;

    public RenameFileReqDTO(String srcDir, String srcName, String newName) {
        this.srcDir = srcDir;
        this.renameFileList = List.of(new RenameFile(srcName, newName));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RenameFile {

        /**
         * 源名称
         */
        @JsonProperty("src_name")
        private String srcName;

        /**
         * 新名称
         */
        @JsonProperty("new_name")
        private String newName;

        @Override
        public String toString() {
            return srcName + "->" + newName;
        }
    }
}
