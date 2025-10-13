-- 安全版本：检查字段是否存在再添加
-- 使用存储过程避免重复添加字段

DELIMITER $$

-- 创建存储过程：安全地添加remark字段
CREATE PROCEDURE AddRemarkColumnIfNotExists()
BEGIN
    -- 1. t_device_510k
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_device_510k' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_device_510k ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_device_510k 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_device_510k 表的 remark 字段已存在，跳过' AS result;
    END IF;

    -- 2. t_device_event
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_device_event' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_device_event ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_device_event 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_device_event 表的 remark 字段已存在，跳过' AS result;
    END IF;

    -- 3. t_device_recall
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_device_recall' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_device_recall ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_device_recall 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_device_recall 表的 remark 字段已存在，跳过' AS result;
    END IF;

    -- 4. t_device_registration
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_device_registration' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_device_registration ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_device_registration 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_device_registration 表的 remark 字段已存在，跳过' AS result;
    END IF;

    -- 5. t_guidance_document
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_guidance_document' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_guidance_document ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_guidance_document 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_guidance_document 表的 remark 字段已存在，跳过' AS result;
    END IF;

    -- 6. t_customs_case
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 't_customs_case' 
        AND COLUMN_NAME = 'remark'
    ) THEN
        ALTER TABLE t_customs_case ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';
        SELECT 't_customs_case 表添加 remark 字段成功' AS result;
    ELSE
        SELECT 't_customs_case 表的 remark 字段已存在，跳过' AS result;
    END IF;

END$$

DELIMITER ;

-- 调用存储过程
CALL AddRemarkColumnIfNotExists();

-- 删除存储过程
DROP PROCEDURE IF EXISTS AddRemarkColumnIfNotExists;

-- 验证结果
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND COLUMN_NAME = 'remark'
AND TABLE_NAME IN (
    't_device_510k',
    't_device_event',
    't_device_recall',
    't_device_registration',
    't_guidance_document',
    't_customs_case'
)
ORDER BY TABLE_NAME;

