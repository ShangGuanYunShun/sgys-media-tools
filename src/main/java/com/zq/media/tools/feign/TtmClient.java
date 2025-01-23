package com.zq.media.tools.feign;

import com.zq.media.tools.config.FeignTtmConfig;
import com.zq.media.tools.dto.req.ttm.TtmReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * tinyMediaManager api
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 16:56
 */
@FeignClient(name = "ttm", url = "${app.ttm.url}", configuration = FeignTtmConfig.class)
public interface TtmClient {

    /**
     * 执行ttm命令
     *
     * @param ttmReqDTO ttm req dto
     */
    @PostMapping
    void execute(@RequestBody List<TtmReqDTO> ttmReqDTO);

    /**
     * 更新媒体库
     *
     * @param ttmReqDTO ttm req dto
     */
    @PostMapping
    void update(@RequestBody TtmReqDTO ttmReqDTO);

    /**
     * 刮削
     *
     * @param ttmReqDTO ttm req dto
     */
    @PostMapping
    void scrape(@RequestBody TtmReqDTO ttmReqDTO);
}
