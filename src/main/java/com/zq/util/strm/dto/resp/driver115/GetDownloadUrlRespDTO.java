package com.zq.util.strm.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-13 9:58
 */
@Getter
@Setter
@ToString
public class GetDownloadUrlRespDTO {

    /**
     * 请求状态
     */
    private boolean state;

    /**
     * 响应码
     */
    @JsonProperty("msg_code")
    private int msgCode;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 文件名
     */

    @JsonProperty("file_name")
    private String fileName;

    /**
     * 文件 URL
     */
    @JsonProperty("file_url")
    private String fileUrl;

}
