-- 删除 t_device_pma 表及其相关索引和约束
-- 执行前请先备份数据库！

-- 删除相关索引
DROP INDEX IF EXISTS idx_pma_number ON t_device_pma;
DROP INDEX IF EXISTS idx_product_code ON t_device_pma;
DROP INDEX IF EXISTS idx_decision_date ON t_device_pma;
DROP INDEX IF EXISTS idx_date_received ON t_device_pma;

-- 删除唯一约束
ALTER TABLE t_device_pma DROP CONSTRAINT IF EXISTS uk_pma_supplement;

-- 删除表
DROP TABLE IF EXISTS t_device_pma;

-- 验证删除结果
SHOW TABLES LIKE 't_device_pma';
