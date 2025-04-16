package com.zq.media.tools.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.zq.common.domain.Result;
import com.zq.common.util.ThreadUtil;
import com.zq.media.tools.dto.HandleFileDTO;
import com.zq.media.tools.dto.req.alist.DeleteFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import com.zq.media.tools.enums.EmbyEvent;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.params.EmbyNotifyParam;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.properties.TelegramBotProperties;
import com.zq.media.tools.service.IAlistService;
import com.zq.media.tools.service.IReceiveNotificationService;
import com.zq.media.tools.service.ITelegramBotService;
import com.zq.media.tools.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final ITelegramBotService telegramService;
    private final TelegramBotProperties telegramBotProperties;

    @Override
    public void receiveQuarkAutoSave(String content) {
        ThreadUtil.execute(() -> {
            List<String> split = StrUtil.split(content, "\\n");
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
                } else if (isFolderPathSet && StrmUtil.isEpisodes(str)) {
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
            list.forEach(alistService::copyFileQuarkTo115);
        });
    }

    /**
     * 接收 Emby 的神医通知
     *
     * @param embyNotifyParam emby 通知参数
     */
    @Override
    public void receiveEmbyFromShenYi(EmbyNotifyParam embyNotifyParam) {
        //TODO 暂时只做简单通知
        if (embyNotifyParam.getEvent() == EmbyEvent.DEEP_DELETE) {
            deepDelete(embyNotifyParam);
        } else {
            telegramService.sendMessage(telegramBotProperties.getChatId(), embyNotifyParam.getDescription());
        }
    }

    private void deepDelete(EmbyNotifyParam embyNotifyParam) {
        List<String> deleteItems = extractItemPaths(embyNotifyParam.getDescription());
        String deleteDic;
        Set<String> deleteNames = new HashSet<>();
        String message;
        switch (embyNotifyParam.getItem().getType()) {
            case MOVIE:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 2);
                deleteNames.add(getLastSegments(deleteItems.get(0), 2).get(0));
                message = StrUtil.format("删除剧集：{}\n季：{}\n", embyNotifyParam.getItem().getSeriesName(), embyNotifyParam.getItem().getName());
                break;
            case SEASON:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 2);
                deleteNames.add(getLastSegments(deleteItems.get(0), 2).get(0));
                message = StrUtil.format("删除电影：{}", embyNotifyParam.getItem().getName());
                break;
            case SERIES:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 3);
                deleteNames.add(getLastSegments(deleteItems.get(0), 3).get(0));
                message = StrUtil.format("删除剧集：{}", embyNotifyParam.getItem().getSeriesName());
                break;
            case EPISODE:
                deleteDic = getPrefixByLevel(deleteItems.get(0), 1);
                for (String item : deleteItems) {
                    deleteNames.add(getLastSegments(item, 1).get(0));
                }
                Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(deleteDic, true));
                deleteNames.addAll(convertList(listFileResult.getCheckedData().getContent(),
                        file -> anyMatch(deleteNames, episodeName -> StrmUtil.areEpisodesEqual(file.getName(), episodeName)),
                        listFileRespDTO.Content::getName));
                message = StrUtil.format("删除剧集：{}\n季：{}\n集：{} - {}", embyNotifyParam.getItem().getSeriesName(), embyNotifyParam.getItem().getSeasonName(),
                        embyNotifyParam.getItem().getIndexNumber(), embyNotifyParam.getItem().getName());
                break;
            default:
                log.warn("不支持的媒体类型：{}", embyNotifyParam.getItem().getType());
                return;
        }
        log.info("接收到emby深度删除，调用alist删除文件，目录：{},文件：{}", deleteDic, deleteNames);
        alistClient.deleteFile(new DeleteFileReqDTO(deleteDic, deleteNames));
        telegramService.sendMessage(telegramBotProperties.getChatId(), message);
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
                    paths.add(URLUtil.decode(line.substring(configProperties.getAlist().getMediaUrl().length())));
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
     * @param path 原始路径
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
