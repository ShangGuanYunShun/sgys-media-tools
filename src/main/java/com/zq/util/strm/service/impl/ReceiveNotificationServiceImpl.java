package com.zq.util.strm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zq.common.util.ThreadUtil;
import com.zq.util.strm.dto.HandleFileDTO;
import com.zq.util.strm.service.IAlistService;
import com.zq.util.strm.service.IReceiveNotificationService;
import com.zq.util.strm.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaoqiang
 * @version 1.0
 * @date 2025/1/8 22:21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveNotificationServiceImpl implements IReceiveNotificationService {

    private final IAlistService alistService;

    @Override
    public void receiveQuarkAutoSave(String content) {
        log.info("收到来自夸克的自动转存通知：{}", content);
        ThreadUtil.execute(() -> {
            List<String> split = StrUtil.split(content, "\\n");
//            Set<String> handlePath = new HashSet<>();
            List<HandleFileDTO> list = new ArrayList<>();

            // 初始化文件 DTO
            HandleFileDTO handleFileDTO = createHandleFileDTO();
            boolean isFolderPathSet = false;

            for (String str : split) {
                str = str.trim(); // 去除前后空格
                System.out.println(str);

                if (str.contains("/接收连载动漫") || str.contains("/接收连载电视")) {
                    // 设置文件夹路径
                    handleFileDTO.setFolderPath("/夸克网盘" + str);
//                    handlePath.add("/夸克网盘" + str);
                    isFolderPathSet = true;
                } else if (str.isEmpty() || "\"".equals(str)) {
                    // 处理结束，保存当前 DTO
                    if (!handleFileDTO.getFiles().isEmpty() || handleFileDTO.getFolderPath() != null) {
                        list.add(handleFileDTO);
                    }
                    handleFileDTO = createHandleFileDTO(); // 重置 DTO
                    isFolderPathSet = false;
                } else if (isFolderPathSet && StrmUtil.isEpisodes(str)) {
                    // 处理文件路径
                    String cleanedPath = str.substring(str.indexOf("──") + 2) // 去掉前缀符号
                            .replaceAll("[\\p{Cf}\\uFEFF🎞️]", "") // 清理特殊字符
                            .trim();
                    handleFileDTO.getFiles().add(cleanedPath);
                }
            }

            // 添加最后一个 DTO
            if (!handleFileDTO.getFiles().isEmpty() || handleFileDTO.getFolderPath() != null) {
                list.add(handleFileDTO);
            }

            // 延时处理
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复线程中断状态
            }

            // 调用服务方法处理
            list.forEach(alistService::copyFileQuarkTo115);
        });
    }

    private HandleFileDTO createHandleFileDTO() {
        HandleFileDTO dto = new HandleFileDTO();
        dto.setFiles(new HashSet<>());
        return dto;
    }
}
