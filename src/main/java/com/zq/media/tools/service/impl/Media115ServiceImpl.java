package com.zq.media.tools.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zq.media.tools.entity.Media115;
import com.zq.media.tools.mapper.Media115Mapper;
import com.zq.media.tools.service.IMedia115Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 115媒体库service实现
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 11:55
 */
@Service
@RequiredArgsConstructor
public class Media115ServiceImpl extends ServiceImpl<Media115Mapper, Media115> implements IMedia115Service {

    private final Media115Mapper media115Mapper;

    /**
     * 获取通过文件id
     *
     * @param fileId 文件id
     * @return {@link Media115 }
     */
    @Override
    public Media115 getByFileId(String fileId) {
        return media115Mapper.selectOne(Media115::getFileId, fileId);
    }

    /**
     * 查询树列表通过文件id
     *
     * @param fileId 文件id
     * @return {@link List }<{@link Media115 }>
     */
    @Override
    public List<Media115> queryDescendantsByFileId(String fileId) {
        List<Media115> descendants = new ArrayList<>();
        findDescendants(fileId, descendants);
        return descendants;
    }

    /**
     * 查询列表通过SHA1
     *
     * @param sha1 SHA1
     * @param name 名字
     * @return {@link Media115 }
     */
    @Override
    public Media115 getBySha1(String sha1, String name) {
        return media115Mapper.selectOne(Wrappers.lambdaQuery(Media115.class)
                .eq(Media115::getSha1, sha1)
                .eq(Media115::getFileName, name));
    }

    private void findDescendants(String fileId, List<Media115> descendants) {
        List<Media115> children = media115Mapper.selectList(Media115::getParentId, fileId);
        if (!children.isEmpty()) {
            descendants.addAll(children);
            for (Media115 child : children) {
                findDescendants(child.getFileId(), descendants);
            }
        }
    }
}
