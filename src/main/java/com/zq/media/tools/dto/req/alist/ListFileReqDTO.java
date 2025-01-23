package com.zq.media.tools.dto.req.alist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 16:29
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ListFileReqDTO {

    /**
     * 页数
     */
    private Integer page = 1;
    /**
     * 密码
     */
    private String password = "";
    /**
     * 路径
     */
    private String path;
    /**
     * 每页数目
     */
    private Integer perPage = 0;
    /**
     * 是否强制刷新
     */
    private Boolean refresh = false;

    public ListFileReqDTO(String path, Boolean refresh) {
        this.path = path;
        this.refresh = refresh;
    }
}
