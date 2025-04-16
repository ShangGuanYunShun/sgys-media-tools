package com.zq.media.tools.service;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;

import java.io.File;
import java.io.Serializable;

/**
 * tg
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 16:17
 */
public interface ITelegramBotService {

    /**
     * 发送消息
     *
     * @param chatId  聊天id
     * @param message 消息
     */
    void sendMessage(Long chatId, String message);

    /**
     * 发送文件
     *
     * @param chatId 聊天id
     * @param file   文件
     */
    void sendFile(Long chatId, File file);

    /**
     * 发送文件
     *
     * @param chatId  聊天id
     * @param file    文件
     * @param caption 标题
     */
    void sendFile(Long chatId, File file, String caption);

    /**
     * 发送消息
     *
     * @param method 消息
     */
    <T extends Serializable, BotMethod extends BotApiMethod<T>> void execute(BotMethod method);
}
