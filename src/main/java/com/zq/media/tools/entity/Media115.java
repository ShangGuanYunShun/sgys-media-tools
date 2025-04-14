package com.zq.media.tools.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 115媒体库
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-15 11:41
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("MEDIA_115")
public class Media115 {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 父文件id
     */
    private String parentId;

    /**
     * 路径（包含文件名）
     */
    private String path;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * SHA1
     */
    private String sha1;

    /**
     * 文件扩展名
     */
    private String ext;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
