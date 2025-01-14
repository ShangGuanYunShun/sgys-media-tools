package com.zq.util.strm.feign;

import com.zq.util.strm.config.Feign115Config;
import com.zq.util.strm.dto.resp.driver115.LifeListRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用于与 115生活 API 进行通信的 Feign 客户端接口。
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 15:04
 */
@FeignClient(name = "life115Client", url = "https://life.115.com/api/1.0/web/1.0", configuration = Feign115Config.class)
public interface Life115Client {

    /**
     * 调用 life_list 接口获取事件列表。
     *
     * @param startTime 起始时间戳
     * @param limit     最大记录数
     * @param lastData  上一条数据
     * @return 包含事件列表的响应对象
     */
    @GetMapping("/life/life_list")
    LifeListRespDTO queryLifeList(@RequestParam("start_time") long startTime, @RequestParam("limit") int limit,@RequestParam("last_data") String lastData);

}
