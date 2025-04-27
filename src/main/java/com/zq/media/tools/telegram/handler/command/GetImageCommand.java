package com.zq.media.tools.telegram.handler.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zq.common.util.JsonUtil;
import com.zq.media.tools.service.ITelegramBotService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取图片命令，通过必应搜索
 * /getImage
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-27 13:48
 */
@Slf4j
public class GetImageCommand extends BotCommand {

    private final ITelegramBotService telegramBotService;

    public GetImageCommand(ITelegramBotService telegramBotService) {
        super("getImage", "根据关键字随机获取一张图片，不传入关键字随机获取一张美女图片");
        this.telegramBotService = telegramBotService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] arguments) {
        List<String> imageUrls;
        if (arguments != null && arguments.length > 0) {
            imageUrls = fetchImageUrls(arguments[0]);
        } else {
            imageUrls = fetchImageUrls("美女");
        }
        String imageUrl = imageUrls.get(RandomUtil.randomInt(imageUrls.size()));
        File file = FileUtil.createTempFile(".jpg", true);
        HttpUtil.downloadFile(imageUrl, file);
        telegramBotService.sendFile(chat.getId(), file);
    }

    @SneakyThrows
    private List<String> fetchImageUrls(String keyword) {
        List<String> imageUrls = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String url = "https://www.bing.com/images/async?q=" + encodedKeyword +
                "&first=0&count=20&adlt=off&lostate=r";

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .get();

        Elements links = doc.select("a.iusc");
        for (Element link : links) {
            String attr = link.attr("m");
            if (!attr.isEmpty()) {
                try {
                    Map<String, Object> json = JsonUtil.parseObject(attr, new TypeReference<Map<String, Object>>() {
                    });
                    String imageUrl = (String) json.get("murl");
                    if (imageUrl != null && imageUrl.startsWith("http")) {
                        imageUrls.add(imageUrl);
                    }
                } catch (Exception ignored) {
                    // skip malformed json
                }
            }
        }

        return imageUrls;
    }
}
