package com.zq.media.tools.init;

import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.service.impl.AlistServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * alist文件strm初始化
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-2-20 10:10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlistStrmInit implements ApplicationListener<ApplicationStartedEvent> {

    private final ConfigProperties configProperties;
    private final AlistServiceImpl alistService;

    @Override
    @Async
    public void onApplicationEvent(ApplicationStartedEvent event) {
        List<String> mediaPath = configProperties.getAlist().getMediaPath();
        for (String path : mediaPath) {
            alistService.processDic(path);
        }
    }
}
