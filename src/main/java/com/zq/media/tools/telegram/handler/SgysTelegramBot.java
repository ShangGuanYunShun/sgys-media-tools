package com.zq.media.tools.telegram.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import com.zq.media.tools.enums.TelegramBotCommand;
import com.zq.media.tools.properties.TelegramBotProperties;
import com.zq.media.tools.service.ITelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

/**
 * 默认处理器
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 14:20
 */
@Slf4j
@RequiredArgsConstructor
public class SgysTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final ITelegramBotService telegramService;
    private final TelegramBotProperties telegramBotProperties;

    @Override
    public String getBotToken() {
        return telegramBotProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            TelegramBotCommand botCommand = TelegramBotCommand.of(messageText);
            log.info("Received command: {} from chatId: {}", botCommand, chatId);
            switch (botCommand) {
                case START -> telegramService.sendMessage(chatId, "启动成功");
                case STOP -> telegramService.sendMessage(chatId, "停止成功");
                case PIC -> {
                    Resource resource = ResourceUtil.getResourceObj("static/pic.jpg");
                    File file = FileUtil.writeFromStream(resource.getStream(), FileUtil.createTempFile(".jpg", true));
                    telegramService.sendFile(chatId, file);
                }
                default -> telegramService.sendMessage(chatId, "未知命令");
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        try {
            log.info("Registered bot running state is: {}", botSession.isRunning());
            if (!botSession.isRunning()) {
                log.error("Bot session is not running. Please check the configuration and try again.");
            }
        } catch (Exception e) {
            log.error("Error during bot registration: ", e);
        }
    }

}