package com.zq.media.tools.util;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 媒体工具类
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-4-25 9:19
 */
public class MediaUtil {

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
        String regex = "[Ss](\\d{2})[Ee](\\d+)";
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
        String regex = "[Ss](\\d{2})[Ee](\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String season = matcher.group(1);
            String episode = matcher.group(2);
            return "S" + season + "E" + episode;
        }
        return null;
    }

    /**
     * 获取集数
     *
     * @param fileName 文件名
     * @return {@link String }
     */
    public static int getEpisode(String fileName) {
        String regex = "[Ss](\\d{2})[Ee](\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }
        return -1;
    }

    /**
     * 获取季数
     *
     * @param fileName 文件名
     * @return {@link String }
     */
    public static String getSeason(String fileName) {
        String regex = "[Ss](\\d{2})[Ee](\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 格式化视频大小
     *
     * @param size 视频大小（比特）
     * @return 格式化后的视频大小字符串
     */
    public static String formatSize(double size) {
        final double gb = 1024 * 1024 * 1024;
        final double mb = 1024 * 1024;

        if (size >= gb) {
            return String.format("%.2f GB", size / gb);
        } else {
            return String.format("%.2f MB", size / mb);
        }
    }

    /**
     * 格式化视频码率
     *
     * @param bitrate 视频码率（比特/秒）
     * @return 格式化后的视频码率字符串
     */
    public static String formatBitrate(int bitrate) {
        final double MBPS = 1024 * 1024;
        double mbps = bitrate / MBPS;

        // 检查是否为整数
        if (mbps == Math.floor(mbps)) {
            return String.format("%.0f", mbps);
        } else {
            return String.format("%.1f", mbps);
        }
    }

    /**
     * 检查是否匹配到季数
     *
     * @param input 输入字符串，例如 "第 1 季"，"第1季"
     * @return 是否成功匹配到季数
     */
    public static boolean isSeasonNumberMatched(String input) {
        String regex = "第\\s*.*?\\s*季";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.find();
    }

    /**
     * 检查是否匹配到集数
     *
     * @param input 输入字符串，例如 "第 1 集"，"第一集"
     * @return 是否成功匹配到集数
     */
    public static boolean isEpisodeNumberMatched(String input) {
        String regex = "第\\s*.*?\\s*集";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.find();
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

}
