-- 为 t_device_event 表添加爬取时间字段
-- 创建时间: 2025-10-09
-- 说明: 记录数据爬取的时间戳，便于跟踪数据更新

-- 检查 crawl_time 字段是否已存在
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'common_db'
    AND TABLE_NAME = 't_device_event'
    AND COLUMN_NAME = 'crawl_time'
);

-- 如果不存在，则添加
SET @sql_add = IF(
    @column_exists = 0,
    'ALTER TABLE common_db.t_device_event ADD COLUMN crawl_time DATETIME COMMENT ''爬取时间（数据抓取时的时间戳）''',
    'SELECT "crawl_time字段已存在，跳过添加" AS message'
);

PREPARE stmt FROM @sql_add;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为现有数据设置默认值（使用创建时间）
UPDATE common_db.t_device_event
SET crawl_time = create_time
WHERE crawl_time IS NULL AND create_time IS NOT NULL;

-- 对于没有创建时间的记录，使用当前时间
UPDATE common_db.t_device_event
SET crawl_time = NOW()
WHERE crawl_time IS NULL;

-- 检查索引是否已存在
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'common_db'
    AND TABLE_NAME = 't_device_event'
    AND INDEX_NAME = 'idx_device_event_crawl_time'
);

-- 如果不存在，则创建索引
SET @sql_index = IF(
    @index_exists = 0,
    'CREATE INDEX idx_device_event_crawl_time ON common_db.t_device_event(crawl_time)',
    'SELECT "索引已存在，跳过创建" AS message'
);

PREPARE stmt FROM @sql_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建组合索引（国家+爬取时间）
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'common_db'
    AND TABLE_NAME = 't_device_event'
    AND INDEX_NAME = 'idx_device_event_country_crawl_time'
);

SET @sql_index = IF(
    @index_exists = 0,
    'CREATE INDEX idx_device_event_country_crawl_time ON common_db.t_device_event(jd_country, crawl_time)',
    'SELECT "组合索引已存在，跳过创建" AS message'
);

PREPARE stmt FROM @sql_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

