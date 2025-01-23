package com.zq.media.tools.controller;

import com.zq.common.domain.Result;
import com.zq.media.tools.driver.Driver115;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 115网盘管理
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2025-1-21 10:19
 */
@Tag(name = "115网盘管理")
@RestController
@RequestMapping("/driver/115")
@RequiredArgsConstructor
@Validated
public class Driver115Controller {

    private final Driver115 driver115;

    @Operation(summary = "处理115网盘的生活动作")
    @GetMapping("/behavior/handle")
    public Result handleBehavior(@NotNull(message = "开始时间不能为空") LocalDateTime beginTime, LocalDateTime endTime) {
        driver115.handleBehavior(beginTime, endTime);
        return Result.success();
    }
}
