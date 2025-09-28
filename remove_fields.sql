-- 删除数据库表中的指定字段
-- 执行前请先备份数据库！

-- 1. 删除 d_510k_records 表中的字段
ALTER TABLE d_510k_records DROP COLUMN decision_date;
ALTER TABLE d_510k_records DROP COLUMN device_url;

-- 2. 删除 t_device_510k 表中的字段
ALTER TABLE t_device_510k DROP COLUMN device_url;
ALTER TABLE t_device_510k DROP COLUMN decision_date;
ALTER TABLE t_device_510k DROP COLUMN product_code;
ALTER TABLE t_device_510k DROP COLUMN regulation_number;
ALTER TABLE t_device_510k DROP COLUMN openfda;

-- 3. 删除相关索引（如果存在）
DROP INDEX IF EXISTS idx_decision_date ON d_510k_records;

-- 验证删除结果
DESCRIBE d_510k_records;
DESCRIBE t_device_510k;
