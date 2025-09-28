-- 修改 t_device_registration 表中的 fei_number 字段类型
-- 从 LONGTEXT 修改为 VARCHAR(50)
-- 执行前请先备份数据库！

-- 修改字段类型
ALTER TABLE t_device_registration MODIFY COLUMN fei_number VARCHAR(50) NULL COMMENT '次要标识符（US: fei_number, EU: basic_udi_di）';

-- 验证修改结果
DESCRIBE t_device_registration;

-- 检查是否有数据超过50个字符
SELECT fei_number, LENGTH(fei_number) as length 
FROM t_device_registration 
WHERE fei_number IS NOT NULL 
AND LENGTH(fei_number) > 50;
