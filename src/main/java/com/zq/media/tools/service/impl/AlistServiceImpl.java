package com.zq.media.tools.service.impl;

import com.zq.common.domain.Result;
import com.zq.common.util.ThreadUtil;
import com.zq.media.tools.dto.HandleFileDTO;
import com.zq.media.tools.dto.req.alist.CopyFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.req.alist.MoveFileReqDTO;
import com.zq.media.tools.dto.req.alist.RenameFileReqDTO;
import com.zq.media.tools.dto.req.ttm.TtmReqDTO;
import com.zq.media.tools.dto.resp.alist.TaskRespDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import com.zq.media.tools.enums.TtmAction;
import com.zq.media.tools.enums.TtmScopeName;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.feign.TtmClient;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.service.IAlistService;
import com.zq.media.tools.util.MediaUtil;
import com.zq.media.tools.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.io.file.FileNameUtil;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.RuntimeUtil;
import org.dromara.hutool.http.client.HttpDownloader;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zq.common.util.CollectionUtil.anyMatch;
import static com.zq.common.util.CollectionUtil.convertSet;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 13:04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlistServiceImpl implements IAlistService {

    private final ConfigProperties configProperties;
    private final AlistClient alistClient;
    private final TtmClient ttmClient;

    private final Map<Integer, Integer> taskIds = new HashMap<>();
    AtomicInteger taskIndex = new AtomicInteger(0);

    /**
     * 处理云盘自动保存
     *
     * @param handleFile handle 文件
     */
    @Override
    public void handleCloudAutoSave(HandleFileDTO handleFile) {
        // 1、获取目标文件夹下已存在的文件列表
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(handleFile.getFolderPath(), false));
        Set<String> existingEpisodes = convertSet(listFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);

        // 2、过滤出需要复制的新文件
        Set<String> newFiles = new HashSet<>();
        List<String> handleFiles = filterDuplicateEpisodes(handleFile.getFiles());
        for (String file : handleFiles) {
            boolean exists = false;
            if (handleFile.getIsSingleTask()) {
                exists = anyMatch(existingEpisodes,
                        existingFile -> !existingFile.equals(file) && MediaUtil.areEpisodesEqual(existingFile, file));
            }
            if (exists) {
                log.info("剧集已存在: {}\n{}", handleFile.getFolderPath(), file);
            } else {
                newFiles.add(file);
            }
        }

        if (newFiles.isEmpty()) {
            log.info("所有剧集已存在,无需复制: {}\n{}", handleFile.getFolderPath(), handleFiles);
            return;
        }

        // 3、刷新文件列表并准备复制
        alistClient.listFile(new ListFileReqDTO(handleFile.getFolderPath(), true));
        // 剧集名
        String seriesName = FileNameUtil.getName(handleFile.getFolderPath());
        String scrapPath = configProperties.getAlist().getScrapPath().get(seriesName);

        // 4、判断目标网盘是否已经存在此文件了
        Result<listFileRespDTO> destListFileResult = alistClient.listFile(new ListFileReqDTO(scrapPath, true));
        Set<String> existingFiles = convertSet(destListFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);
        newFiles.removeIf(file -> {
            if (existingFiles.contains(file)) {
                log.info("目标网盘已存在此文件: {}", file);
                return true;
            }
            return false;
        });

        if (newFiles.isEmpty()) {
            log.info("目标网盘已存在所有文件,无需复制，直接刮削: {}", scrapPath);
            // 直接触发刮削和移动文件
            processCompletedCopy(seriesName, scrapPath);
            return;
        }

        // 5、执行文件复制
        CopyFileReqDTO copyRequest = new CopyFileReqDTO();
        copyRequest.setSrcDir(handleFile.getFolderPath())
                .setDstDir(scrapPath)
                .setNames(newFiles);

        alistClient.copyFile(copyRequest);
        log.info("复制文件: {} -> {}, 文件列表: {}", handleFile.getFolderPath(), scrapPath, newFiles);

        // 6、启动复制监控任务
        AtomicBoolean copyTaskDone = new AtomicBoolean(false);
        int taskIndex = this.taskIndex.addAndGet(1);
        try {
            // 等待10秒，防止同网盘内复制，无复制任务的情况
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
        int taskId = ThreadUtil.executeCycle(
                () -> copyFileMonitor(copyTaskDone, taskIndex, seriesName, scrapPath),
                5,
                ChronoUnit.MINUTES
        );
        taskIds.put(taskIndex, taskId);
    }

    /**
     * 处理目录（创建 STRM 并下载文件）
     *
     * @param mediaPath 媒体路径
     */
    @Override
    public void processDic(String mediaPath) {
        try {
            TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
        } catch (InterruptedException ignored) {
        }
        log.info("处理alist目录: {}", mediaPath);
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(mediaPath, true));
        if (listFileResult.isSuccess()) {
            for (listFileRespDTO.Content content : listFileResult.getCheckedData().getContent()) {
                if (content.getIsDir()) {
                    // 构建子目录的完整路径
                    String subDirPath = Paths.get(mediaPath, content.getName()).toString();
                    processDic(subDirPath);
                } else {
                    // 构建文件的完整路径
                    Path filePath = Paths.get(mediaPath, content.getName());
                    createStrmAndDownloadFile(filePath);
                }
            }
        } else {
            log.error("获取alist文件列表失败: {}", mediaPath);
        }
    }

    /**
     * 查询列表文件通过目录
     *
     * @param folderPath 文件夹路径
     * @return {@link Set }<{@link String }>
     */
    @Override
    public Set<String> queryListFileByDic(String folderPath) {
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(folderPath, true));
        return convertSet(listFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);
    }

    /**
     * 刮削
     */
    private void scrap() {
        List<TtmReqDTO> ttmReqDTOList = new ArrayList<>();
        TtmReqDTO ttmReqDTO = new TtmReqDTO();

        ttmReqDTO.setAction(TtmAction.UPDATE)
                .setScope(new TtmReqDTO.Scope(TtmScopeName.ALL, new ArrayList<>()));
        ttmReqDTOList.add(ttmReqDTO);
        ttmReqDTO = new TtmReqDTO();
        ttmReqDTO.setAction(TtmAction.SCRAPE)
                .setScope(new TtmReqDTO.Scope(TtmScopeName.NEW, new ArrayList<>()));
        ttmReqDTOList.add(ttmReqDTO);
        ttmClient.execute(ttmReqDTOList);
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException ignored) {
        }
        ttmClient.execute(ttmReqDTOList);
        try {
            // 等待刮削完成
            TimeUnit.SECONDS.sleep(configProperties.getTtm().getScrapTime());
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 监控文件复制进度
     *
     * @param copyTaskDone 复制任务完成标志
     * @param index        任务索引
     * @param seriesName   剧集名
     * @param scrapPath    刮削路径
     */
    private void copyFileMonitor(AtomicBoolean copyTaskDone, int index, String seriesName, String scrapPath) {
        // 获取复制任务列表
        Result<List<TaskRespDTO>> copyUndoneTaskListResult = alistClient.listCopyUndoneTask();
        log.debug("复制文件监视器：{}", copyUndoneTaskListResult.getCheckedData());

        // 检查任务是否完成
        if (copyUndoneTaskListResult.getCheckedData().isEmpty()) {
            copyTaskDone.set(true);
        }

        if (!copyTaskDone.get()) {
            return;
        }

        // 异步处理后续任务
        ThreadUtil.execute(() -> processCompletedCopy(seriesName, scrapPath));

        // 停止监听任务
        log.info("复制文件已完成，停止监听任务：{}-{}", taskIds.get(index), scrapPath);
        ThreadUtil.stop(taskIds.get(index));
    }

    /**
     * 处理复制完成后的任务
     * 包括刷新目录、刮削文件和移动文件等操作
     *
     * @param seriesName 剧集名
     * @param scrapPath  刮削路径
     */
    private void processCompletedCopy(String seriesName, String scrapPath) {
        // 剧集组重命名
        if (!configProperties.getEpisodeGroup().isEmpty()) {
            for (String episodeGroup : configProperties.getEpisodeGroup()) {
                if (episodeGroup.contains(seriesName)) {
                    // 重命名剧集组
                    List<RenameFileReqDTO.RenameFile> renameFileList = getRenameFiles(scrapPath, episodeGroup);
                    log.info("剧集组重命名：{}: {}", scrapPath, renameFileList);
                    alistClient.renameFile(new RenameFileReqDTO(scrapPath, renameFileList));
                }
            }
        }

        if (configProperties.getCloudDrive().getEnabled()) {
            // 刷新cd2目录
            refreshCD2Directory(scrapPath);
        }

        if (configProperties.getTtm().getEnabled()) {
            // 刷新目录
            alistClient.listFile(new ListFileReqDTO(scrapPath, true));
            // 刮削文件
            log.info("刮削文件：{}", scrapPath);
            scrap();
        }

        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(scrapPath, true));
        Set<String> fileNames = convertSet(listFileResult.getCheckedData().getContent(),
                content -> {
                    if ("season.nfo".equals(content.getName())) {
                        return false;
                    }
                    // 强迫症福音，过滤掉无用文件：ttm刮削之后，文件名会变成  剧集名-episodes-XXX
                    if (configProperties.getTtm().getEnableRename()) {
                        String episodes = MediaUtil.getEpisodes(content.getName());
                        return content.getName().startsWith(seriesName + " - " + episodes);
                    }
                    return true;
                },
                listFileRespDTO.Content::getName);

        // 移动文件到目标目录
        String targetPath = moveFilesToTarget(scrapPath, fileNames);

        // 非115网盘触发生成strm和下载文件
        if (!scrapPath.contains("115")) {
            // 生成strm和下载文件
            for (String fileName : fileNames) {
                createStrmAndDownloadFile(Paths.get(targetPath + "/" + fileName));
            }
        }
    }

    @NotNull
    private List<RenameFileReqDTO.RenameFile> getRenameFiles(String scrapPath, String episodeGroup) {
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(scrapPath, false));
        List<RenameFileReqDTO.RenameFile> renameFileList = new ArrayList<>();
        listFileResult.getCheckedData().getContent().forEach(content -> {
            if (!content.getIsDir()) {
                String[] split = episodeGroup.split("\\|");
                // 解析替换规则。比如：S01E135-S01E152 -> S07E11-S07E28
                String[] parts = split[1].split(" -> ");
                String[] oldParts = parts[0].split("-");
                String[] newParts = parts[1].split("-");

                // 旧季号，如 01
                String oldSeason = MediaUtil.getSeason(oldParts[0]);
                // 135
                int oldStart = MediaUtil.getEpisode(oldParts[0]);
                // 152
                int oldEnd = MediaUtil.getEpisode(oldParts[1]);

                // 新季号，如 07
                String newSeason = MediaUtil.getSeason(newParts[0]);
                // 11
                int newStart = MediaUtil.getEpisode(newParts[0]);

                // 动态构造正则匹配 SxxE135 - SxxE252
                String regex = "S" + oldSeason + "E(" + oldStart;
                for (int i = oldStart + 1; i <= oldEnd; i++) {
                    regex += "|" + i;
                }
                regex += ")";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content.getName());

                // 替换逻辑
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    String oldEpisode = matcher.group();
                    // 提取集数
                    int oldEpisodeNum = MediaUtil.getEpisode(oldEpisode);
                    // 计算新集数
                    int newEpisodeNum = newStart + (oldEpisodeNum - oldStart);
                    String newEpisode = StrUtil.format("S{}E{}", newSeason, newEpisodeNum);

                    matcher.appendReplacement(result, newEpisode);
                }
                matcher.appendTail(result);

                renameFileList.add(new RenameFileReqDTO.RenameFile(content.getName(), result.toString()));
            }
        });
        return renameFileList;
    }

    /**
     * 刷新CD2目录
     * 通过执行Python脚本来刷新目录内容
     * 主要针对于115网盘，因为通过alist挂载刮削115容易出现冗余文件
     *
     * @param scrapPath 需要刷新的目录路径
     */
    private void refreshCD2Directory(String scrapPath) {
        try {
            String[] script = {"python3", "/app/python/clouddrive_api.py",
                    configProperties.getCloudDrive().getUrl(),
                    configProperties.getCloudDrive().getUsername(),
                    configProperties.getCloudDrive().getPassword(),
                    "list_files", scrapPath};

            log.debug("执行python脚本：{}", Arrays.asList(script));
            String execResult = RuntimeUtil.execForStr(script);
            log.debug("python执行结果：{}", execResult);

            TimeUnit.SECONDS.sleep(10);
        } catch (Exception ignored) {
        }
    }

    /**
     * 将文件移动到目标目录
     * 获取文件列表并移动到对应的电视剧目录
     *
     * @param scrapPath 源文件路径
     * @param fileNames 文件名列表
     */
    private String moveFilesToTarget(String scrapPath, Set<String> fileNames) {
        // 获取电视节目名称和目标路径
        String tvShowName = FileNameUtil.mainName(FileUtil.getParent(scrapPath, 1));
        String targetPath = configProperties.getAlist().getSerializedTvShow().get(tvShowName);
        // 构建移动请求
        MoveFileReqDTO moveFileReqDTO = new MoveFileReqDTO()
                .setSrcDir(scrapPath)
                .setDstDir(targetPath)
                .setNames(fileNames);

        log.info("移动文件：{}-{} -> {}", scrapPath, fileNames, targetPath);
        alistClient.moveFile(moveFileReqDTO);
        try {
            TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
        } catch (InterruptedException ignored) {
        }
        // alist刷新目标目录
        alistClient.listFile(new ListFileReqDTO(targetPath, true));
        log.info("新增剧集处理完成：{}-{}", tvShowName, MediaUtil.getEpisodes(CollUtil.getFirst(fileNames)));
        return targetPath;
    }

    /**
     * 下载文件并创建strm文件
     *
     * @param path 路径
     */
    private void createStrmAndDownloadFile(Path path) {
        if (MediaUtil.isVideoFile(path)) {
            String strmPath = StrmUtil.generateStrmFiles(path);
            log.info("生成strm文件: {}", strmPath);
        } else {
            if (configProperties.getDownloadMediaFile()) {
                Path fullPath = Paths.get(configProperties.getServer().getBasePath(), path.toString());
                try {
                    TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
                } catch (InterruptedException ignored) {
                }
                HttpDownloader.of(configProperties.getAlist().getMediaUrl() + path).downloadFile(fullPath.toFile());
                log.info("下载文件：{}", fullPath);
            }
        }
    }

    /**
     * 过滤所有重复的剧集文件名
     *
     * @param fileList 文件名列表
     * @return 过滤后的文件名列表
     */
    public static List<String> filterDuplicateEpisodes(Set<String> fileList) {
        // 用于存储已经处理过的剧集编号
        Set<String> seenEpisodes = new HashSet<>();
        // 用于存储过滤后的文件名
        List<String> filteredList = new ArrayList<>();

        // 正则表达式匹配剧集编号（如 S01E150）
        Pattern pattern = Pattern.compile("S\\d{2}E\\d+");

        for (String fileName : fileList) {
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                // 提取剧集编号
                String episode = matcher.group();
                if (!seenEpisodes.contains(episode)) {
                    // 标记为已处理
                    seenEpisodes.add(episode);
                    // 添加到过滤后的列表
                    filteredList.add(fileName);
                }
            } else {
                // 如果没有剧集编号，直接添加到过滤后的列表
                filteredList.add(fileName);
            }
        }

        return filteredList;
    }
}