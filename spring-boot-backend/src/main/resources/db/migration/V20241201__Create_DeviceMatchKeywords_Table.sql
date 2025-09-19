-- 创建设备匹配关键词表
CREATE TABLE t_devicematch_keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(255) NOT NULL COMMENT '关键词内容',
    keyword_type VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '关键词类型：normal-普通关键词, blacklist-黑名单关键词',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    UNIQUE KEY uk_keyword_type (keyword, keyword_type) COMMENT '关键词+类型唯一索引',
    INDEX idx_type_enabled (keyword_type, enabled) COMMENT '类型+启用状态索引'
) COMMENT '设备匹配关键词表';

-- 插入一些默认关键词数据
INSERT INTO t_devicematch_keywords (keyword, keyword_type, enabled) VALUES
('Skin Analysis', 'normal', true),
('Skin Scanner', 'normal', true),
('3D skin imaging system', 'normal', true),
('Facial Imaging', 'normal', true),
('Skin pigmentation analysis system', 'normal', true),
('skin elasticity analysis', 'normal', true),
('test', 'blacklist', true),
('demo', 'blacklist', true),
('sample', 'blacklist', true);
