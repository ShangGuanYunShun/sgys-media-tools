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
    public List<PendingProcessFileDTO> processBehavior(LifeListRespDTO.BehaviorDTO behavior) {
        List<PendingProcessFileDTO> pendingProcessFiles = new ArrayList<>();
        processLoop:
        for (LifeListRespDTO.ItemDTO item : behavior.getItems()) {
            TimeUnit.SECONDS.sleep(1);
            String path = buildFullPath(item);
            for (String ignoreFolder : configProperties.getClient115().getIgnoreFolders()) {
                if (path.startsWith(ignoreFolder)) {
                    log.debug("忽略文件（夹）：{}", path);
                    continue processLoop;
                }
            }
            // 如果是目录
            if (item.getFileCategory() == FileCategory.CATALOG) {
                // 删除文件夹则不遍历
                if (behavior.getBehaviorType() == BehaviorType.DELETE_FILE) {
                    PendingProcessFileDTO pendingProcessFileDTO = new PendingProcessFileDTO();
                    pendingProcessFileDTO.setFileId(item.getFileId())
                            .setFileName(item.getFileName())
                            .setPickCode(item.getPickCode())
                            .setSha1(item.getSha1())
                            .setFilePath(path)
                            .setParentId(item.getParentId())
                            .setExt(item.getExt())
                            .setBehaviorType(behavior.getBehaviorType())
                            .setIsDic(true)
                    ;
                    pendingProcessFiles.add(pendingProcessFileDTO);
                    log.info("操作类型：{}，文件路径： {}", behavior.getBehaviorType(), path);
                    continue;
                }
                fetchAllFilesInDirectory(item.getFileId(), path, behavior.getBehaviorType(), pendingProcessFiles);
            } else { // 如果是文件
                PendingProcessFileDTO pendingProcessFileDTO = new PendingProcessFileDTO();
                pendingProcessFileDTO.setFileId(item.getFileId())
                        .setFileName(item.getFileName())
                        .setPickCode(item.getPickCode())
                        .setSha1(item.getSha1())
                        .setFilePath(path)
                        .setParentId(item.getParentId())
                        .setExt(item.getExt())
                        .setBehaviorType(behavior.getBehaviorType())
                        .setIsDic(false)
                ;
                pendingProcessFiles.add(pendingProcessFileDTO);
                log.info("操作类型：{}，文件路径： {}", behavior.getBehaviorType(), path);
            }
        }
        return pendingProcessFiles;
    }

    /**
     * 获取文件 URL 和 Cookie
     *
     * @param pickCode 选取代码
     * @return {@link Map }<{@link String }, {@link String }>
     */
    public Map<String, String> getFileUrlAndCookies(String pickCode) {
        Map<String, String> map = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Cookie", configProperties.getClient115().getCookie());
        httpHeaders.set("User-Agent", configProperties.getClient115().getUserAgent());
        String url = "https://webapi.115.com/files/download?pickcode=" + pickCode;
        ResponseEntity<GetDownloadUrlRespDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), GetDownloadUrlRespDTO.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            GetDownloadUrlRespDTO getDownloadUrlRespDTO = responseEntity.getBody();
            if (getDownloadUrlRespDTO.isState()) {
                String fileUrl = getDownloadUrlRespDTO.getFileUrl();

                // 从响应头中获取 Set-Cookie 并进行正则匹配
                List<String> cookieHeaders = responseEntity.getHeaders().get("Set-Cookie");
                Matcher matcher = COOKIE_PATTERN.matcher(CollectionUtil.join(cookieHeaders, ";"));

                StringJoiner cookieJoiner = new StringJoiner(";");
                // 打印并返回匹配到的 Cookie 信息
                while (matcher.find()) {
                    cookieJoiner.add(matcher.group());
                }
                map.put("cookie", cookieJoiner.toString());
                map.put("fileUrl", fileUrl);
                return map;
            }
        } else {
            log.error("获取文件下载url失败: \n{}", responseEntity);
        }
        return map;
    }

    /**
     * 构造完整路径
     *
     * @param item 项目
     * @return {@link String }
     */
    private String buildFullPath(LifeListRespDTO.ItemDTO item) {
        GetPathRespDTO getPathResponse = driver115Client.getFilePath(item.getFileId());
        StringJoiner fullPath = new StringJoiner("/", "/", "");
        for (GetPathRespDTO.PathDTO path : getPathResponse.getPaths()) {
            if (Objects.equals(path.getFileId(), "0")) {
                continue;
            }
            fullPath.add(path.getFileName());
        }
        return fullPath.add(getPathResponse.getFileName()).toString();
    }

    /**
     * 获取目录中所有文件
     *
     * @param cid                 CID
     * @param parentPath          父路径
     * @param behaviorType        行为类型
     * @param pendingProcessFiles 待处理文件
     */
    private void fetchAllFilesInDirectory(String cid, String parentPath, BehaviorType behaviorType, List<PendingProcessFileDTO> pendingProcessFiles) {
        FileListRespDTO fileListResponse = driver115Client.listFiles(cid, configProperties.getClient115().getLimit());
        String lastCid = cid;
        String lastCidPath = parentPath;
        for (FileListRespDTO.FileDataDTO file : fileListResponse.getData()) {
            String filePath;
            if (file.getCatalogId().equals(cid)) {
                filePath = parentPath + "/" + file.getFileName();
            } else {
                // 多级目录
                if (!file.getCatalogId().equals(lastCid)) {
                    GetPathRespDTO getPathResponse = driver115Client.getFilePath(file.getCatalogId());
                    StringJoiner fullPath = new StringJoiner("/");
                    for (GetPathRespDTO.PathDTO path : getPathResponse.getPaths()) {
                        if (Objects.equals(path.getFileId(), "0")) {
                            continue;
                        }
                        fullPath.add(path.getFileName());
                    }
                    fullPath.add(getPathResponse.getFileName());
                    lastCid = file.getCatalogId();
                    lastCidPath = fullPath.toString();
                }
                filePath = lastCidPath + "/" + file.getFileName();
            }
            PendingProcessFileDTO pendingProcessFileDTO = new PendingProcessFileDTO();
            pendingProcessFileDTO.setFileId(file.getFileId())
                    .setFileName(file.getFileName())
                    .setPickCode(file.getPickCode())
                    .setSha1(file.getSha1())
                    .setFilePath(filePath)
                    .setParentId(file.getCatalogId())
                    .setExt(file.getExt())
                    .setBehaviorType(behaviorType)
            ;
            pendingProcessFiles.add(pendingProcessFileDTO);
            log.info("操作类型：{}，文件路径： {}", behaviorType, filePath);
        }
    }
}
