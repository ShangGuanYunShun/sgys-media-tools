package com.zq.util.strm.task;

import com.zq.util.strm.driver.Driver115;
import com.zq.util.strm.properties.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 115生活监听
 * // TODO 删除文件时未删除文件夹
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:19
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.client115", name = "enabled", havingValue = "true")
public class Client115LifeMonitor {

    private final Driver115 driver115;
    private final ConfigProperties configProperties;

    /**
     * 监控115生活事件
     * 定时任务，监控115网盘的文件变化并同步到本地
     */
    @Scheduled(cron = "0 */${app.client115.intervalMinutes} * * * ?")
    public void monitorLifeEvents() {
        try {
            driver115.handleBehavior(LocalDateTime.now().minusMinutes(configProperties.getClient115().getIntervalMinutes()), LocalDateTime.now());
        } catch (Exception e) {
            log.error("监控生活事件时发生错误: ", e);
        }
    }
}
