package com.zq.util.strm.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.zq.util.strm.driver.Drive115;
import com.zq.util.strm.dto.PendingProcessFileDTO;
import com.zq.util.strm.dto.resp.driver115.LifeListRespDTO;
import com.zq.util.strm.entity.Media115;
import com.zq.util.strm.enums.BehaviorType;
import com.zq.util.strm.feign.Life115Client;
import com.zq.util.strm.properties.ConfigProperties;
import com.zq.util.strm.service.IMedia115Service;
import com.zq.util.strm.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 115生活监听
 * // TODO 重命名文件夹
 * // TODO 删除文件时未删除文件夹
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:19
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.client115", name = "enabled", havingValue = "true")
public class Client115LifeMonitor {

    private final Life115Client life115Client;
    private final Drive115 drive115;
    private final IMedia115Service media115Service;
    private final ConfigProperties configProperties;

    private List<PendingProcessFileDTO> pendingProcessFiles;
    private int strmFileCount, nonStrmFileCount, deleteFileCount, renameVideoCount;

    private List<BehaviorType> unHandleBehaviorTypes = Arrays.asList(BehaviorType.BROWSE_AUDIO, BehaviorType.BROWSE_IMAGE,
            BehaviorType.BROWSE_VIDEO, BehaviorType.BROWSE_DOCUMENT, BehaviorType.STAR_FILE, BehaviorType.ACCOUNT_SECURITY);

    /**
     * 监控115生活事件
     * 定时任务，监控115网盘的文件变化并同步到本地
     */
    @Scheduled(cron = "0 */${app.client115.intervalMinutes} * * * ?")
    public void monitorLifeEvents() {
        LocalDateTime exeStartTime = LocalDateTime.now();
        initializeCounters();
        
        long startTime = calculateStartTime();
        log.info("开始处理115生活监听，处理时间范围：{}-{}", LocalDateTimeUtil.of(startTime * 1000), LocalDateTime.now());
        
        try {
            pendingProcessFiles = new ArrayList<>();
            processLifeList(startTime);
            processPendingFiles();
        } catch (Exception e) {
            log.error("监控生活事件时发生错误: ", e);
        }

        logExecutionSummary(exeStartTime);
    }

    /**
     * 初始化计数器
     * 重置所有文件操作的计数器
     */
    private void initializeCounters() {
        strmFileCount = 0;
        nonStrmFileCount = 0; 
        deleteFileCount = 0;
        renameVideoCount = 0;
    }

