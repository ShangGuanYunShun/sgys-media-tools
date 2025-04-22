package com.zq.media.tools.config;

import com.zq.media.tools.properties.ConfigProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-21 9:41
 */
public class FeignEmbyConfig {

    @Bean
    public RequestInterceptor requestInterceptor(ConfigProperties configProperties) {
        return requestTemplate -> requestTemplate.header("X-Emby-Token", configProperties.getEmby().getApiKey());
    }
}
