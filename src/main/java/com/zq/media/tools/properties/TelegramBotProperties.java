package com.zq.media.tools.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * tg机器人配置
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 15:08
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "telegrambots")
public class TelegramBotProperties {

    private boolean enabled;

    /**
     * 机器人token
     */
    private String token;

    /**
     * 机器人名称
     */
    private String botName;

    /**
     * 聊天id
     */
    private Long chatId;

    /**
     * 代理
     */
    private Proxy proxy;

    @Getter
    @Setter
    @ToString
    public static class Proxy {

        private String hostname;

        private int port;

        private String username;

        private String password;
    }
}
