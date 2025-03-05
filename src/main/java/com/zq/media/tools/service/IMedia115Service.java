package com.zq.media.tools.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zq.media.tools.entity.Media115;

import java.util.List;

/**
 * 115媒体库service
 *
 * @author zhaoqiang
 * @since V1.0.0 2024-11-15
 */
public interface IMedia115Service extends IService<Media115> {

    /**
     * 获取通过文件id
     *
     * @param fileId 文件id
     * @return {@link Media115 }
     */
    Media115 getByFileId(String fileId);

    /**
     * 查询树列表通过文件id
     *
     * @param fileId 文件id
     * @return {@link List }<{@link Media115 }>
     */
    List<Media115> queryDescendantsByFileId(String fileId);

    /**
     * 查询列表通过SHA1和路径
     *
     * @param sha1 SHA1
     * @param path 路径
     * @return {@link Media115 }
     */
    Media115 getBySha1AndPath(String sha1, String path);
}
