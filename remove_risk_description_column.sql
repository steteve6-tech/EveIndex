-- 删除 t_crawler_data 表中的 risk_description 列
-- 执行前请先备份数据库！

-- 删除列
ALTER TABLE t_crawler_data DROP COLUMN risk_description;

-- 验证删除结果
DESCRIBE t_crawler_data;
