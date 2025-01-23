package com.zq.media.tools.dto.req.alist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 获取文件信息
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-1-8 10:45
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GetFileReqDTO extends ListFileReqDTO {

    public GetFileReqDTO(String path, Boolean refresh) {
        super(path, refresh);
    }
}
