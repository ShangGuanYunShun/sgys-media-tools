package com.zq.media.tools.feign;

import com.zq.media.tools.config.Feign115Config;
import com.zq.media.tools.dto.resp.driver115.FileListRespDTO;
import com.zq.media.tools.dto.resp.driver115.GetDownloadUrlRespDTO;
import com.zq.media.tools.dto.resp.driver115.GetPathRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用于与 115 API 进行通信的 Feign 客户端接口。
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:07
 */
@FeignClient(name = "driver115Client", url = "https://webapi.115.com", configuration = Feign115Config.class)
public interface Driver115Client {

    /**
     * 调用 get 接口获取文件或目录的完整路径信息。
     *
     * @param cid 文件或目录的 ID
     * @return 包含路径信息的响应对象
     */
    @GetMapping("/category/get")
    GetPathRespDTO getFilePath(@RequestParam("cid") String cid);

    /**
     * 调用 files 接口列出指定目录下的文件。
     *
     * @param cid   目录 ID
     * @param limit 最大返回文件数量
     * @return 包含文件列表的响应对象
     */
    @GetMapping("/files")
    FileListRespDTO listFiles(@RequestParam("cid") String cid, @RequestParam("limit") int limit);

    /**
     * 获取下载 URL
     *
     * @param pickCode 选取代码
     * @return {@link GetDownloadUrlRespDTO }
     */
    @GetMapping("/files/download")
    GetDownloadUrlRespDTO getDownloadUrl(@RequestParam("pickcode") String pickCode);
}
