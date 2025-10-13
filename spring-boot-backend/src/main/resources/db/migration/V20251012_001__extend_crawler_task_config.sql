-- 扩展爬虫任务配置表，添加新字段
-- 用于支持关键词批量爬取、任务类型、优先级等功能

-- 添加关键词列表字段（忽略已存在的错误）
ALTER TABLE t_scheduled_crawler_config 
ADD COLUMN keywords TEXT COMMENT '关键词列表（JSON数组格式）';

-- 添加任务类型字段
ALTER TABLE t_scheduled_crawler_config 
ADD COLUMN task_type VARCHAR(20) COMMENT '任务类型：KEYWORD_BATCH/DATE_RANGE/FULL';

-- 添加执行优先级字段
ALTER TABLE t_scheduled_crawler_config 
ADD COLUMN priority INT DEFAULT 5 COMMENT '执行优先级（1-10，数字越大优先级越高）';

-- 添加超时时间字段
ALTER TABLE t_scheduled_crawler_config 
ADD COLUMN timeout_minutes INT DEFAULT 60 COMMENT '超时时间（分钟）';

-- 添加重试次数字段
ALTER TABLE t_scheduled_crawler_config 
ADD COLUMN retry_count INT DEFAULT 3 COMMENT '失败重试次数';

-- 添加索引以提升查询性能（忽略已存在的错误）
CREATE INDEX idx_country_code ON t_scheduled_crawler_config(country_code);
CREATE INDEX idx_enabled ON t_scheduled_crawler_config(enabled);
CREATE INDEX idx_task_type ON t_scheduled_crawler_config(task_type);
CREATE INDEX idx_priority ON t_scheduled_crawler_config(priority);

