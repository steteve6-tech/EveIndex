-- 添加数据来源URL字段到标准表
-- 迁移脚本：添加source_url字段到t_standard表

-- 检查字段是否已存在，如果不存在则添加
DO $$
BEGIN
    -- 检查source_url列是否存在
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 't_standard' 
        AND column_name = 'source_url'
    ) THEN
        -- 添加source_url字段
        ALTER TABLE t_standard ADD COLUMN source_url VARCHAR(2000) DEFAULT NULL;
        
        -- 添加字段注释
        COMMENT ON COLUMN t_standard.source_url IS '数据来源URL';
        
        -- 记录迁移日志
        RAISE NOTICE '已添加source_url字段到t_standard表';
    ELSE
        RAISE NOTICE 'source_url字段已存在于t_standard表中';
    END IF;
END $$;

-- 创建索引以提高查询性能（可选）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE tablename = 't_standard' 
        AND indexname = 'idx_standard_source_url'
    ) THEN
        CREATE INDEX idx_standard_source_url ON t_standard (source_url);
        RAISE NOTICE '已创建source_url字段的索引';
    END IF;
END $$;

