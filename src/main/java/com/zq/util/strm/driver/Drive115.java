package com.zq.util.strm.driver;

import cn.hutool.core.collection.CollectionUtil;
import com.zq.util.strm.dto.PendingProcessFileDTO;
import com.zq.util.strm.dto.resp.driver115.FileListRespDTO;
import com.zq.util.strm.dto.resp.driver115.GetDownloadUrlRespDTO;
import com.zq.util.strm.dto.resp.driver115.GetPathRespDTO;
import com.zq.util.strm.dto.resp.driver115.LifeListRespDTO;
import com.zq.util.strm.enums.BehaviorType;
import com.zq.util.strm.enums.FileCategory;
import com.zq.util.strm.feign.Driver115Client;
import com.zq.util.strm.properties.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
public class Drive115 {

    private final ConfigProperties configProperties;
    private final Driver115Client driver115Client;
    private final RestTemplate restTemplate;

    // 定义正则表达式来匹配32位十六进制的cookie键值对
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(acw_tc=[0-9a-f]+)|([0-9a-f]{32}=[0-9a-f]{32})");

    /**
     * 处理115生活动作
     *
     * @param behavior 行为
     * @return {@link List }<{@link PendingProcessFileDTO }>
     */
    @SneakyThrows
    /**
     * 处理115网盘的生活动作，包括文件的增删改等操作
     *
     * @param behavior 行为对象，包含操作类型和文件项目列表
     * @return 待处理文件列表
     */
    public List<PendingProcessFileDTO> processBehavior(LifeListRespDTO.BehaviorDTO behavior) {
        List<PendingProcessFileDTO> pendingProcessFiles = new ArrayList<>();
        
        // 遍历处理每个文件项目
        for (LifeListRespDTO.ItemDTO item : behavior.getItems()) {
            try {
                // 每个文件处理间隔1秒
                TimeUnit.SECONDS.sleep(1);
                String path = buildFullPath(item);
                
                // 检查是否在忽略列表中
                if (shouldIgnore(path)) {
                    log.debug("忽略文件（夹）：{}", path);
                    continue;
                }
                
                // 根据文件类型分别处理目录和文件
                if (item.getFileCategory() == FileCategory.CATALOG) {
                    processCatalog(item, path, behavior.getBehaviorType(), pendingProcessFiles);
                } else {
                    processFile(item, path, behavior.getBehaviorType(), pendingProcessFiles);
                }
            } catch (InterruptedException e) {
                log.error("处理行为时发生中断异常", e);
                Thread.currentThread().interrupt();
            }
        }
        return pendingProcessFiles;
    }

    /**
     * 获取文件的下载URL和相关Cookie信息
     *
     * @param pickCode 文件的唯一标识码
     * @return 包含fileUrl和cookie的Map
     */
    public Map<String, String> getFileUrlAndCookies(String pickCode) {
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
     * @param item 文件项目对象
     * @return 完整的文件路径
     */
    private String buildFullPath(LifeListRespDTO.ItemDTO item) {
        // 获取文件路径信息
        GetPathRespDTO pathResponse = driver115Client.getFilePath(item.getFileId());
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
     * @param cid 目录ID
     * @param parentPath 父目录路径
     * @param behaviorType 行为类型
     * @param pendingProcessFiles 待处理文件列表
     */
    private void fetchAllFilesInDirectory(String cid, String parentPath, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        // 获取目录下的文件列表
        FileListRespDTO fileListResponse = driver115Client.listFiles(cid, configProperties.getClient115().getLimit());
        
        for (FileListRespDTO.FileDataDTO file : fileListResponse.getData()) {
            String filePath = parentPath + "/" + file.getFileName();
            // 判断是否为目录
            boolean isDic = !file.getCatalogId().equals(cid);
            
            // 递归处理子目录
            if (isDic) {
                log.info("操作类型：{}，文件路径： {}", behaviorType, filePath);
                fetchAllFilesInDirectory(file.getCatalogId(), filePath, behaviorType, pendingProcessFiles);
            }
            
            // 创建待处理文件对象并添加到列表
            PendingProcessFileDTO pendingFile = createPendingFile(file, filePath, behaviorType, isDic);
            pendingProcessFiles.add(pendingFile);
            log.info("操作类型：{}，文件路径： {}", behaviorType, filePath);
        }
    }
    
    /**
     * 检查文件路径是否需要忽略
     */
    private boolean shouldIgnore(String path) {
        return configProperties.getClient115().getIgnoreFolders().stream()
                .anyMatch(path::startsWith);
    }
    
    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", configProperties.getClient115().getCookie());
        headers.set("User-Agent", configProperties.getClient115().getUserAgent());
        return headers;
    }
    
    /**
     * 从响应头中提取Cookie信息
     */
    private String extractCookies(List<String> cookieHeaders) {
        if (cookieHeaders == null) return "";
        // 使用正则表达式匹配Cookie
        Matcher matcher = COOKIE_PATTERN.matcher(CollectionUtil.join(cookieHeaders, ";"));
        StringJoiner cookieJoiner = new StringJoiner(";");
        while (matcher.find()) {
            cookieJoiner.add(matcher.group());
        }
        return cookieJoiner.toString();
    }
    
    /**
     * 处理目录类型的文件
     */
    private void processCatalog(LifeListRespDTO.ItemDTO item, String path, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        // 处理删除文件操作
        if (behaviorType == BehaviorType.DELETE_FILE) {
            PendingProcessFileDTO pendingFile = createPendingFileFromItem(item, path, behaviorType, true);
            pendingProcessFiles.add(pendingFile);
            log.info("操作类型：{}，文件路径： {}", behaviorType, path);
        } else {
            // 递归处理目录内容
            fetchAllFilesInDirectory(item.getFileId(), path, behaviorType, pendingProcessFiles);
        }
    }
    
    /**
     * 处理普通文件
     */
    private void processFile(LifeListRespDTO.ItemDTO item, String path, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        PendingProcessFileDTO pendingFile = createPendingFileFromItem(item, path, behaviorType, false);
        pendingProcessFiles.add(pendingFile);
        log.info("操作类型：{}，文件路径： {}", behaviorType, path);
    }
    
    /**
     * 从ItemDTO创建待处理文件对象
     */
    private PendingProcessFileDTO createPendingFileFromItem(LifeListRespDTO.ItemDTO item, String path, BehaviorType behaviorType, boolean isDic) {
        PendingProcessFileDTO dto = new PendingProcessFileDTO();
        return dto.setFileId(item.getFileId())
                 .setFileName(item.getFileName())
                 .setPickCode(item.getPickCode())
                 .setSha1(item.getSha1())
                 .setFilePath(path)
                 .setParentId(item.getParentId())
                 .setExt(item.getExt())
                 .setBehaviorType(behaviorType)
                 .setIsDic(isDic);
    }
    
    /**
     * 从FileDataDTO创建待处理文件对象
     */
    private PendingProcessFileDTO createPendingFile(FileListRespDTO.FileDataDTO file, String filePath, BehaviorType behaviorType, boolean isDic) {
        PendingProcessFileDTO dto = new PendingProcessFileDTO();
        return dto.setFileId(file.getFileId())
                 .setFileName(file.getFileName())
                 .setPickCode(file.getPickCode())
                 .setSha1(file.getSha1())
                 .setFilePath(filePath)
                 .setParentId(file.getCatalogId())
                 .setExt(file.getExt())
                 .setIsDic(isDic)
                 .setBehaviorType(behaviorType);
    }
}
