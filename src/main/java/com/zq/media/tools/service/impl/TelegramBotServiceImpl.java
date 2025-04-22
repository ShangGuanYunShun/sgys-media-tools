package com.zq.media.tools.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import com.zq.media.tools.service.ITelegramBotService;
import com.zq.media.tools.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.Serializable;

/**
 * tg
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 16:19
 */
@Slf4j
@Service
public class TelegramBotServiceImpl implements ITelegramBotService {

    @Autowired(required = false)
    private TelegramClient telegramClient;

    /**
     * 发送消息
     *
     * @param chatId  聊天id
     * @param message 消息
     */
    @Override
    public void sendMessage(Long chatId, String message) {
        sendMessage(chatId, message, null);
    }

    /**
     * 发送消息（markdown语法）
     *
     * @param chatId  聊天id
     * @param message 消息
     */
    @Override
    public void sendMarkdownMessage(Long chatId, String message) {
        sendMessage(chatId, message, "Markdown");
    }

    private void sendMessage(Long chatId, String message, String parseMode) {
        log.debug("发送tg消息：{}，{}", chatId, message);
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(message)
                .parseMode(parseMode)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送tg消息失败， {}", sendMessage, e);
        }
    }


    /**
     * 发送文件
     *
     * @param chatId 聊天id
     * @param file   文件
     */
    @Override
    public void sendFile(Long chatId, File file) {
        sendFile(chatId, file, null);
    }

    /**
     * 发送文件
     *
     * @param chatId  聊天id
     * @param file    文件
     * @param caption 标题
     */
    @Override
    public void sendFile(Long chatId, File file, String caption) {
        sendFile(chatId, file, caption, null);
    }

    /**
     * 发送文件（markdown语法）
     *
     * @param chatId  聊天id
     * @param file    文件
     * @param caption 标题
     */
    @Override
    public void sendMarkdownFile(Long chatId, File file, String caption) {
        sendFile(chatId, file, caption, "Markdown");
    }

    private void sendFile(Long chatId, File file, String caption, String parseMode) {
        String type = FileTypeUtil.getType(file);
        log.debug("发送tg文件消息：{}，文件类型：{}，文件名：{}，文件描述：{}", chatId, type, file.getName(), caption);
        try {
            if (FileUtil.isImage(type)) {
                SendPhoto sendPhoto = SendPhoto
                        .builder()
                        .chatId(chatId)
                        .photo(new InputFile(file))
                        .caption(caption)
                        .parseMode(parseMode)
                        .build();
                telegramClient.execute(sendPhoto);
            }
        } catch (TelegramApiException e) {
            log.error("发送tg消息失败：{}，{}", chatId, file.getName(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param method 消息
     */
    @Override
    public <T extends Serializable, BotMethod extends BotApiMethod<T>> void execute(BotMethod method) {
        try {
            log.debug("发送tg消息：{}", method);
            telegramClient.execute(method);
        } catch (TelegramApiException e) {
            log.error("发送tg消息失败：{}", method, e);
        }
    }
}
