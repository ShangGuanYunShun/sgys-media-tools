package com.zq.media.tools.dto.req.alist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 删除文件req
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 17:33
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class DeleteFileReqDTO {

    /**
     * 目录
     */
    private String dir;

    /**
     * 文件名
     */
    private Set<String> names;

    public DeleteFileReqDTO(String dir, String... names) {
        this.dir = dir;
        this.names = Set.of(names);
    }

}
