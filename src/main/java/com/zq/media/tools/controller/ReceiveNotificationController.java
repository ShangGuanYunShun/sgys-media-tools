package com.zq.media.tools.controller;

import com.zq.common.domain.Result;
import com.zq.media.tools.params.EmbyNotifyParam;
import com.zq.media.tools.service.IReceiveNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @Operation(summary = "接收来自emby的神医通知", tags = "emby通知")
    @PostMapping("/emby/shenyi")
    public Result embyFromShenYi(@RequestBody EmbyNotifyParam embyNotifyParam) {
        receiveNotificationService.receiveEmbyFromShenYi(embyNotifyParam);
        return Result.success();
    }

    @Operation(summary = "接收来自emby的用户操作通知", tags = "emby通知")
    @PostMapping("/emby/user")
    public Result embyFromUser(@RequestBody EmbyNotifyParam embyNotifyParam) {
//        String content = (String) request.get("body");
//        receiveNotificationService.receiveQuarkAutoSave(content);
        return Result.success();
    }

}
