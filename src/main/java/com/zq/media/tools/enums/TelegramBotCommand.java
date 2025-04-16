package com.zq.media.tools.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * tg机器人命令
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 16:33
 */
@Getter
@AllArgsConstructor
public enum TelegramBotCommand {

    START("start"),
    STOP("stop"),
    PIC("pic"),
    UNKNOWN("unknown");

    private final String command;

    public static TelegramBotCommand of(String command) {
        for (TelegramBotCommand value : values()) {
            if (("/" + value.getCommand()).equals(command)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
