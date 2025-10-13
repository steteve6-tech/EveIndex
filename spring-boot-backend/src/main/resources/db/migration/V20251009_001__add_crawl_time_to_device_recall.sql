-- 为 t_device_recall 表添加爬取时间字段
-- 创建时间: 2025-10-09
-- 说明: 记录数据爬取的时间戳，便于跟踪数据更新

-- 添加 crawl_time 列
ALTER TABLE common_db.t_device_recall
ADD COLUMN crawl_time DATETIME COMMENT '爬取时间（数据抓取时的时间戳）';

-- 为现有数据设置默认值（使用当前时间）
UPDATE common_db.t_device_recall
SET crawl_time = NOW()
WHERE crawl_time IS NULL;

-- 添加索引（便于按爬取时间查询）
CREATE INDEX idx_crawl_time ON common_db.t_device_recall(crawl_time);

-- 添加组合索引（国家+爬取时间）
CREATE INDEX idx_country_crawl_time ON common_db.t_device_recall(jd_country, crawl_time);

