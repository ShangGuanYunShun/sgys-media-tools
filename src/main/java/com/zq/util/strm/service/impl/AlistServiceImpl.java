package com.zq.util.strm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.http.HttpUtil;
import com.zq.common.domain.Result;
import com.zq.common.util.CollectionUtil;
import com.zq.common.util.ThreadUtil;
import com.zq.util.strm.dto.HandleFileDTO;
import com.zq.util.strm.dto.req.alist.CopyFileReqDTO;
import com.zq.util.strm.dto.req.alist.GetFileReqDTO;
import com.zq.util.strm.dto.req.alist.ListFileReqDTO;
import com.zq.util.strm.dto.req.alist.MoveFileReqDTO;
import com.zq.util.strm.dto.req.ttm.TtmReqDTO;
import com.zq.util.strm.dto.resp.alist.GetFileRespDTO;
import com.zq.util.strm.dto.resp.alist.TaskRespDTO;
import com.zq.util.strm.dto.resp.alist.listFileRespDTO;
import com.zq.util.strm.entity.Media115;
import com.zq.util.strm.enums.TtmAction;
import com.zq.util.strm.enums.TtmScopeName;
import com.zq.util.strm.feign.AlistClient;
import com.zq.util.strm.feign.TtmClient;
import com.zq.util.strm.properties.ConfigProperties;
import com.zq.util.strm.service.IAlistService;
import com.zq.util.strm.service.IMedia115Service;
import com.zq.util.strm.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

