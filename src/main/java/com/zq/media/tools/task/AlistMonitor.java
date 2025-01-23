package com.zq.media.tools.task;

import cn.hutool.core.io.FileUtil;
import com.zq.common.domain.Result;
import com.zq.common.util.CollectionUtil;
import com.zq.media.tools.dto.req.alist.CopyFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.req.ttm.TtmReqDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import com.zq.media.tools.enums.TtmAction;
import com.zq.media.tools.enums.TtmScopeName;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.feign.TtmClient;
import com.zq.media.tools.properties.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * alist监听
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 16:22
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.alist", name = "enabled", havingValue = "true")
public class AlistMonitor {

    private final ConfigProperties configProperties;
    private final AlistClient alistClient;
    private final TtmClient ttmClient;

    @Scheduled(cron = "0 */${app.alist.intervalMinutes} * * * ?")
    public void monitorAlistFile() {
        log.info("开始处理alist文件监听...");
        List<String> baseMonitorPath = configProperties.getAlist().getBaseMonitorPath();
        for (String basePath : baseMonitorPath) {
            // 基本监听路径强制刷新获取最新的文件
            alistClient.listFile(new ListFileReqDTO(basePath, true));
        }
        List<String> monitorPath = configProperties.getAlist().getMonitorPath();
        boolean hasNewFile = false;
        List<String> newFilePath = new ArrayList<>();
        for (String path : monitorPath) {
            // 实际监听路径获取缓存文件
            Result<listFileRespDTO> listFileRespResult = alistClient.listFile(new ListFileReqDTO(path, false));
            List<listFileRespDTO.Content> listFile = listFileRespResult.getCheckedData().getContent();
            for (listFileRespDTO.Content file : listFile) {
                if (!file.isDir()) {
                    hasNewFile = true;
                    newFilePath.add(path + "/" + file.getName());
                }
            }
        }
        if (hasNewFile) {
            List<TtmReqDTO> ttmReqDTOList = new ArrayList<>();
            TtmReqDTO ttmReqDTO = new TtmReqDTO();
            ttmReqDTO.setAction(TtmAction.UPDATE)
                    .setScope(new TtmReqDTO.Scope(TtmScopeName.ALL, new ArrayList<>()));
            ttmReqDTOList.add(ttmReqDTO);
            ttmReqDTO = new TtmReqDTO();
            ttmReqDTO.setAction(TtmAction.SCRAPE)
                    .setScope(new TtmReqDTO.Scope(TtmScopeName.UN_SCRAPED, new ArrayList<>()));
            ttmReqDTOList.add(ttmReqDTO);
            log.info("刮削文件：{}", newFilePath);
            ttmClient.execute(ttmReqDTOList);
            try {
                TimeUnit.MINUTES.sleep(10);
            } catch (InterruptedException e) {
            }
            // 刮削完成后移动文件
            for (String folderPath : monitorPath) {
                Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(folderPath, false));
                Set<String> fileNames = CollectionUtil.convertSet(listFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);
                // 获取电视节目名称
                String tvShowName = FileUtil.mainName(FileUtil.getParent(folderPath, 1));
                String targetPath = configProperties.getAlist().getSerializedTvShow().get(tvShowName);
                CopyFileReqDTO copyFileReqDTO = new CopyFileReqDTO();
                copyFileReqDTO.setSrcDir(folderPath)
                        .setDstDir(targetPath)
                        .setNames(fileNames)
                ;
                log.info("复制文件：{}-[{}] -> {}", folderPath, fileNames, targetPath);
                alistClient.copyFile(copyFileReqDTO);
            }
        }
        log.info("结束处理alist文件监听");
    }
}
