package com.zq.util.strm.config;

import com.zq.util.strm.properties.ConfigProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * alist feign配置
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 17:05
 */
public class FeignAlistConfig {

    @Bean
    public RequestInterceptor requestInterceptor(ConfigProperties configProperties) {
        return requestTemplate -> requestTemplate.header("Authorization", configProperties.getAlist().getToken());
    }
}
