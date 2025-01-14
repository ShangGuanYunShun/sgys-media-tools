package com.zq.util.strm.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.zq.util.strm.properties.ConfigProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//        String encodedUrl = URLUtil.encode(videoRelativeUrl, StandardCharsets.UTF_8);
        FileUtil.writeUtf8String(videoRelativeUrl, strmPath.toString());
        return Paths.get(filePath.getParent().toString(), FileUtil.mainName(filePath.getFileName().toString()) + ".strm").toString();
    }

    /**
     * 重写生成 STRM 文件内容
     *
     * @param strmFilePath strm文件路径
     * @return {@link String }
     */
    public static void writeStrmFiles(Path strmFilePath, String path) {
        String videoRelativeUrl = configProperties.getAlist().getMediaUrl() + path.replace("\\", "/").replace("//", "/");
        String encodedUrl = URLUtil.encode(videoRelativeUrl, StandardCharsets.UTF_8);
        FileUtil.writeUtf8String(encodedUrl, strmFilePath.toString());
    }

    /**
     * 是视频文件
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isVideoFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return isVideoFile(fileName);
    }

    /**
     * 是视频文件
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static boolean isVideoFile(String fileName) {
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mkv") || fileName.endsWith(".mov") ||
                fileName.endsWith(".wmv") || fileName.endsWith(".flv") ||
                fileName.endsWith(".rmvb") || fileName.endsWith(".iso") ||
                fileName.endsWith(".ts") || fileName.endsWith(".bdmv");
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
                .header("User-Agent", configProperties.getClient115().getUserAgent())
                .timeout(10000);
        HttpResponse response = request.execute();

        // 检查响应状态是否成功
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            FileUtil.writeBytes(response.bodyBytes(), savePath.toString());
        } else {
            log.warn("远程下载文件失败：\n{} \n{}", request, response);
        }
    }

    /**
     * 判断两个文件名是否包含相同的季数和集数（格式 SxxExx）。
     *
     * @param fileName1 第一个文件名
     * @param fileName2 第二个文件名
     * @return 如果季数和集数一致，返回 true；否则返回 false
     */
    public static boolean areEpisodesEqual(String fileName1, String fileName2) {
        // 正则表达式匹配 SxxExx 格式
        String regex = "S(\\d{2})E(\\d+)";
        Pattern pattern = Pattern.compile(regex);

        // 提取第一个文件名中的季数和集数
        Matcher matcher1 = pattern.matcher(fileName1);
        Matcher matcher2 = pattern.matcher(fileName2);

        if (matcher1.find() && matcher2.find()) {
            // 获取季数和集数
            String season1 = matcher1.group(1);
            String episode1 = matcher1.group(2);
            String season2 = matcher2.group(1);
            String episode2 = matcher2.group(2);

            // 比较季数和集数是否相等
            return season1.equals(season2) && episode1.equals(episode2);
        }

        // 如果任一文件名不包含 SxxExx 格式，则视为不匹配
        return false;
    }

    /**
     * 判断文件名是否是剧集（格式 SxxExx）。
     *
     * @param fileName 文件名
     * @return 如果是剧集，返回 true；否则返回 false
     * }
     */
    public static boolean isEpisodes(String fileName) {
        // 正则表达式匹配 SxxExx 格式
        String regex = "S(\\d{2})E(\\d+)";
        Pattern pattern = Pattern.compile(regex);

        // 提取文件名中的季数和集数
        Matcher matcher = pattern.matcher(fileName);

        return matcher.find();
    }

    /**
     * 获取剧集
     *
     * @param fileName 文件名
     * @return {@link String }
     */
    public static String getEpisodes(String fileName) {
        String regex = "S(\\d{2})E(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String season = matcher.group(1);
            String episode = matcher.group(2);
            return "S" + season + "E" + episode;
        }
        return null;
    }

    @Autowired
    public void setConfigProperties(ConfigProperties configProperties) {
        StrmUtil.configProperties = configProperties;
    }

}
