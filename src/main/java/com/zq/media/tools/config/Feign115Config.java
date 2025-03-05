package com.zq.media.tools.config;

import com.zq.media.tools.properties.ConfigProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:32
 */
public class Feign115Config {

    @Bean
    public RequestInterceptor requestInterceptor(ConfigProperties configProperties) {
        return requestTemplate -> requestTemplate.header("Cookie", configProperties.getDriver115().getCookie());
    }

}
