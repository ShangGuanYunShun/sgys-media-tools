package com.zq.media.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * strm工具服务
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-11 14:04
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.zq")
public class MediaToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaToolsApplication.class, args);
    }
}
