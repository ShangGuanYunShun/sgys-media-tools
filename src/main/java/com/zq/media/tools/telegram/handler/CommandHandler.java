package com.zq.media.tools.telegram.handler;

import com.zq.common.util.CollectionUtil;
import com.zq.media.tools.properties.TelegramBotProperties;
import com.zq.media.tools.service.ITelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * 命令处理器
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-27 11:53
 */
@Slf4j
public class CommandHandler extends CommandLongPollingTelegramBot implements SpringLongPollingBot {

    private final ITelegramBotService telegramService;
    private final TelegramBotProperties telegramBotProperties;

    public CommandHandler(TelegramClient telegramClient, ITelegramBotService telegramBotService, TelegramBotProperties telegramBotProperties,
                          List<BotCommand> commands) {
        super(telegramClient, false, () -> "");
        this.telegramService = telegramBotService;
        this.telegramBotProperties = telegramBotProperties;
        commands.forEach(this::register);
        // 设置命令
        setCommands(commands);
        registerDefaultAction((tgClient, message) -> {
            telegramBotService.sendMessage(message.getChatId(), "Unknown command. Here comes some help");
            telegramBotService.sendMessage(message.getChatId(), HelpCommand.getHelpText(getRegisteredCommands()));
        });
    }

    private void setCommands(List<BotCommand> commands) {
        List<org.telegram.telegrambots.meta.api.objects.commands.BotCommand> botCommands = CollectionUtil.convertList(commands,
                botCommand -> new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(botCommand.getCommandIdentifier(), botCommand.getDescription()));
        SetMyCommands setMyCommands = new SetMyCommands(botCommands);
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error("设置命令失败", e);
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                telegramService.sendMessage(message.getChatId(), "You said: " + message.getText());
            }
        }
    }

    @Override
    public String getBotToken() {
        return telegramBotProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}
