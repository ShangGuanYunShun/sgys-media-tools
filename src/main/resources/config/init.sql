-- 创建 media_115 表
CREATE TABLE IF NOT EXISTS MEDIA_115
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_id     VARCHAR(64) COMMENT '文件ID',
    parent_id   VARCHAR(64) COMMENT '父文件ID',
    path        VARCHAR(2000) NOT NULL COMMENT '路径',
    file_name   VARCHAR(500)  NOT NULL COMMENT '文件名',
    pick_code   VARCHAR(64) COMMENT '选取代码',
    sha1        CHAR(40) COMMENT 'sha1',
    ext         VARCHAR(32) COMMENT '文件扩展名',
    create_time TIMESTAMP     NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间'
);

-- 为表添加注释
COMMENT ON TABLE MEDIA_115 IS '媒体文件表';
