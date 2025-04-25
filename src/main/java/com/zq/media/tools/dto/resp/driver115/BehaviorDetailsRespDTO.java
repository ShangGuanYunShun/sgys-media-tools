package com.zq.media.tools.dto.resp.driver115;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-3-31 10:24
 */
@Getter
@Setter
@ToString
public class BehaviorDetailsRespDTO {

    /**
     * 请求状态
     */
    private Boolean state;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    private BehaviorDetailsDataDTO data;

    @Getter
    @Setter
    @ToString
    public static class BehaviorDetailsDataDTO {

        /**
         * 计数
         */
        private String count;

        /**
         * 是否存在下一页
         */
        private Boolean nextPage;

        private List<BehaviorDetailRespDTO> list;
    }
}
