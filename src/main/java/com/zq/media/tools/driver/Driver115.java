package com.zq.media.tools.driver;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.zq.media.tools.dto.PendingProcessFileDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.resp.driver115.BehaviorDetailDTO;
import com.zq.media.tools.dto.resp.driver115.BehaviorDetailsRespDTO;
import com.zq.media.tools.dto.resp.driver115.FileListRespDTO;
import com.zq.media.tools.dto.resp.driver115.GetDownloadUrlRespDTO;
import com.zq.media.tools.dto.resp.driver115.GetPathRespDTO;
import com.zq.media.tools.dto.resp.driver115.LifeListRespDTO;
import com.zq.media.tools.entity.Media115;
import com.zq.media.tools.enums.BehaviorType;
import com.zq.media.tools.enums.FileCategory;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.feign.Driver115Client;
import com.zq.media.tools.feign.Life115Client;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.service.IMedia115Service;
import com.zq.media.tools.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 115网盘
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 14:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Driver115 {

    private final ConfigProperties configProperties;
    private final Driver115Client driver115Client;
    private final Life115Client life115Client;
    private final RestTemplate restTemplate;
    private final IMedia115Service media115Service;
    private final AlistClient alistClient;

    private final List<BehaviorType> unHandleBehaviorTypes = Arrays.asList(BehaviorType.BROWSE_AUDIO, BehaviorType.BROWSE_IMAGE,
            BehaviorType.BROWSE_VIDEO, BehaviorType.BROWSE_DOCUMENT, BehaviorType.STAR_FILE, BehaviorType.ACCOUNT_SECURITY);

    // 定义正则表达式来匹配32位十六进制的cookie键值对
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(acw_tc=[0-9a-f]+)|([0-9a-f]{32}=[0-9a-f]{32})");

    public static final int behaviorLimit = 32;

    private int strmFileCount, nonStrmFileCount, deleteFileCount, renameVideoCount;

    /**
     * 处理115生活动作
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     */
    public void handleBehavior(LocalDateTime beginTime, LocalDateTime endTime) {
        log.info("开始处理115网盘的生活动作，开始时间：{}，结束时间：{}", beginTime, endTime);
        LocalDateTime exeStartTime = LocalDateTime.now();
        // 初始化计数器
        initializeCounters();
        // 获取生活事件列表
        List<PendingProcessFileDTO> pendingProcessFiles = getLifeBehaviorList(beginTime.toEpochSecond(ZoneOffset.ofHours(8)), endTime == null ? null : endTime.toEpochSecond(ZoneOffset.ofHours(8)));
        // 处理待处理文件
        processPendingFiles(pendingProcessFiles);
        // 输出执行摘要
        logExecutionSummary(exeStartTime);
    }

    /**
     * 获取并处理115网盘的生活事件列表
     *
     * @param startTime 开始时间戳
     * @param endTime   结束时间
     * @return {@link List }<{@link PendingProcessFileDTO }>
     */
    @SneakyThrows
    private List<PendingProcessFileDTO> getLifeBehaviorList(Long startTime, Long endTime) {
        if (endTime == null) {
            endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        }
        LifeListRespDTO lifeListResponse = life115Client.queryLifeList(startTime, endTime, configProperties.getDriver115().getLimit(), null);
        if (lifeListResponse == null || lifeListResponse.getData() == null || lifeListResponse.getData().getList().isEmpty()) {
            return Collections.emptyList();
        }

        List<LifeListRespDTO.BehaviorDTO> lifeList = new ArrayList<>(lifeListResponse.getData().getList());

        // 分页获取所有数据
        while (StrUtil.isNotBlank(lifeListResponse.getData().getLastData())) {
            TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
            lifeListResponse = life115Client.queryLifeList(startTime, endTime, configProperties.getDriver115().getLimit(), lifeListResponse.getData().getLastData());
            if (lifeListResponse == null || lifeListResponse.getData() == null ||
                    lifeListResponse.getData().getList().isEmpty()) {
                break;
            }
            lifeList.addAll(lifeListResponse.getData().getList());
        }
        // 待处理行为列表
        List<PendingProcessFileDTO> pendingProcessFiles = new ArrayList<>();
        Map<String, PendingProcessFileDTO> pendingProcessFileMap = new HashMap<>();
        // 处理行为列表
        for (LifeListRespDTO.BehaviorDTO behavior : lifeList) {
            if (unHandleBehaviorTypes.contains(behavior.getBehaviorType())) {
                continue;
            }
            try {
                pendingProcessFiles.addAll(processBehavior(startTime, endTime, pendingProcessFileMap, behavior));
            } catch (Exception e) {
                log.warn("处理监听事件时发生异常：{}", behavior, e);
            }
        }
        return pendingProcessFiles;
    }

    /**
     * 处理115网盘的生活动作，包括文件的增删改等操作
     *
     * @param startTime             开始时间
     * @param endTime               结束时间
     * @param pendingProcessFileMap 待处理流程文件
     * @param behavior              行为对象，包含操作类型和文件项目列表
     * @return 待处理文件列表
     */
    @SneakyThrows
    private List<PendingProcessFileDTO> processBehavior(Long startTime, Long endTime, Map<String, PendingProcessFileDTO> pendingProcessFileMap, LifeListRespDTO.BehaviorDTO behavior) {
        List<PendingProcessFileDTO> pendingProcessFiles = new ArrayList<>();
        // 查询更多详情
        if (behavior.getTotal() > behavior.getItems().size()) {
            int offset = 0;
            while (true) {
                TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
                BehaviorDetailsRespDTO behaviorDetails = driver115Client.getBehaviorDetails(offset, behaviorLimit, behavior.getBehaviorType().getCode(), behavior.getDate());
                if (behaviorDetails.getState()) {
                    for (BehaviorDetailDTO behaviorDetailDTO : behaviorDetails.getData().getList()) {
                        PendingProcessFileDTO pendingProcessFileDTO = pendingProcessFileMap.get(behaviorDetailDTO.getId());
                        if ((pendingProcessFileDTO != null && pendingProcessFileDTO.getBehaviorType() == behavior.getBehaviorType())
                                || behaviorDetailDTO.getUpdateTime() >= endTime) {
                            continue;
                        }
                        if (behaviorDetailDTO.getUpdateTime() < startTime) {
                            break;
                        }
                        // 构建待处理文件
                        buildPendingProcessFile(pendingProcessFileMap, behavior, behaviorDetailDTO, pendingProcessFiles);
                    }
                    // 是否存在下一页
                    if (Boolean.TRUE.equals(behaviorDetails.getData().getNextPage())) {
                        offset += behaviorLimit;
                    } else {
                        break;
                    }
                } else {
                    log.error("获取行为详情失败，行为类型：{}，日期：{}\n{}", behavior.getBehaviorType(), behavior.getDate(), behaviorDetails);
                }
            }
        } else {
            // 遍历处理每个文件项目
            for (BehaviorDetailDTO behaviorDetailDTO : behavior.getItems()) {
                // 构建待处理文件
                buildPendingProcessFile(pendingProcessFileMap, behavior, behaviorDetailDTO, pendingProcessFiles);
            }
        }

        return pendingProcessFiles;
    }

    private void buildPendingProcessFile(Map<String, PendingProcessFileDTO> pendingProcessFileMap, LifeListRespDTO.BehaviorDTO behavior, BehaviorDetailDTO behaviorDetailDTO, List<PendingProcessFileDTO> pendingProcessFiles) throws InterruptedException {
        // 每个文件处理间隔1秒
        TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
        String path = buildFullPath(behaviorDetailDTO.getFileId());

        PendingProcessFileDTO pendingFile;
        // 根据文件类型分别处理目录和文件
        if (behaviorDetailDTO.getFileCategory() == FileCategory.CATALOG) {
            pendingFile = createPendingFileFromItem(behaviorDetailDTO, path, behavior.getBehaviorType(), true);
            log.info("操作类型：{}，文件夹路径： {}", behavior.getBehaviorType(), path);
        } else {
            pendingFile = createPendingFileFromItem(behaviorDetailDTO, path, behavior.getBehaviorType(), false);
            log.info("操作类型：{}，文件路径： {}", behavior.getBehaviorType(), path);
        }
        pendingProcessFiles.add(pendingFile);
        pendingProcessFileMap.put(behaviorDetailDTO.getId(), pendingFile);
    }

    /**
     * 处理目录类型的文件
     */
    private void processCatalog(BehaviorDetailDTO behaviorDetailDTO, String path, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        // 处理删除文件操作
        if (behaviorType == BehaviorType.DELETE_FILE) {
            PendingProcessFileDTO pendingFile = createPendingFileFromItem(behaviorDetailDTO, path, behaviorType, true);
            pendingProcessFiles.add(pendingFile);
            log.info("操作类型：{}，文件路径： {}", behaviorType, path);
        } else {
            // 递归处理目录内容
            fetchAllFilesInDirectory(behaviorDetailDTO.getFileId(), path, behaviorType, pendingProcessFiles);
        }
    }

    /**
     * 处理普通文件
     */
    private void processPendingFile(BehaviorDetailDTO behaviorDetailDTO, String path, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        PendingProcessFileDTO pendingFile = createPendingFileFromItem(behaviorDetailDTO, path, behaviorType, false);
        pendingProcessFiles.add(pendingFile);
        log.info("操作类型：{}，文件路径： {}", behaviorType, path);
    }

    /**
     * 获取文件的下载URL和相关Cookie信息
     *
     * @param pickCode 文件的唯一标识码
     * @return 包含fileUrl和cookie的Map
     */
    private Map<String, String> getFileUrlAndCookies(String pickCode) {
        Map<String, String> result = new HashMap<>();
        // 构建请求头
        HttpHeaders httpHeaders = buildHttpHeaders();
        String url = "https://webapi.115.com/files/download?pickcode=" + pickCode;

        try {
            // 发送HTTP请求获取下载信息
            ResponseEntity<GetDownloadUrlRespDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(httpHeaders),
                    GetDownloadUrlRespDTO.class
            );

            // 处理成功响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isState()) {
                result.put("fileUrl", response.getBody().getFileUrl());
                result.put("cookie", extractCookies(response.getHeaders().get("Set-Cookie")));
            } else {
                log.error("获取文件下载url失败: \n{}", response);
            }
        } catch (Exception e) {
            log.error("获取文件URL和Cookie时发生异常", e);
        }

        return result;
    }

    /**
     * 构造文件的完整路径
     *
     * @param fileId 文件id
     * @return 完整的文件路径
     */
    private String buildFullPath(String fileId) {
        // 获取文件路径信息
        GetPathRespDTO pathResponse = driver115Client.getFilePath(fileId);
        StringJoiner fullPath = new StringJoiner("/", "/", "");

        // 过滤并构建完整路径
        pathResponse.getPaths().stream()
                .filter(path -> !Objects.equals(path.getFileId(), "0"))
                .forEach(path -> fullPath.add(path.getFileName()));

        return fullPath.add(pathResponse.getFileName()).toString();
    }

    /**
     * 递归获取目录中的所有文件
     *
     * @param cid                 目录ID
     * @param parentPath          父目录路径
     * @param behaviorType        行为类型
     * @param pendingProcessFiles 待处理文件列表
     */
    @SneakyThrows
    private void fetchAllFilesInDirectory(String cid, String parentPath, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        // 获取目录下的文件列表
        TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
        FileListRespDTO fileListResponse = driver115Client.listFiles(cid, configProperties.getDriver115().getLimit());

        for (FileListRespDTO.FileDataDTO file : fileListResponse.getData()) {
            String filePath = parentPath + "/" + file.getFileName();
            // 判断是否为目录
            boolean isDic = !file.getCatalogId().equals(cid);

            // 递归处理子目录
            if (isDic) {
                fetchAllFilesInDirectory(file.getCatalogId(), filePath, behaviorType, pendingProcessFiles);
            }

            // 创建待处理文件对象并添加到列表
            PendingProcessFileDTO pendingFile = buildPendingFile(file, filePath, behaviorType, isDic);
            pendingProcessFiles.add(pendingFile);
        }
    }

    /**
     * 检查文件路径是否需要忽略
     */
    private boolean shouldIgnore(String path) {
        return configProperties.getDriver115().getIgnoreFolders().stream()
                .anyMatch(path::startsWith);
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", configProperties.getDriver115().getCookie());
        headers.set("User-Agent", configProperties.getDriver115().getUserAgent());
        return headers;
    }

    /**
     * 从响应头中提取Cookie信息
     */
    private String extractCookies(List<String> cookieHeaders) {
        if (cookieHeaders == null) {
            return "";
        }
        // 使用正则表达式匹配Cookie
        Matcher matcher = COOKIE_PATTERN.matcher(CollectionUtil.join(cookieHeaders, ";"));
        StringJoiner cookieJoiner = new StringJoiner(";");
        while (matcher.find()) {
            cookieJoiner.add(matcher.group());
        }
        return cookieJoiner.toString();
    }

    /**
     * 从ItemDTO创建待处理文件对象
     */
    private PendingProcessFileDTO createPendingFileFromItem(BehaviorDetailDTO behaviorDetailDTO, String path, BehaviorType behaviorType, boolean isDic) {
        PendingProcessFileDTO dto = new PendingProcessFileDTO();
        return dto.setFileId(behaviorDetailDTO.getFileId())
                .setFileName(behaviorDetailDTO.getFileName())
                .setPickCode(behaviorDetailDTO.getPickCode())
                .setSha1(StrUtil.swapCase(behaviorDetailDTO.getSha1()))
                .setFilePath(path)
                .setParentId(behaviorDetailDTO.getParentId())
                .setExt(behaviorDetailDTO.getExt())
                .setBehaviorType(behaviorType)
                .setIsDic(isDic);
    }

    /**
     * 从FileDataDTO创建待处理文件对象
     */
    private PendingProcessFileDTO buildPendingFile(FileListRespDTO.FileDataDTO file, String filePath, BehaviorType behaviorType, boolean isDic) {
        PendingProcessFileDTO dto = new PendingProcessFileDTO();
        return dto.setFileId(isDic ? file.getCatalogId() : file.getFileId())
                .setFileName(file.getFileName())
                .setPickCode(file.getPickCode())
                .setSha1(file.getSha1())
                .setFilePath(filePath)
                .setParentId(isDic ? file.getParentId() : file.getCatalogId())
                .setExt(file.getExt())
                .setIsDic(isDic)
                .setBehaviorType(behaviorType);
    }

    /**
     * 处理待处理文件列表
     * 遍历并处理所有待处理的文件
     */
    private void processPendingFiles(List<PendingProcessFileDTO> pendingProcessFiles) {
        for (PendingProcessFileDTO pendingFile : pendingProcessFiles) {
            try {
                // 刷新目录
                String path = configProperties.getAlist().getDriver115Path() + FileUtil.getParent(pendingFile.getFilePath(), 1);
                alistClient.listFile(new ListFileReqDTO(path, true));
                processPendingFile(pendingFile);
            } catch (Exception e) {
                log.warn("处理文件时发生异常：{}", pendingFile, e);
            }
        }
    }

    /**
     * 根据文件的行为类型执行相应的处理操作
     *
     * @param pendingFile 待处理的文件信息
     */
    private void processPendingFile(PendingProcessFileDTO pendingFile) {
        Path path = Paths.get(pendingFile.getFilePath());
        Path fullPath = getFullPath(path);

        switch (pendingFile.getBehaviorType()) {
            case RECEIVE_FILES:
            case NEW_FOLDER:
            case COPY_FOLDER:
            case UPLOAD_FILE:
            case UPLOAD_IMAGE_FILE:
                handleFileCreation(fullPath, path, pendingFile);
                break;
            case MOVE_FILE:
            case MOVE_IMAGE_FILE:
                handleFileMove(fullPath, path, pendingFile);
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
            return Paths.get(configProperties.getServer().getDriver115Path(),
                    path.getParent().toString(),
                    FileUtil.mainName(path.getFileName().toString()) + ".strm");
        }
        return Paths.get(configProperties.getServer().getDriver115Path(), path.toString());
    }

    /**
     * 处理文件创建
     * 创建新文件并保存相关信息到数据库
     *
     * @param fullPath    完整文件路径
     * @param path        相对文件路径
     * @param pendingFile 待处理文件信息
     */
    private void handleFileCreation(Path fullPath, Path path, PendingProcessFileDTO pendingFile) {
        // 检查是否在忽略列表中
        if (shouldIgnore(path.toString())) {
            log.debug("忽略{}：{}", pendingFile.getIsDic() ? "文件夹" : "文件", path);
            return;
        }
        Media115 media115 = media115Service.getByFileId(pendingFile.getFileId());
        if (media115 == null) {
            if (!pendingFile.getIsDic()) {
                // 下载文件或创建strm文件
                downloadFileOrCreateStrm(path, pendingFile.getPickCode());
            }
            // 保存文件信息到数据库
            media115 = new Media115()
                    .setFileId(pendingFile.getFileId())
                    .setParentId(pendingFile.getParentId())
                    .setPath(fullPath.toString())
                    .setFileName(pendingFile.getFileName())
                    .setPickCode(pendingFile.getPickCode())
                    .setSha1(StrUtil.swapCase(pendingFile.getSha1()))
                    .setExt(pendingFile.getExt());
            media115Service.save(media115);
        }
    }

    /**
     * 处理文件移动
     * 移动文件并保存相关信息到数据库或者删除相关信息及数据库记录
     *
     * @param fullPath    完整文件路径
     * @param path        相对文件路径
     * @param pendingFile 待处理文件信息
     */
    private void handleFileMove(Path fullPath, Path path, PendingProcessFileDTO pendingFile) {
        Media115 media115 = media115Service.getByFileId(pendingFile.getFileId());
        // 说明是未刮削目录到刮削目录，则执行创建操作
        if (media115 == null) {
            // 检查是否在忽略列表中
            if (shouldIgnore(path.toString())) {
                log.debug("忽略{}：{}", pendingFile.getIsDic() ? "文件夹" : "文件", path);
                return;
            }
            handleFileCreation(fullPath, path, pendingFile);
            if (pendingFile.getIsDic()) {
                List<PendingProcessFileDTO> pendingProcessFiles = new ArrayList<>();
                // 递归处理目录内容
                fetchAllFilesInDirectory(pendingFile.getFileId(), pendingFile.getFilePath(), pendingFile.getBehaviorType(), pendingProcessFiles);
                pendingProcessFiles.forEach(file -> {
                    Path filePath = Paths.get(file.getFilePath());
                    handleFileCreation(getFullPath(filePath), filePath, file);
                });
            }
        } else {
            // 删除旧文件
            handleFileDeletion(pendingFile);
            // 重新创建
            handleFileMove(fullPath, path, pendingFile);
        }
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
            Media115 media115 = media115Service.getByFileId(pendingFile.getFileId());
            if (media115 == null) {
                return;
            }
            deleteFiles.add(media115);
        }

        for (Media115 deleteFile : deleteFiles) {
            FileUtil.del(deleteFile.getPath());
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
        // 检查是否在忽略列表中
        if (shouldIgnore(pendingFile.getFilePath())) {
            log.debug("忽略文件夹：{}", pendingFile.getFilePath());
            return;
        }
        // 查询文件夹下的所有视频文件
        List<Media115> renameFiles = media115Service.queryDescendantsByFileId(pendingFile.getFileId());
        for (Media115 renameFile : renameFiles) {
            if (StrmUtil.isVideoFile(renameFile.getFileName())) {
                Path renamePath = Paths.get(renameFile.getPath()).getParent().resolve(pendingFile.getFileName());
                renameFile.setPath(renamePath.toString());
                media115Service.updateById(renameFile);

                Path strmFilePath = Paths.get(configProperties.getServer().getDriver115Path(), renameFile.getPath());
                StrmUtil.writeStrmFiles(strmFilePath, Paths.get(pendingFile.getFilePath()).toString());
                renameVideoCount++;
                log.info("重命名文件：{}", renameFile.getPath());
            }
        }
    }

    /**
     * 根据文件类型下载文件或创建strm文件
     *
     * @param path     文件路径
     * @param pickCode 文件提取码
     */
    @SneakyThrows
    private void downloadFileOrCreateStrm(Path path, String pickCode) {
        if (StrmUtil.isVideoFile(path)) {
            String strmPath = StrmUtil.generateStrmFiles(Paths.get(configProperties.getAlist().getDriver115Path() + path));
            log.info("生成strm文件: {}", strmPath);
            strmFileCount++;
        } else {
            if (configProperties.getDownloadMediaFile()) {
                Map<String, String> map = getFileUrlAndCookies(pickCode);
                if (!MapUtil.isEmpty(map)) {
                    String fileUrl = map.get("fileUrl");
                    String cookie = map.get("cookie");
                    // 等待1秒，防止风控
                    TimeUnit.SECONDS.sleep(configProperties.getApiRateLimit());
                    Path fullPath = Paths.get(configProperties.getServer().getDriver115Path(), path.toString());
                    StrmUtil.downloadFile(fileUrl, cookie, fullPath);
                    log.info("下载文件：{}", fullPath);
                    nonStrmFileCount++;
                }
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
     * 初始化计数器
     * 重置所有文件操作的计数器
     */
    private void initializeCounters() {
        strmFileCount = 0;
        nonStrmFileCount = 0;
        deleteFileCount = 0;
        renameVideoCount = 0;
    }

}
