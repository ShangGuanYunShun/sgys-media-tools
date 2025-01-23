package com.zq.media.tools.dto.req.alist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 移动文件req
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-25 17:52
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class MoveFileReqDTO {

    /**
     * 源文件夹
     */
    @JsonProperty("src_dir")
    private String srcDir;

    /**
     * 目标文件夹
     */
    @JsonProperty("dst_dir")
    private String dstDir;

    /**
     * 文件名
     */
    private Set<String> names;
}
