package com.zq.media.tools.dto.req.alist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 复制文件请求体
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 16:14
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CopyFileReqDTO extends MoveFileReqDTO {
}
