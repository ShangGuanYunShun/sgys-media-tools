package com.zq.util.strm.controller;

import cn.hutool.core.util.StrUtil;
import com.zq.common.domain.Result;
import com.zq.common.util.ThreadUtil;
import com.zq.util.strm.dto.HandleFileDTO;
import com.zq.util.strm.service.IAlistService;
import com.zq.util.strm.service.IReceiveNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/1 0:11
 */
@Slf4j
@Tag(name = "接收通知")
@RestController
@RequestMapping("/receive/notification")
@RequiredArgsConstructor
public class ReceiveNotificationController {

    private final IReceiveNotificationService receiveNotificationService;

    @Operation(summary = "接收来自夸克的自动转存通知")
    @PostMapping("/quark/auto-save")
    public Result quarkAutoSave(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("body");
        receiveNotificationService.receiveQuarkAutoSave(content);
        return Result.success();
    }

}
