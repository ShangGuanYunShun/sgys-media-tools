package com.zq.util.strm.dto;

import com.zq.util.strm.enums.BehaviorType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 待处理文件
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 13:50
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PendingProcessFileDTO {

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 旧文件名
     */
    private String oldFileName;

    /**
     * 选取代码
     */
    private String pickCode;

    /**
     * SHA1
     */
    private String sha1;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 父文件id
     */
    private String parentId;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * 行为类型
     */
    private BehaviorType behaviorType;

    /**
     * 是否是目录
     */
    private Boolean isDic;
}
