package com.zq.media.tools.dto.resp.alist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务返回
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 16:52
 */
@Getter
@Setter
@ToString
public class TaskRespDTO {

    /**
     * 错误信息
     */
    private String error;
    /**
     * id
     */
    private String id;
    /**
     * 任务名
     */
    private String name;
    /**
     * 进度
     */
    private Long progress;
    /**
     * 任务完成状态
     */
    private String state;

    private String status;
}