import static cn.hutool.core.date.DatePattern.UTC_MS_WITH_XXX_OFFSET_PATTERN;
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
    private final IMedia115Service media115Service;

    private final Map<Integer, Integer> taskIds = new HashMap<>();
    AtomicInteger taskIndex = new AtomicInteger(0);

    /**
     * 将文件夹文件从夸克复制到115
     * 刮削夸克
     *
     * @param folderPath 文件夹路径
     */
    @Override
    public void copyFileQuarkTo115(String folderPath) {
        List<String> newFilePath = new ArrayList<>();
        // 获取最新的文件
        Result<listFileRespDTO> listFileRespResult = alistClient.listFile(new ListFileReqDTO(folderPath, true));
        List<listFileRespDTO.Content> listFile = listFileRespResult.getCheckedData().getContent();
        for (listFileRespDTO.Content file : listFile) {
            if (!file.isDir()) {
                newFilePath.add(folderPath + "/" + file.getName());
            }
        }
        LocalDateTime startTime = LocalDateTime.now();
        log.info("刮削文件：{}", newFilePath);
        // 刮削文件
        scrap();
        // 刮削完成后移动文件
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(folderPath, true));
        Set<String> fileNames = convertSet(listFileResult.getCheckedData().getContent(), content -> {
            // 只保留修改时间大于开始时间之后的文件
            return LocalDateTimeUtil.parse(content.getModified(), UTC_MS_WITH_XXX_OFFSET_PATTERN).isAfter(startTime);
        }, listFileRespDTO.Content::getName);
        // 获取电视节目名称
        String tvShowName = FileUtil.mainName(FileUtil.getParent(folderPath, 1));
        String targetPath = configProperties.getAlist().getSerializedTvShow().get(tvShowName);
        CopyFileReqDTO copyFileReqDTO = new CopyFileReqDTO();
        copyFileReqDTO.setSrcDir(folderPath)
                .setDstDir(targetPath)
                .setNames(fileNames)
        ;
        log.info("跨盘复制文件：{}-{} -> {}", folderPath, fileNames, targetPath);
        alistClient.copyFile(copyFileReqDTO);
        AtomicBoolean copyTaskDone = new AtomicBoolean(false);
        Set<String> files = convertSet(fileNames, fileName -> folderPath + "/" + fileName);
        int index = taskIndex.addAndGet(1);
        // 监控复制任务完成
        int taskId = ThreadUtil.executeCycle(() -> copyFileMonitor(files, copyTaskDone, index, targetPath), 5, ChronoUnit.MINUTES);
        taskIds.put(index, taskId);
    }

    /**
     * 将文件夹文件从夸克复制到115
     * 刮削115
     *
     * @param handleFile 处理文件
     */
    @Override
    public void copyFileQuarkTo115(HandleFileDTO handleFile) {
        Set<String> files = new HashSet<>();
        // 1、先判断是否已经存在当前剧集
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(handleFile.getFolderPath(), false));
        // 已存在的剧集信息
        Set<String> existingEpisodes = convertSet(listFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);
        // 过滤重复的剧集
        List<String> handleFiles = filterDuplicateEpisodes(handleFile.getFiles());
        // 遍历最新保存的剧集，检查是否已经存在
        for (String file : handleFiles) {
            boolean isEpisodeExists = anyMatch(existingEpisodes, existingFile -> !existingFile.equals(file) && StrmUtil.areEpisodesEqual(existingFile, file));
            if (isEpisodeExists) {
                log.info("当前剧集已存在：{}\n{}", handleFile.getFolderPath(), file);
            } else {
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            log.info("当前剧集已全部存在,退出执行：{}\n{}", handleFile.getFolderPath(), files);
            return;
        }
        // 2、刷新文件
        alistClient.listFile(new ListFileReqDTO(handleFile.getFolderPath(), true));
        CopyFileReqDTO quarkCopyFileReqDTO = new CopyFileReqDTO();
        String scrapPath = configProperties.getAlist().getScrapPath().get(FileUtil.getName(handleFile.getFolderPath()));
        quarkCopyFileReqDTO.setSrcDir(handleFile.getFolderPath())
                .setDstDir(scrapPath)
                .setNames(files)
        ;
        // 3、跨盘复制文件
        alistClient.copyFile(quarkCopyFileReqDTO);
        log.info("跨盘复制文件：{}-{} -> {}", handleFile.getFolderPath(), files, scrapPath);
        AtomicBoolean copyTaskDone = new AtomicBoolean(false);
        int index = taskIndex.addAndGet(1);
        // 4、监控复制任务是否完成
        int taskId = ThreadUtil.executeCycle(() -> copyFileMonitor(copyTaskDone, index, scrapPath), 5, ChronoUnit.MINUTES);
        taskIds.put(index, taskId);
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
                .setScope(new TtmReqDTO.Scope(TtmScopeName.UN_SCRAPED, new ArrayList<>()));
        ttmReqDTOList.add(ttmReqDTO);
        ttmClient.execute(ttmReqDTOList);
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException ignored) {
        }
        // 第一次接口通常没反应，需要再调用一次
        ttmClient.execute(ttmReqDTOList);
        try {
            // 等待刮削完成
            TimeUnit.MINUTES.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 复制文件监视器
     *
     * @param files        监听文件
     * @param copyTaskDone 复制任务已完成
     * @param targetPath   目标路径
     */
    private void copyFileMonitor(Set<String> files, AtomicBoolean copyTaskDone, int index, String targetPath) {
        // 获取复制任务列表
        Result<List<TaskRespDTO>> copyUndoneTaskListResult = alistClient.listCopyUndoneTask();
        log.debug("复制文件监视器：{}", copyUndoneTaskListResult.getCheckedData());
        if (copyUndoneTaskListResult.getCheckedData().isEmpty()) {
            copyTaskDone.set(true);
        }
        if (copyTaskDone.get()) {
            // 删除夸克文件
//            alistClient.deleteFile(new DeleteFileReqDTO(files));
//            log.info("跨盘复制文件已完成，删除夸克源文件：{}", files);
            // 刷新一下
            alistClient.listFile(new ListFileReqDTO(targetPath, true));
            // 生成strm 文件和下载文件
            for (String file : files) {
                String path = targetPath + "/" + FileUtil.getName(file);
                downloadFileAndCreateStrm(Paths.get(path));
                Result<GetFileRespDTO> fileInfoResult = alistClient.getFileInfo(new GetFileReqDTO(path, true));
                // 保存文件信息，方便后面删除或者修改文件名时查询源文件信息
                Media115 media115 = new Media115();
                media115.setPath(path)
                        .setFileName(FileUtil.getName(path))
                        .setSha1(fileInfoResult.getCheckedData().getHashInfo().getSha1())
                        .setExt(FileUtil.extName(path));
                media115Service.save(media115);
            }
            log.info("跨盘复制文件已完成，停止监听任务：{}-{}", taskIds.get(index), files);
            ThreadUtil.stop(taskIds.get(index));
        }
    }

    private void copyFileMonitor(AtomicBoolean copyTaskDone, int index, String scrapPath) {
        // 获取复制任务列表
        Result<List<TaskRespDTO>> copyUndoneTaskListResult = alistClient.listCopyUndoneTask();
        log.debug("复制文件监视器：{}", copyUndoneTaskListResult.getCheckedData());
        if (copyUndoneTaskListResult.getCheckedData().isEmpty()) {
            copyTaskDone.set(true);
        }
        if (copyTaskDone.get()) {
            ThreadUtil.execute(() -> {
                // 5、刷新一下cd2的目录
                String[] script = {"python3", "/app/python/clouddrive_api.py",
                        configProperties.getCloudDrive().getUrl(), configProperties.getCloudDrive().getUsername(), configProperties.getCloudDrive().getPassword(),
                        "list_files", scrapPath};
                log.info("执行python脚本：{}", Arrays.asList(script));
                String execResult = RuntimeUtil.execForStr(script);
                log.info("python执行结果：{}", execResult);
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException ignored) {
                }
                log.info("刮削文件：{}", scrapPath);
                // 6、刮削文件
                scrap();
                // 7、移动文件
                Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(scrapPath, true));
                Set<String> fileNames = convertSet(listFileResult.getCheckedData().getContent(),
                        content -> !"season.nfo".equals(content.getName()),
                        listFileRespDTO.Content::getName);
                // 获取电视节目名称
                String tvShowName = FileUtil.mainName(FileUtil.getParent(scrapPath, 1));
                String targetPath = configProperties.getAlist().getSerializedTvShow().get(tvShowName);
                MoveFileReqDTO moveFileReqDTO = new MoveFileReqDTO();
                moveFileReqDTO.setSrcDir(scrapPath)
                        .setDstDir(targetPath)
                        .setNames(fileNames)
                ;
                log.info("移动文件：{}-{} -> {}", scrapPath, fileNames, targetPath);
                alistClient.moveFile(moveFileReqDTO);
                log.info("新增剧集处理完成：{}-{}", tvShowName, StrmUtil.getEpisodes(CollUtil.getFirst(fileNames)));
            });
            log.info("跨盘复制文件已完成，停止监听任务：{}-{}", taskIds.get(index), scrapPath);
            ThreadUtil.stop(taskIds.get(index));
        }
    }

    /**
     * 下载文件并创建strm文件
     *
     * @param path 路径
     */
    private void downloadFileAndCreateStrm(Path path) {
        path = Paths.get(path.toString().substring(6));
        if (StrmUtil.isVideoFile(path)) {
            String strmPath = StrmUtil.generateStrmFiles(path);
            log.info("生成strm文件: {}", strmPath);
        } else {
            Path fullPath = Paths.get(configProperties.getServer().getBasePath(), path.toString());
            HttpUtil.downloadFile(configProperties.getAlist().getMediaUrl() + path, fullPath.toFile());
            log.info("下载文件：{}", path);
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