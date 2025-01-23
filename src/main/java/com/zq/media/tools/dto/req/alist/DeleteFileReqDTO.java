package com.zq.media.tools.dto.req.alist;

import cn.hutool.core.collection.ListUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

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
public class DeleteFileReqDTO {

    /**
     * 文件名
     */
    private List<String> names;

    /**
     * 目录
     */
    private String dir;

    public DeleteFileReqDTO(String... names) {
        this.names = ListUtil.of(names);
    }

    public DeleteFileReqDTO(List<String> names) {
        this.names = names;
    }


}
