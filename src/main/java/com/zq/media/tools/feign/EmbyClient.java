package com.zq.media.tools.feign;

import com.zq.media.tools.config.FeignEmbyConfig;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * emby feign
 *
 * @author zhaoqiang
 * @since V1.0.0 2025-4-21
 */
@FeignClient(name = "emby", url = "${app.emby.url}", configuration = FeignEmbyConfig.class)
public interface EmbyClient {

    /**
     * 下载图片
     *
     * @param itemId 项目id
     * @param tag    标记
     * @return {@link Response }
     */
    @GetMapping("/emby/Items/{itemId}/Images/Primary")
    Response downloadImage(@PathVariable("itemId") String itemId, @RequestParam("tag") String tag);
}
