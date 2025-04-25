package com.zq.media.tools.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.zq.media.tools.properties.ConfigProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
        Path strmPath = Paths.get(configProperties.getServer().getBasePath(), filePath.getParent().toString(), FileUtil.mainName(filePath.getFileName().toString()) + ".strm");
        String videoRelativeUrl = configProperties.getAlist().getMediaUrl() + filePath.toString().replace("\\", "/").replace("//", "/");
        FileUtil.writeUtf8String(configProperties.getEncodeStrmPath() ? URLUtil.encode(videoRelativeUrl, StandardCharsets.UTF_8) : videoRelativeUrl, strmPath.toString());
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
        FileUtil.writeUtf8String(configProperties.getEncodeStrmPath() ? URLUtil.encode(videoRelativeUrl, StandardCharsets.UTF_8) : videoRelativeUrl, strmFilePath.toString());
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
        HttpRequest request = HttpRequest.get(url)
                .header("Cookie", cookie)
                .header("User-Agent", configProperties.getDriver115().getUserAgent())
                .timeout(10000);
        HttpResponse response = request.execute();

        // 检查响应状态是否成功
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            FileUtil.writeBytes(response.bodyBytes(), savePath.toString());
        } else {
            log.warn("远程下载文件失败：\n{} \n{}", request, response);
        }
    }

    @Autowired
    public void setConfigProperties(ConfigProperties configProperties) {
        StrmUtil.configProperties = configProperties;
    }

}
