package com.zq.media.tools.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zq.common.util.ThreadUtil;
import com.zq.media.tools.dto.HandleFileDTO;
import com.zq.media.tools.properties.ConfigProperties;
import com.zq.media.tools.service.IAlistService;
import com.zq.media.tools.service.IReceiveNotificationService;
import com.zq.media.tools.util.StrmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    private final ConfigProperties configProperties;

    @Override
    public void receiveQuarkAutoSave(String content) {
        ThreadUtil.execute(() -> {
            List<String> split = StrUtil.split(content, "\\n");
            List<HandleFileDTO> list = new ArrayList<>();

            // 初始化文件 DTO
            HandleFileDTO handleFileDTO = createHandleFileDTO();
            boolean isFolderPathSet = false;

            for (String str : split) {
                str = str.trim(); // 去除前后空格
                log.debug(str);
                if (configProperties.getDriverQuark().getHandleFolders().stream().anyMatch(str::contains) ) {
                    // 设置文件夹路径
                    handleFileDTO.setFolderPath("/夸克网盘" + str);
                    isFolderPathSet = true;
                } else if (str.isEmpty() || "\"".equals(str)) {
                    // 处理结束，保存当前 DTO
                    if (!handleFileDTO.getFiles().isEmpty() && handleFileDTO.getFolderPath() != null) {
                        // 判断当前文件夹是否已经在list中存在
                        boolean isExist = false;
                        for (HandleFileDTO fileDTO : list) {
                            if (fileDTO.getFolderPath().equals(handleFileDTO.getFolderPath())) {
                                fileDTO.setIsSingleTask(false);
                                isExist = true;
                            }
                        }
                        if (!isExist) {
                            list.add(handleFileDTO);
                        }
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

            // 调用服务方法处理
            list.forEach(alistService::copyFileQuarkTo115);
        });
    }

    private HandleFileDTO createHandleFileDTO() {
        HandleFileDTO dto = new HandleFileDTO();
        dto.setFiles(new HashSet<>());
        dto.setIsSingleTask(true);
        return dto;
    }
}
