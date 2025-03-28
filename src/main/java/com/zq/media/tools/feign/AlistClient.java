package com.zq.media.tools.feign;

import com.zq.common.domain.Result;
import com.zq.media.tools.config.FeignAlistConfig;
import com.zq.media.tools.dto.req.alist.CopyFileReqDTO;
import com.zq.media.tools.dto.req.alist.DeleteFileReqDTO;
import com.zq.media.tools.dto.req.alist.GetFileReqDTO;
import com.zq.media.tools.dto.req.alist.ListFileReqDTO;
import com.zq.media.tools.dto.req.alist.MoveFileReqDTO;
import com.zq.media.tools.dto.req.alist.RenameFileReqDTO;
import com.zq.media.tools.dto.resp.alist.GetFileRespDTO;
import com.zq.media.tools.dto.resp.alist.TaskRespDTO;
import com.zq.media.tools.dto.resp.alist.listFileRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * alist api
 *
 * @author zhaoqiang
 * @since V1.0.0 2024-12-23
 */
@FeignClient(name = "alist", url = "${app.alist.url}", configuration = FeignAlistConfig.class)
public interface AlistClient {

    // region 文件目录相关
    /**
     * 列出文件目录
     *
     * @param listFileReqDTO 列出文件目录req
     * @return {@link Result }<{@link listFileRespDTO }>
     */
    @PostMapping("/api/fs/list")
    Result<listFileRespDTO> listFile(@RequestBody ListFileReqDTO listFileReqDTO);

    /**
     * 获取文件信息
     *
     * @param getFileReqDTO 获取文件请求 DTO
     * @return {@link Result }<{@link GetFileRespDTO }>
     */
    @PostMapping("/api/fs/get")
    Result<GetFileRespDTO> getFileInfo(@RequestBody GetFileReqDTO getFileReqDTO);

    /**
     * 移动文件
     *
     * @param moveFileReqDTO 移动文件req dto
     * @return {@link Result }
     */
    @PostMapping("/api/fs/move")
    Result moveFile(@RequestBody MoveFileReqDTO moveFileReqDTO);

    /**
     *
     * @return {@link Result }
     */
    @PostMapping("/api/fs/copy")
    Result copyFile(@RequestBody CopyFileReqDTO copyFileReqDTO);

    /**
     * 删除文件
     *
     * @param deleteFileReqDTO 删除文件req
     * @return {@link Result }
     */
    @PostMapping("/api/fs/remove")
    Result deleteFile(DeleteFileReqDTO deleteFileReqDTO);

    /**
     * 批量重命名文件
     *
     * @param renameFileReqDTO 将 file req dto 重命名为
     * @return {@link Result }
     */
    @PostMapping("/api/fs/batch_rename")
    Result renameFile(RenameFileReqDTO renameFileReqDTO);
    // endregion

    // region 任务相关
    /**
     * 查询已完成复制任务
     *
     * @return {@link Result }<{@link List }<{@link TaskRespDTO }>>
     */
    @GetMapping("/api/admin/task/copy/done")
    Result<List<TaskRespDTO>> listCopyDoneTask();

    /**
     * 查询未完成复制任务
     *
     * @return {@link Result }<{@link List }<{@link TaskRespDTO }>>
     */
    @GetMapping("/api/admin/task/copy/undone")
    Result<List<TaskRespDTO>> listCopyUndoneTask();

    /**
     * 清除已成功复制任务
     *
     * @return {@link Result }
     */
    @PostMapping("/api/admin/task/copy/clear_succeeded")
    Result clearCopySuccessTask();
    // endregion

}