package com.zq.media.tools.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * 夸克通知文件
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-1-8 15:46
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HandleFileDTO {

    /**
     * 文件夹
     */
    private String folderPath;

    /**
     * 文件
     */
    private Set<String> files;

    /**
     * 是否是单个任务
     */
    private Boolean isSingleTask;

    public HandleFileDTO(String folderPath, Set<String> files) {
        this.folderPath = folderPath;
        this.files = files;
        this.isSingleTask = true;
    }
}
