package com.zq.media.tools.config;

import cn.hutool.core.collection.ListUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-1-2 15:19
 */
@Configuration
@ConditionalOnProperty(prefix = "app.client115", name = "enabled", havingValue = "true")
public class StrmWebMvcConfig {

    @Bean
    public CustomMappingJackson2HttpMessageConverter customMappingJackson2HttpMessageConverter() {
        return new CustomMappingJackson2HttpMessageConverter();
    }

    public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

        public CustomMappingJackson2HttpMessageConverter() {
            // 设置支持的内容类型
            this.setSupportedMediaTypes(ListUtil.of(
                    MediaType.APPLICATION_JSON,
                    MediaType.TEXT_HTML // 添加对 text/html 的支持
            ));
        }
    }
}
