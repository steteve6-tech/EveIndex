-- 删除 t_device_registration 表中的 risk_class 列
-- 执行前请先备份数据库！

-- 删除相关索引
DROP INDEX IF EXISTS idx_risk_class ON t_device_registration;

-- 删除列
ALTER TABLE t_device_registration DROP COLUMN risk_class;

-- 验证删除结果
DESCRIBE t_device_registration;
