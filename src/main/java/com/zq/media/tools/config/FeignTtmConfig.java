package com.zq.media.tools.config;

import com.zq.media.tools.properties.ConfigProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 17:13
 */
public class FeignTtmConfig {

    @Bean
    public RequestInterceptor requestInterceptor(ConfigProperties configProperties) {
        return requestTemplate -> requestTemplate.header("api-key", configProperties.getTtm().getApiKey());
    }
}
