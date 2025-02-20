package com.zq.media.tools.controller;

import com.zq.common.domain.Result;
import com.zq.media.tools.service.IAlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * alist管理
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-2-20 11:23
 */
@Tag(name = "alist管理")
@RestController
@RequestMapping("/alist")
@RequiredArgsConstructor
public class AlistController {

    private final IAlistService alistService;

    @Operation(summary = "创建strm文件")
    @PostMapping("/createStrm")
    public Result createStrm(@RequestBody List<String> mediaPath) {
        mediaPath.forEach(alistService::processDic);
        return Result.success();
    }
}
