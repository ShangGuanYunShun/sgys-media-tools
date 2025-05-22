package com.zq.media.tools.service.impl;

import com.zq.common.domain.Result;
import com.zq.common.util.ThreadUtil;
import com.zq.media.tools.dto.HandleFileDTO;
import com.zq.media.tools.dto.req.alist.DeleteFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import com.zq.media.tools.dto.resp.emby.ItemRespDTO;
import com.zq.media.tools.dto.resp.emby.MediaPlaybackInfoRespDTO;
import com.zq.media.tools.enums.EmbyMediaType;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.feign.EmbyClient;
import com.zq.media.tools.params.EmbyNotifyParam;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.properties.TelegramBotProperties;
import com.zq.media.tools.service.IAlistService;
import com.zq.media.tools.service.IReceiveNotificationService;
import com.zq.media.tools.service.ITelegramBotService;
import com.zq.media.tools.util.MediaUtil;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.net.url.UrlDecoder;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.text.split.SplitUtil;
import org.dromara.hutool.http.meta.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zq.common.util.CollectionUtil.anyMatch;
import static com.zq.common.util.CollectionUtil.convertList;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/8 22:21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveNotificationServiceImpl implements IReceiveNotificationService {

    private final IAlistService alistService;
    private final ConfigProperties configProperties;
    private final AlistClient alistClient;
    private final EmbyClient embyClient;
    private final ITelegramBotService telegramService;
    private final TelegramBotProperties telegramBotProperties;

    /**
     * 接收夸克自动保存
     *
     * @param content 内容
     */
    @Override
    public void receiveQuarkAutoSave(String content) {
        ThreadUtil.execute(() -> {
            List<String> split = SplitUtil.split(content, "\\n");
            List<HandleFileDTO> list = new ArrayList<>();

            // 初始化文件 DTO
            HandleFileDTO handleFileDTO = createHandleFileDTO();
            boolean isFolderPathSet = false;

            for (String str : split) {
                str = str.trim(); // 去除前后空格
                log.debug(str);
                if (configProperties.getDriverQuark().getHandleFolders().stream().anyMatch(str::contains)) {
                    // 设置文件夹路径
                    handleFileDTO.setFolderPath("/夸克网盘" + str);
                    isFolderPathSet = true;
                } else if (str.isEmpty() || "\"".equals(str)) {
                    // 处理结束，保存当前 DTO
                    if (!handleFileDTO.getFiles().isEmpty() && handleFileDTO.getFolderPath() != null) {
                        // 判断当前文件夹是否已经在list中存在
                        boolean isExist = false;
                        for (HandleFileDTO fileDTO : list) {
                            if (fileDTO.getFolderPath().equals(handleFileDTO.getFolderPath())) {
                                fileDTO.setIsSingleTask(false);
                                isExist = true;
                            }
                        }
                        if (!isExist) {
                            list.add(handleFileDTO);
                        }
                    }
                    handleFileDTO = createHandleFileDTO(); // 重置 DTO
                    isFolderPathSet = false;
                } else if (isFolderPathSet && MediaUtil.isEpisodes(str)) {
                    // 处理文件路径
                    String cleanedPath = str.substring(str.indexOf("──") + 2) // 去掉前缀符号
                            .replaceAll("[\\p{Cf}\\uFEFF🎞️]", "") // 清理特殊字符
                            .trim();
                    handleFileDTO.getFiles().add(cleanedPath);
                }
            }

            // 添加最后一个 DTO
            if (!handleFileDTO.getFiles().isEmpty() || handleFileDTO.getFolderPath() != null) {
                list.add(handleFileDTO);
            }

            // 调用服务方法处理
            list.forEach(alistService::handleCloudAutoSave);
        });
    }

    /**
     * 接收天翼云盘自动保存
     *
     * @param content 内容
     */
    @Override
    public void receiveCloud189AutoSave(String content) {
        String seriesName = content.split("\n")[0].split("/")[0];
        Set<String> episodes = getEpisodesByCloud189Content(content);
        for (String handleFolder : configProperties.getDriverCloud189().getHandleFolders()) {
            String folderPath = "/天翼云盘" + handleFolder;
            Set<String> fileNames = alistService.queryListFileByDic(folderPath);
            if (anyMatch(fileNames, seriesName::equals)) {
                alistService.handleCloudAutoSave(new HandleFileDTO(folderPath + "/" + seriesName, episodes));
            }
        }
    }

    /**
     * 获取转存剧集通过天翼与云盘自动转存内容
     *
     * @param content 内容
     * @return {@link Set }<{@link String }>
     */
    private Set<String> getEpisodesByCloud189Content(String content) {
        Pattern pattern = Pattern.compile("<font[^>]*>(.*?)</font>");
        Matcher matcher = pattern.matcher(content);

        Set<String> episodes = new HashSet<>();
        while (matcher.find()) {
            episodes.add(matcher.group(1).trim());
        }
        return episodes;
    }

    /**
     * 接收 Emby 的神医通知
     *
     * @param embyNotifyParam emby 通知参数
     */
    @Override
    public void receiveEmbyFromShenYi(EmbyNotifyParam embyNotifyParam) {
        switch (embyNotifyParam.getEvent()) {
            case DEEP_DELETE -> deepDelete(embyNotifyParam);
            case INTRO_SKIP_UPDATE -> introSkipUpdate(embyNotifyParam);
            case FAVORITES_UPDATE -> favoritesUpdate(embyNotifyParam);
        }
    }

    /**
     * 片头片尾跳过更新
     *
     * @param embyNotifyParam emby 通知参数
     */
    private void introSkipUpdate(EmbyNotifyParam embyNotifyParam) {
        String introSkipUpdate = "片头";
        if (embyNotifyParam.getDescription().contains("片尾标记")) {
            introSkipUpdate = "片尾";
        }
        StringJoiner messageJoiner = new StringJoiner("\n");
        messageJoiner.add(StrUtil.format("#{}更新 #{} #{}", introSkipUpdate, embyNotifyParam.getItem().getSeriesName(), embyNotifyParam.getServer().getName()));
        messageJoiner.add(embyNotifyParam.getDescription());
        telegramService.sendMessage(telegramBotProperties.getChatId(), messageJoiner.toString());
    }

    /**
     * 最爱更新
     *
     * @param embyNotifyParam emby 通知参数
     */
    @SneakyThrows
    private void favoritesUpdate(EmbyNotifyParam embyNotifyParam) {
        ItemRespDTO itemInfo = embyClient.getItem(embyNotifyParam.getItem().getSeriesId());
        MediaPlaybackInfoRespDTO mediaPlaybackInfoRespDTO = embyClient.getPlaybackInfo(embyNotifyParam.getItem().getId());
        MediaPlaybackInfoRespDTO.MediaSource mediaSource = mediaPlaybackInfoRespDTO.getMediaSources().get(0);
        String sizeStr = MediaUtil.formatSize(mediaSource.getSize());
        String bitrateStr = MediaUtil.formatBitrate(mediaSource.getBitrate());

        StringJoiner messageJoiner = new StringJoiner("\n");
        messageJoiner.add(StrUtil.format("#最爱更新 #{} #{}", embyNotifyParam.getItem().getSeriesName(), embyNotifyParam.getServer().getName()));
        messageJoiner.add("\\[" + embyNotifyParam.getItem().getType().getDesc() + "]");
        appendSeriesInfo(embyNotifyParam, messageJoiner);
        messageJoiner.add("视频大小：" + sizeStr);
        messageJoiner.add("视频信息：" + mediaSource.getMediaStreams().get(0).getDisplayTitle());
        messageJoiner.add(StrUtil.format("分辨率：{}x{}", mediaSource.getMediaStreams().get(0).getWidth(), mediaSource.getMediaStreams().get(0).getHeight()));
        messageJoiner.add(StrUtil.format("比特率：{}mbps", bitrateStr));
        messageJoiner.add("帧率：" + mediaSource.getMediaStreams().get(0).getAverageFrameRate().intValue());
        messageJoiner.add("上映日期：" + embyNotifyParam.getItem().getProductionYear());
        messageJoiner.add("内容简介：" + Optional.ofNullable(embyNotifyParam.getItem().getOverview()).orElse(""));
        messageJoiner.add(StrUtil.format("相关链接： [TMDB](https://www.themoviedb.org/tv/{}/season/{}/episode/{})", itemInfo.getProviderIds().getTmdb(),
                embyNotifyParam.getItem().getParentIndexNumber(), embyNotifyParam.getItem().getIndexNumber()));

        Response response = null;
        try {
            response = embyClient.downloadImage(embyNotifyParam.getItem().getId(), embyNotifyParam.getItem().getSeriesPrimaryImageTag());
            // 剧集图片获取失败，尝试获取剧集主图
            if (response.status() != HttpStatus.HTTP_OK) {
                response = embyClient.downloadImage(embyNotifyParam.getItem().getSeriesId(), embyNotifyParam.getItem().getSeriesPrimaryImageTag());
            }
            File file = FileUtil.writeFromStream(response.body().asInputStream(), FileUtil.createTempFile(".jpg", true));
            telegramService.sendMarkdownFile(telegramBotProperties.getChatId(), file, messageJoiner.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private static void appendSeriesInfo(EmbyNotifyParam embyNotifyParam, StringJoiner messageJoiner) {
        appendSeason(embyNotifyParam, messageJoiner);
        if (MediaUtil.isEpisodeNumberMatched(embyNotifyParam.getItem().getName())) {
            messageJoiner.add("集：" + embyNotifyParam.getItem().getName());
        } else {
            messageJoiner.add(StrUtil.format("集：第 {} 集 {}", embyNotifyParam.getItem().getIndexNumber(), embyNotifyParam.getItem().getName()));
        }
    }

    private static void appendSeason(EmbyNotifyParam embyNotifyParam, StringJoiner messageJoiner) {
        messageJoiner.add("片名：" + embyNotifyParam.getItem().getSeriesName());
        if (StrUtil.isBlank(embyNotifyParam.getItem().getSeasonName())) {
            messageJoiner.add(StrUtil.format("季：第 {} 季", embyNotifyParam.getItem().getParentIndexNumber()));
        } else if (MediaUtil.isSeasonNumberMatched(embyNotifyParam.getItem().getSeasonName())) {
            messageJoiner.add("季：" + embyNotifyParam.getItem().getSeasonName());
        } else {
            messageJoiner.add(StrUtil.format("季：第 {} 季 {}", embyNotifyParam.getItem().getParentIndexNumber(), embyNotifyParam.getItem().getSeasonName()));
        }
    }


    /**
     * 深度删除
     *
     * @param embyNotifyParam emby 通知参数
     */
    private void deepDelete(EmbyNotifyParam embyNotifyParam) {
        List<String> deleteItems = extractItemPaths(embyNotifyParam.getDescription());
        String deleteDic;
        Set<String> deleteNames = new HashSet<>();
        StringJoiner messageJoiner = new StringJoiner("\n");
        if (embyNotifyParam.getItem().getType() == EmbyMediaType.MOVIE) {
            messageJoiner.add(StrUtil.format("#影视删除 #{} #{}", embyNotifyParam.getItem().getName(), embyNotifyParam.getServer().getName()));
        } else {
            messageJoiner.add(StrUtil.format("#影视删除 #{} #{}", embyNotifyParam.getItem().getSeriesName(), embyNotifyParam.getServer().getName()));
        }
        messageJoiner.add("[" + embyNotifyParam.getItem().getType().getDesc() + "]");
        switch (embyNotifyParam.getItem().getType()) {
            case MOVIE:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 2);
                deleteNames.add(getLastSegments(deleteItems.get(0), 2).get(0));
                messageJoiner.add("片名：" + embyNotifyParam.getItem().getName());
                break;
            case SEASON:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 2);
                deleteNames.add(getLastSegments(deleteItems.get(0), 2).get(0));
                appendSeason(embyNotifyParam, messageJoiner);
                break;
            case SERIES:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 3);
                deleteNames.add(getLastSegments(deleteItems.get(0), 3).get(0));
                messageJoiner.add("片名：" + embyNotifyParam.getItem().getName());
                break;
            case EPISODE:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 1);
                for (String item : deleteItems) {
                    deleteNames.add(getLastSegments(item, 1).get(0));
                }
                Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(deleteDic, true));
                deleteNames.addAll(convertList(listFileResult.getCheckedData().getContent(),
                        file -> anyMatch(deleteNames, episodeName -> MediaUtil.areEpisodesEqual(file.getName(), episodeName)),
                        listFileRespDTO.Content::getName));
                appendSeriesInfo(embyNotifyParam, messageJoiner);
                break;
            default:
                log.warn("不支持的媒体类型：{}", embyNotifyParam.getItem().getType());
                return;
        }
        log.info("接收到emby深度删除，调用alist删除文件，目录：{},文件：{}", deleteDic, deleteNames);
        alistClient.deleteFile(new DeleteFileReqDTO(deleteDic, deleteNames));
        telegramService.sendMessage(telegramBotProperties.getChatId(), messageJoiner.toString());
    }

    /**
     * 提取项路径
     *
     * @param input 输入
     * @return {@link List }<{@link String }>
     */
    private List<String> extractItemPaths(String input) {
        List<String> paths = new ArrayList<>();
        String[] lines = input.split("\n");
        boolean isMountPathSection = false;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("Mount Paths:")) {
                isMountPathSection = true;
                continue;
            }

            if (isMountPathSection && line.startsWith(configProperties.getAlist().getMediaUrl())) {
                if (configProperties.getEncodeStrmPath()) {
                    paths.add(UrlDecoder.decode(line.substring(configProperties.getAlist().getMediaUrl().length())));
                } else {
                    paths.add(line.substring(configProperties.getAlist().getMediaUrl().length()));
                }
            }
        }
        return paths;
    }

    /**
     * 获取路径中倒数指定层级数的目录/文件名
     *
     * @param path  原始路径
     * @param level 要获取的倒数层级数（例如3表示获取倒数三层）
     * @return 倒数层级的字符串列表，若不满足层数则返回空列表
     */
    public static List<String> getLastSegments(String path, int level) {
        if (path == null || path.isEmpty() || level <= 0) {
            return Collections.emptyList();
        }

        String[] parts = path.split("/");
        if (parts.length < level) {
            return Collections.emptyList();
        }

        return new ArrayList<>(Arrays.asList(parts).subList(parts.length - level, parts.length));
    }

    /**
     * 截取路径中前面 N - level 层（正向）
     * 例如路径有10层，level=2，则保留前8层
     *
     * @param path  原始路径
     * @param level 倒向层级数（表示要排除最后几层）
     * @return 拼接后的子路径字符串
     */
    public static String getPrefixByLevel(String path, int level) {
        if (path == null || path.isEmpty() || level < 0) {
            return "";
        }

        String[] parts = path.split("/");
        int keepLength = parts.length - level;
        if (keepLength <= 0) {
            return "";
        }

        String[] subParts = Arrays.copyOfRange(parts, 0, keepLength);
        String result = String.join("/", subParts);
        return !path.startsWith("/") ? "/" + result : result;
    }

    private HandleFileDTO createHandleFileDTO() {
        HandleFileDTO dto = new HandleFileDTO();
        dto.setFiles(new HashSet<>());
        dto.setIsSingleTask(true);
        return dto;
    }
}