    /**
     * 计算开始时间
     * 根据配置的间隔时间计算监控的起始时间点
     *
     * @return 返回开始时间戳（秒）
     */
    private long calculateStartTime() {
        return LocalDateTime.now()
                .minusMinutes(configProperties.getClient115().getIntervalMinutes())
                .toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 处理生活列表
     * 分页获取并处理115网盘的生活事件列表
     *
     * @param startTime 开始时间戳
     * @throws InterruptedException 当线程休眠被中断时抛出
     */
    private void processLifeList(long startTime) throws InterruptedException {
        LifeListRespDTO lifeListResponse = life115Client.queryLifeList(startTime, configProperties.getClient115().getLimit(), null);
        if (lifeListResponse == null || lifeListResponse.getData() == null || lifeListResponse.getData().getList().isEmpty()) {
            return;
        }

        List<LifeListRespDTO.BehaviorDTO> lifeList = new ArrayList<>(lifeListResponse.getData().getList());
        
        // 分页获取所有数据
        while (StrUtil.isNotBlank(lifeListResponse.getData().getLastData())) {
            TimeUnit.SECONDS.sleep(1);
            lifeListResponse = life115Client.queryLifeList(startTime, configProperties.getClient115().getLimit(), 
                    lifeListResponse.getData().getLastData());
                    
            if (lifeListResponse == null || lifeListResponse.getData() == null || 
                lifeListResponse.getData().getList().isEmpty()) {
                break;
            }
            lifeList.addAll(lifeListResponse.getData().getList());
        }

        // 处理行为列表
        for (LifeListRespDTO.BehaviorDTO behavior : lifeList) {
            if (unHandleBehaviorTypes.contains(behavior.getBehaviorType())) {
                continue;
            }
            try {
                pendingProcessFiles.addAll(drive115.processBehavior(behavior));
            } catch (Exception e) {
                log.warn("处理监听事件时发生异常：{}", behavior, e);
            }
        }
    }

    /**
     * 处理待处理文件列表
     * 遍历并处理所有待处理的文件
     */
    private void processPendingFiles() {
        for (PendingProcessFileDTO pendingFile : pendingProcessFiles) {
            try {
                processFile(pendingFile);
            } catch (Exception e) {
                log.error("处理文件时发生异常：{}", pendingFile, e);
            }
        }
    }

    /**
     * 处理单个文件
     * 根据文件的行为类型执行相应的处理操作
     *
     * @param pendingFile 待处理的文件信息
     * @throws Exception 处理过程中可能发生的异常
     */
    private void processFile(PendingProcessFileDTO pendingFile) throws Exception {
        Path path = Paths.get(pendingFile.getFilePath());
        Path fullPath = getFullPath(path);
        
        switch (pendingFile.getBehaviorType()) {
            case RECEIVE_FILES:
            case NEW_FOLDER:
            case COPY_FOLDER:
            case MOVE_FILE:
            case MOVE_IMAGE_FILE:
            case UPLOAD_FILE:
            case UPLOAD_IMAGE_FILE:
                handleFileCreation(fullPath, path, pendingFile);
                break;
            case DELETE_FILE:
                handleFileDeletion(pendingFile);
                break;
            case FOLDER_RENAME:
                handleFolderRename(pendingFile);
                break;
            case RENAME_FILE:
            case COPY_FILE:
                // 115暂不支持这些操作
                break;
        }
    }

    /**
     * 获取完整文件路径
     * 根据文件类型构建完整的本地文件路径
     *
     * @param path 原始文件路径
     * @return 完整的本地文件路径
     */
    private Path getFullPath(Path path) {
        boolean isVideoFile = StrmUtil.isVideoFile(path);
        if (isVideoFile) {
            return Paths.get(configProperties.getServer().getBasePath(), 
                    path.getParent().toString(), 
                    FileUtil.mainName(path.getFileName().toString()) + ".strm");
        }
        return Paths.get(configProperties.getServer().getBasePath(), path.toString());
    }

    /**
     * 处理文件创建
     * 创建新文件并保存相关信息到数据库
     *
     * @param fullPath 完整文件路径
     * @param path 相对文件路径
     * @param pendingFile 待处理文件信息
     */
    private void handleFileCreation(Path fullPath, Path path, PendingProcessFileDTO pendingFile) {
        if (Files.exists(fullPath)) {
            return;
        }

        String filePath = pendingFile.getFilePath();
        if (!pendingFile.getIsDic()) {
            // 下载文件或创建strm文件
            filePath = downloadFileOrCreateStrm(path, pendingFile.getPickCode());
        }
        // 保存文件信息到数据库
        Media115 media115 = new Media115()
                .setFileId(pendingFile.getFileId())
                .setParentId(pendingFile.getParentId())
                .setPath(filePath)
                .setFileName(pendingFile.getFileName())
                .setPickCode(pendingFile.getPickCode())
                .setSha1(StrUtil.isNotBlank(pendingFile.getSha1()) ? pendingFile.getSha1() : null)
                .setExt(pendingFile.getExt());
        media115Service.save(media115);
    }

    /**
     * 处理文件删除
     * 删除文件及其相关数据库记录
     *
     * @param pendingFile 待处理文件信息
     */
    private void handleFileDeletion(PendingProcessFileDTO pendingFile) {
        List<Media115> deleteFiles = new ArrayList<>();
        // 如果是文件夹，则删除所有子文件和子文件夹
        if (pendingFile.getIsDic()) {
            deleteFiles = media115Service.queryDescendantsByFileId(pendingFile.getFileId());
            if (deleteFiles.isEmpty()) {
                return;
            }
        } else {
            Media115 media115 = media115Service.getBySha1(pendingFile.getSha1().toUpperCase(), pendingFile.getFileName());
            if (media115 == null) {
                return;
            }
            deleteFiles.add(media115);
        }

        for (Media115 deleteFile : deleteFiles) {
            FileUtil.del(Paths.get(configProperties.getServer().getBasePath() + deleteFile.getPath()));
            media115Service.removeById(deleteFile.getId());
            log.info("删除文件：{}", deleteFile.getPath());
            deleteFileCount++;
        }
    }

    /**
     * 处理文件夹重命名
     * 重命名文件夹及其包含的视频文件
     *
     * @param pendingFile 待处理文件信息
     */
    private void handleFolderRename(PendingProcessFileDTO pendingFile) {
        // 查询文件夹下的所有视频文件
        List<Media115> renameFiles = media115Service.queryDescendantsByFileId(pendingFile.getFileId());
        for (Media115 renameFile : renameFiles) {
            if (StrmUtil.isVideoFile(renameFile.getFileName())) {
                Path renamePath = Paths.get(renameFile.getPath()).getParent().resolve(pendingFile.getFileName());
                renameFile.setPath(renamePath.toString());
                media115Service.updateById(renameFile);
                
                Path strmFilePath = Paths.get(configProperties.getServer().getBasePath(), renameFile.getPath());
                StrmUtil.writeStrmFiles(strmFilePath, Paths.get(pendingFile.getFilePath()).toString());
                renameVideoCount++;
                log.info("重命名文件：{}", renameFile.getPath());
            }
        }
    }

    /**
     * 记录执行摘要
     * 记录处理完成的统计信息和耗时
     *
     * @param exeStartTime 执行开始时间
     */
    private void logExecutionSummary(LocalDateTime exeStartTime) {
        log.info("处理完成，耗时：{}s，共生成{}个strm文件，下载{}个其他文件，删除{}个文件，重命名{}个视频", 
                Duration.between(exeStartTime, LocalDateTime.now()).getSeconds(),
                strmFileCount, nonStrmFileCount, deleteFileCount, renameVideoCount);
    }

    /**
     * 下载文件或者创建strm文件
     * 根据文件类型下载文件或创建strm文件
     *
     * @param path 文件路径
     * @param pickCode 文件提取码
     * @return 处理后的文件路径
     */
    @SneakyThrows
    private String downloadFileOrCreateStrm(Path path, String pickCode) {
        if (StrmUtil.isVideoFile(path)) {
            String strmPath = StrmUtil.generateStrmFiles(path);
            log.info("生成strm文件: {}", strmPath);
            strmFileCount++;
            return strmPath;
        } else {
            Map<String, String> map = drive115.getFileUrlAndCookies(pickCode);
            if (!MapUtil.isEmpty(map)) {
                String fileUrl = map.get("fileUrl");
                String cookie = map.get("cookie");
                // 等待1秒，防止风控
                TimeUnit.SECONDS.sleep(1);
                Path fullPath = Paths.get(configProperties.getServer().getBasePath(), path.toString());
                StrmUtil.downloadFile(fileUrl, cookie, fullPath);
                log.info("下载文件：{}", path);
                nonStrmFileCount++;
                return path.toString();
            }
        }
        return null;
    }

}
