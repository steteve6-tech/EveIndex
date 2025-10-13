-- 添加索引优化查询
ALTER TABLE t_unified_task_config 
ADD INDEX idx_task_type_crawler (task_type, crawler_name),
ADD INDEX idx_crawler_enabled (crawler_name, enabled);
