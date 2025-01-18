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
    private int strmFileCount = 0;
    private int nonStrmFileCount = 0;
    private int deleteFileCount = 0;
    private int renameVideoCount = 0;

    private List<BehaviorType> unHandleBehaviorTypes = Arrays.asList(BehaviorType.BROWSE_AUDIO, BehaviorType.BROWSE_IMAGE,
            BehaviorType.BROWSE_VIDEO, BehaviorType.BROWSE_DOCUMENT, BehaviorType.STAR_FILE, BehaviorType.ACCOUNT_SECURITY);

    @Scheduled(cron = "0 */${app.client115.intervalMinutes} * * * ?")
    public void monitorLifeEvents() {
        LocalDateTime exeStartTime = LocalDateTime.now();
        strmFileCount = 0;
        nonStrmFileCount = 0;
        deleteFileCount = 0;
        renameVideoCount = 0;
//        long startTime = LocalDateTimeUtil.toEpochMilli(LocalDateTime.of(2024, 11, 15, 16, 0, 0)) / 1000;
        long startTime = System.currentTimeMillis() / 1000 - configProperties.getClient115().getIntervalMinutes() * 60;
        log.info("开始处理115生活监听，处理时间范围：{}-{}，", LocalDateTimeUtil.of(startTime * 1000), LocalDateTime.now());
        try {
            pendingProcessFiles = new ArrayList<>();
            LifeListRespDTO lifeListResponse = life115Client.queryLifeList(startTime, configProperties.getClient115().getLimit(), null);
            if (lifeListResponse != null && lifeListResponse.getData() != null && !lifeListResponse.getData().getList().isEmpty()) {
                List<LifeListRespDTO.BehaviorDTO> lifeList = lifeListResponse.getData().getList();
                // 根据last_data字段进行分页请求
                while (StrUtil.isNotBlank(lifeListResponse.getData().getLastData())) {
                    TimeUnit.SECONDS.sleep(1);
                    lifeListResponse = life115Client.queryLifeList(startTime, configProperties.getClient115().getLimit(), lifeListResponse.getData().getLastData());
                    if (lifeListResponse != null && lifeListResponse.getData() != null && !lifeListResponse.getData().getList().isEmpty()) {
                        lifeList.addAll(lifeListResponse.getData().getList());
                    } else {
                        break;
                    }
                }
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
            for (PendingProcessFileDTO pendingProcessFile : pendingProcessFiles) {
                try {
                    Path path = Paths.get(pendingProcessFile.getFilePath());
                    Path fullPath = Paths.get(configProperties.getServer().getBasePath(), path.toString());
                    boolean isVideoFile = StrmUtil.isVideoFile(path);
                    if (isVideoFile) {
                        fullPath = Paths.get(configProperties.getServer().getBasePath(), path.getParent().toString(), FileUtil.mainName(path.getFileName().toString()) + ".strm");
                    }
                    Media115 media115;
                    switch (pendingProcessFile.getBehaviorType()) {
                        case RECEIVE_FILES:
                        case NEW_FOLDER:
                        case COPY_FOLDER:
                        case MOVE_FILE:
                        case MOVE_IMAGE_FILE:
                        case UPLOAD_FILE:
                        case UPLOAD_IMAGE_FILE:
                            if (Files.exists(fullPath)) {
                                continue;
                            }
                            String filePath = downloadFileAndCreateStrm(path, pendingProcessFile.getPickCode());
                            media115 = new Media115();
                            media115.setFileId(pendingProcessFile.getFileId())
                                    .setParentId(pendingProcessFile.getParentId())
                                    .setPath(filePath)
                                    .setFileName(pendingProcessFile.getFileName())
                                    .setPickCode(pendingProcessFile.getPickCode())
                                    .setSha1(StrUtil.isNotBlank(pendingProcessFile.getSha1()) ? pendingProcessFile.getSha1() : null)
                                    .setExt(pendingProcessFile.getExt())
                            ;
                            media115Service.save(media115);
                            break;
                        case DELETE_FILE:
                            List<Media115> deleteFiles = new ArrayList<>();
                            if (pendingProcessFile.getIsDic()) {
                                // 如果是删除的文件夹，则删除所有子文件
                                deleteFiles = media115Service.queryDescendantsByFileId(pendingProcessFile.getFileId());
                                if (deleteFiles.isEmpty()) {
                                    break;
                                }
                            } else {
                                media115 = media115Service.getBySha1(pendingProcessFile.getSha1().toUpperCase(), pendingProcessFile.getFileName());
                                if (media115 == null) {
                                    break;
                                }
                                deleteFiles.add(media115);
                            }
                            for (Media115 deleteFile : deleteFiles) {
                                FileUtil.del(Paths.get(configProperties.getServer().getBasePath() + deleteFile.getPath()));
                                media115Service.removeById(deleteFile.getId());
                                log.info("删除文件：{}", deleteFile.getPath());
                                deleteFileCount++;
                            }
                            break;
                        case FOLDER_RENAME:
                            List<Media115> renameFiles = media115Service.queryDescendantsByFileId(pendingProcessFile.getFileId());
                            for (Media115 renameFile : renameFiles) {
                                if (StrmUtil.isVideoFile(renameFile.getFileName())) {
                                    Path renamePath = Paths.get(renameFile.getPath() ).getParent().resolve(pendingProcessFile.getFileName());
                                    renameFile.setPath(renamePath.toString());
                                    media115Service.updateById(renameFile);
                                    // 修改strm文件中的路径
                                    Path strmFilePath = Paths.get(configProperties.getServer().getBasePath(), renameFile.getPath());
                                    StrmUtil.writeStrmFiles(strmFilePath, path.toString());
                                    renameVideoCount++;
                                    log.info("重命名文件：{}", renameFile.getPath());
                                }
                            }
                            break;
                        case RENAME_FILE:
                            // 115不支持
//                            if (isVideoFile) {
//                                media115 = media115Service.getBySha1(pendingProcessFile.getSha1());
//                                if (media115 != null) {
//                                    // 修改strm文件中的路径
//                                    Path strmFilePath = Paths.get(configProperties.getServer().getBasePath(), media115.getPath());
//                                    StrmUtil.writeStrmFiles(strmFilePath, path.toString());
//                                    renameVideoCount++;
//                                }
//                            }
                            break;
                        case COPY_FILE:
                            // 115不支持
                            break;
                    }
                } catch (Exception e) {
                    log.error("处理文件时发生异常：{}", pendingProcessFile, e);
                }
            }
        } catch (
                Exception e) {
            log.error("Error monitoring life events: ", e);
        }
        log.info("处理完成，耗时：{}s，共生成{}个strm文件，下载{}个其他文件，删除{}个文件，重命名{}个视频", Duration.between(exeStartTime, LocalDateTime.now()).getSeconds(),
                strmFileCount, nonStrmFileCount, deleteFileCount, renameVideoCount);
    }

    @SneakyThrows
    private String downloadFileAndCreateStrm(Path path, String pickCode) {
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
