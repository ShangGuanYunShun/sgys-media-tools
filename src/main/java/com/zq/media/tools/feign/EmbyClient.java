package com.zq.media.tools.feign;

import com.zq.media.tools.config.feign.FeignEmbyConfig;
import com.zq.media.tools.dto.resp.emby.ItemRespDTO;
import com.zq.media.tools.dto.resp.emby.MediaPlaybackInfoRespDTO;
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

    /**
     * 获取项目
     *
     * @param itemId 项目id
     * @return {@link ItemRespDTO }
     */
    @GetMapping("/emby/Users/[userId]/Items/{itemId}")
    ItemRespDTO getItem(@PathVariable("itemId") String itemId);

    /**
     * 获取播放信息
     *
     * @param itemId 项目id
     * @return {@link MediaPlaybackInfoRespDTO }
     */
    @GetMapping("/emby/Items/{itemId}/PlaybackInfo")
    MediaPlaybackInfoRespDTO getPlaybackInfo(@PathVariable("itemId") String itemId);
}
