package com.zq.media.tools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zq.media.tools.properties.TelegramBotProperties;
import com.zq.media.tools.service.ITelegramBotService;
import com.zq.media.tools.telegram.handler.CommandHandler;
import com.zq.media.tools.telegram.handler.command.GetImageCommand;
import lombok.RequiredArgsConstructor;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-15 15:04
 */
@Configuration
@ConditionalOnProperty(prefix = "telegrambots", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotConfig {

    @Bean
    public OkHttpClient okClientHttp(TelegramBotProperties telegramBotProperties) {
        return new CustomHttpProxyOkHttpClientCreator(
                () -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(telegramBotProperties.getProxy().getHostname(), telegramBotProperties.getProxy().getPort())),
                () -> (route, response) -> {
                    Request.Builder builder = response
                            .request()
                            .newBuilder();
                    if (StrUtil.isNotBlank(telegramBotProperties.getProxy().getUsername()) || StrUtil.isNotBlank(telegramBotProperties.getProxy().getPassword())) {
                        String credential = Credentials.basic(telegramBotProperties.getProxy().getUsername(), telegramBotProperties.getProxy().getPassword());
                        builder.header("Proxy-Authorization", credential);
                    }
                    return builder.build();
                },
                new HttpLoggingInterceptor()
        ).get();
    }

//    @Bean
//    public OkHttpClient okClientSocks(TelegramBotProperties telegramBotProperties) {
//        Authenticator.setDefault(new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                if (getRequestingHost().equalsIgnoreCase(telegramBotProperties.getProxy().getHostname()) &&
//                        telegramBotProperties.getProxy().getPort() == getRequestingPort()) {
//                    return new PasswordAuthentication(telegramBotProperties.getProxy().getUsername(), telegramBotProperties.getProxy().getPassword().toCharArray());
//                }
//
//                return null;
//            }
//        });
//
//       return new TelegramOkHttpClientFactory.SocksProxyOkHttpClientCreator(
//                () -> new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(telegramBotProperties.getProxy().getHostname(), telegramBotProperties.getProxy().getPort()))
//        ).get();
//    }

    @Bean
    public TelegramClient telegramClient(@Qualifier("okClientHttp") OkHttpClient okClient, TelegramBotProperties telegramBotProperties) {
        return new OkHttpTelegramClient(okClient, telegramBotProperties.getToken());
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(@Qualifier("okClientHttp") OkHttpClient okClient, ObjectMapper objectMapper) {
        return new TelegramBotsLongPollingApplication(() -> objectMapper, () -> okClient);
    }

//    @Bean
//    public SgysTelegramBot sgysTelegramBot(ITelegramBotService telegramService, TelegramBotProperties telegramBotProperties) {
//        return new SgysTelegramBot(telegramService, telegramBotProperties);
//    }

    // region 机器人命令
    @Bean
    public HelpCommand helpCommand() {
        return new HelpCommand();
    }

    @Bean
    public GetImageCommand getImageCommand(ITelegramBotService telegramService) {
        return new GetImageCommand(telegramService);
    }
    // endregion

    @Bean
    public CommandHandler commandHandler(TelegramClient telegramClient, ITelegramBotService telegramService,
                                         TelegramBotProperties telegramBotProperties, List<BotCommand> commands) {
        return new CommandHandler(telegramClient, telegramService, telegramBotProperties, commands);
    }

    @RequiredArgsConstructor
    public static class CustomHttpProxyOkHttpClientCreator extends TelegramOkHttpClientFactory.DefaultOkHttpClientCreator {
        private final Supplier<Proxy> proxySupplier;
        private final Supplier<okhttp3.Authenticator> authenticatorSupplier;
        private final HttpLoggingInterceptor loggingInterceptor;

        @Override
        public OkHttpClient get() {
            OkHttpClient.Builder okHttpClientBuilder = getBaseClient();
            okHttpClientBuilder.addInterceptor(loggingInterceptor);

            // Proxy
            ofNullable(proxySupplier.get()).ifPresent(okHttpClientBuilder::proxy);
            ofNullable(authenticatorSupplier.get()).ifPresent(okHttpClientBuilder::proxyAuthenticator);

            return okHttpClientBuilder.build();
        }
    }

}
