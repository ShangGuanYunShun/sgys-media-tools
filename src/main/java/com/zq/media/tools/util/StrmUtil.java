package com.zq.media.tools.util;

import com.zq.media.tools.properties.ConfigProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.io.file.FileNameUtil;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.net.url.UrlEncoder;
import org.dromara.hutool.http.client.HttpDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 13:45
 */
@Slf4j
@Component
public class StrmUtil {

    private static ConfigProperties configProperties;

    /**
     * 生成 STRM 文件
     *
     * @param filePath 文件路径
     * @return {@link String }
     */
    public static String generateStrmFiles(Path filePath) {
        Path strmPath = Paths.get(configProperties.getServer().getBasePath(), filePath.getParent().toString(), FileNameUtil.mainName(filePath.getFileName().toString()) + ".strm");
        String videoRelativeUrl = configProperties.getAlist().getMediaUrl() + filePath.toString().replace("\\", "/").replace("//", "/");
        FileUtil.writeUtf8String(configProperties.getEncodeStrmPath() ? UrlEncoder.encodeQuery(videoRelativeUrl, StandardCharsets.UTF_8) : videoRelativeUrl, strmPath.toString());
        return strmPath.toString();
    }

    /**
     * 重写生成 STRM 文件内容
     *
     * @param strmFilePath strm文件路径
     * @return {@link String }
     */
    public static void writeStrmFiles(Path strmFilePath, String path) {
        String videoRelativeUrl = configProperties.getAlist().getMediaUrl() + path.replace("\\", "/").replace("//", "/");
        FileUtil.writeUtf8String(configProperties.getEncodeStrmPath() ? UrlEncoder.encodeQuery(videoRelativeUrl, StandardCharsets.UTF_8) : videoRelativeUrl, strmFilePath.toString());
    }

    /**
     * 从指定 URL 下载文件，并通过 Cookie 进行身份验证
     *
     * @param url      网络地址
     * @param cookie   需要传入的 Cookie 信息
     * @param savePath 保存路径
     */
    @SneakyThrows
    public static void downloadFile(String url, String cookie, Path savePath) {
        HttpDownloader.of(url)
                .header("Cookie", cookie)
                .header("User-Agent", configProperties.getDriver115().getUserAgent())
                .setTimeout(10000)
                .downloadFile(savePath.toFile());
    }

    @Autowired
    public void setConfigProperties(ConfigProperties configProperties) {
        StrmUtil.configProperties = configProperties;
    }

}
