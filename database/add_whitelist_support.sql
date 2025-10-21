-- ==========================================
-- 为 t_devicematch_keywords 表添加 whitelist 类型支持
-- ==========================================

-- 1. 查看当前表结构
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'common_db' 
  AND TABLE_NAME = 't_devicematch_keywords'
  AND COLUMN_NAME = 'keyword_type';

-- 2. 修改 keyword_type 列的注释，明确包含 whitelist 类型
ALTER TABLE common_db.t_devicematch_keywords 
MODIFY COLUMN keyword_type VARCHAR(20) NOT NULL DEFAULT 'normal' 
COMMENT '关键词类型：normal-普通关键词, blacklist-黑名单关键词, whitelist-白名单关键词';

-- 3. 测试插入一条 whitelist 类型的数据
INSERT INTO common_db.t_devicematch_keywords (keyword, keyword_type, enabled, created_time, updated_time) 
VALUES ('test_whitelist_keyword', 'whitelist', true, NOW(), NOW());

-- 4. 验证插入是否成功
SELECT * FROM common_db.t_devicematch_keywords WHERE keyword_type = 'whitelist';

-- 5. 查看所有类型的数据统计
SELECT keyword_type, COUNT(*) as count 
FROM common_db.t_devicematch_keywords 
GROUP BY keyword_type;

-- 6. 如果测试成功，删除测试数据
DELETE FROM common_db.t_devicematch_keywords WHERE keyword = 'test_whitelist_keyword';

-- ==========================================
-- 执行说明：
-- 1. 连接到您的 MySQL 数据库
-- 2. 逐条执行上述 SQL 语句
-- 3. 确认步骤 3 和步骤 4 执行成功（能插入和查询到 whitelist 类型数据）
-- 4. 执行步骤 6 清理测试数据
-- 5. 完成后，刷新浏览器页面重新测试添加白名单功能
-- ==========================================

