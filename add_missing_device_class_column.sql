-- 添加缺失的 device_class 列到 t_device_registration 表
-- 执行前请先备份数据库！

-- 检查列是否存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 't_device_registration' 
    AND COLUMN_NAME = 'device_class'
);

-- 如果列不存在，则添加列
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE t_device_registration ADD COLUMN device_class LONGTEXT NULL COMMENT ''设备类别''', 
    'SELECT ''Column device_class already exists'' as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证列是否存在
DESCRIBE t_device_registration;

-- 或者使用这个简单的ALTER语句（如果确定列不存在）
-- ALTER TABLE t_device_registration ADD COLUMN device_class LONGTEXT NULL COMMENT '设备类别';
