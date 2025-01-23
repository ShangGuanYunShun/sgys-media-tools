package com.zq.media.tools.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.http.HttpUtil;
import com.zq.common.domain.Result;
import com.zq.common.util.ThreadUtil;
import com.zq.media.tools.dto.HandleFileDTO;
import com.zq.media.tools.dto.req.alist.CopyFileReqDTO;
import com.zq.media.tools.dto.req.alist.GetFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.req.alist.MoveFileReqDTO;
import com.zq.media.tools.dto.req.ttm.TtmReqDTO;
import com.zq.media.tools.dto.resp.alist.GetFileRespDTO;
import com.zq.media.tools.dto.resp.alist.TaskRespDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import com.zq.media.tools.entity.Media115;
import com.zq.media.tools.enums.TtmAction;
import com.zq.media.tools.enums.TtmScopeName;
import com.zq.media.tools.feign.AlistClient;
import com.zq.media.tools.feign.TtmClient;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.service.IAlistService;
import com.zq.media.tools.service.IMedia115Service;
import com.zq.media.tools.util.StrmUtil;
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
     * 刮削115
     *
     * @param handleFile 处理文件
     */
    @Override
    public void copyFileQuarkTo115(HandleFileDTO handleFile) {
        // 1、获取目标文件夹下已存在的文件列表
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(handleFile.getFolderPath(), false));
        Set<String> existingEpisodes = convertSet(listFileResult.getCheckedData().getContent(), listFileRespDTO.Content::getName);
        
        // 2、过滤出需要复制的新文件
        Set<String> newFiles = new HashSet<>();
        List<String> handleFiles = filterDuplicateEpisodes(handleFile.getFiles());
        for (String file : handleFiles) {
            boolean exists = anyMatch(existingEpisodes, 
                existingFile -> !existingFile.equals(file) && StrmUtil.areEpisodesEqual(existingFile, file));
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
        String scrapPath = configProperties.getAlist().getScrapPath().get(FileUtil.getName(handleFile.getFolderPath()));

        // 4、判断115网盘是否已经存在此文件了
        Result<listFileRespDTO> listFileResult115 = alistClient.listFile(new ListFileReqDTO(scrapPath, true));
        Set<String> existingFiles115 = convertSet(listFileResult115.getCheckedData().getContent(), listFileRespDTO.Content::getName);
        newFiles.removeIf(file -> {
            if (existingFiles115.contains(file)) {
                log.info("115网盘已存在此文件: {}", file);
                return true;
            }
            return false;
        });

        if (newFiles.isEmpty()) {
            log.info("115网盘已存在所有文件,无需复制，直接刮削: {}", scrapPath);
            // 直接触发刮削和移动文件
            processCompletedCopy(scrapPath);
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
        int taskId = ThreadUtil.executeCycle(
                () -> copyFileMonitor(copyTaskDone, taskIndex, scrapPath),
                5,
                ChronoUnit.MINUTES
        );
        taskIds.put(taskIndex, taskId);
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

    /**
     * 监控文件复制进度
     * 
     * @param copyTaskDone 复制任务完成标志
     * @param index 任务索引
     * @param scrapPath 刮削路径
     */
    private void copyFileMonitor(AtomicBoolean copyTaskDone, int index, String scrapPath) {
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
        ThreadUtil.execute(() -> processCompletedCopy(scrapPath));
        
        // 停止监听任务
        log.info("复制文件已完成，停止监听任务：{}-{}", taskIds.get(index), scrapPath);
        ThreadUtil.stop(taskIds.get(index));
    }
    
    /**
     * 处理复制完成后的任务
     * 包括刷新目录、刮削文件和移动文件等操作
     *
     * @param scrapPath 刮削路径
     */
    private void processCompletedCopy(String scrapPath) {
        // 刷新cd2目录
        refreshCD2Directory(scrapPath);
        
        // 刮削文件
        log.info("刮削文件：{}", scrapPath);
        scrap();
        
        // 移动文件到目标目录
        moveFilesToTarget(scrapPath);
    }
    
    /**
     * 刷新CD2目录
     * 通过执行Python脚本来刷新目录内容
     *
     * @param scrapPath 需要刷新的目录路径
     */
    private void refreshCD2Directory(String scrapPath) {
        String[] script = {"python3", "/app/python/clouddrive_api.py",
                configProperties.getCloudDrive().getUrl(), 
                configProperties.getCloudDrive().getUsername(), 
                configProperties.getCloudDrive().getPassword(),
                "list_files", scrapPath};
                
        log.info("执行python脚本：{}", Arrays.asList(script));
        String execResult = RuntimeUtil.execForStr(script);
        log.info("python执行结果：{}", execResult);
        
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException ignored) {
        }
    }
    
    /**
     * 将文件移动到目标目录
     * 获取文件列表并移动到对应的电视剧目录
     *
     * @param scrapPath 源文件路径
     */
    private void moveFilesToTarget(String scrapPath) {
        Result<listFileRespDTO> listFileResult = alistClient.listFile(new ListFileReqDTO(scrapPath, true));
        Set<String> fileNames = convertSet(listFileResult.getCheckedData().getContent(),
                content -> !"season.nfo".equals(content.getName()),
                listFileRespDTO.Content::getName);
                
        // 获取电视节目名称和目标路径
        String tvShowName = FileUtil.mainName(FileUtil.getParent(scrapPath, 1));
        String targetPath = configProperties.getAlist().getSerializedTvShow().get(tvShowName);
        
        // 构建移动请求
        MoveFileReqDTO moveFileReqDTO = new MoveFileReqDTO()
                .setSrcDir(scrapPath)
                .setDstDir(targetPath)
                .setNames(fileNames);
                
        log.info("移动文件：{}-{} -> {}", scrapPath, fileNames, targetPath);
        alistClient.moveFile(moveFileReqDTO);
        log.info("新增剧集处理完成：{}-{}", tvShowName, StrmUtil.getEpisodes(CollUtil.getFirst(fileNames)));
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