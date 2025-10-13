-- 删除所有没有实体类对应的多余表
-- 创建时间: 2025-10-09
-- 说明: 删除16个没有Java实体类支持的数据库表

-- ⚠️ 警告：此脚本会永久删除表及其所有数据！
-- ⚠️ 执行前请务必备份数据库！
-- 备份命令：mysqldump -u root -p common_db > common_db_backup_20251009.sql

-- ========== 删除顺序说明 ==========
-- 必须先删除有外键依赖的子表，再删除父表

-- ========== 第1步：删除视图（4个） ==========

DROP VIEW IF EXISTS common_db.v_eu_alerts_by_category;
DROP VIEW IF EXISTS common_db.v_eu_alerts_by_country;
DROP VIEW IF EXISTS common_db.v_eu_alerts_by_risk_type;
DROP VIEW IF EXISTS common_db.v_eu_alerts_monthly;

-- ========== 第2步：删除有外键的子表（先删除子表） ==========

-- 产品相关的子表
DROP TABLE IF EXISTS common_db.t_product_registration;
DROP TABLE IF EXISTS common_db.t_product_category_code;
DROP TABLE IF EXISTS common_db.t_product_recall;

-- ========== 第3步：删除产品相关表（4个） ==========

DROP TABLE IF EXISTS common_db.t_product;
DROP TABLE IF EXISTS common_db.t_standard;
DROP TABLE IF EXISTS common_db.t_source;

-- ========== 第4步：删除风险评分相关表（3个） ==========

DROP TABLE IF EXISTS common_db.t_risk_score;
DROP TABLE IF EXISTS common_db.t_risk_score_rule;
DROP TABLE IF EXISTS common_db.t_risk_weight_config;

-- ========== 第5步：删除通知和日志表（3个） ==========

DROP TABLE IF EXISTS common_db.t_notification;
DROP TABLE IF EXISTS common_db.t_system_log;
DROP TABLE IF EXISTS common_db.t_data_change_log;

-- ========== 第6步：删除其他多余表（4个） ==========

DROP TABLE IF EXISTS common_db.t_regulation_notice;
DROP TABLE IF EXISTS common_db.t_task;

-- 删除旧版本表
DROP TABLE IF EXISTS common_db.d_510k_records;
DROP TABLE IF EXISTS common_db.eu_safety_gate_alerts;

-- 删除重复表
DROP TABLE IF EXISTS common_db.t_guidance_document_2;

-- ========== 验证结果 ==========

-- 查看剩余的表（应该只有14个表 + 1个scheduled_crawler_config如果存在）
SELECT 
    TABLE_NAME as '保留的表',
    TABLE_ROWS as '行数',
    TABLE_COMMENT as '注释'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'common_db'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- 期望保留的表：
-- 1. daily_country_risk_stats
-- 2. t_country
-- 3. t_crawler_checkpoint
-- 4. t_crawler_data
-- 5. t_crawler_state
-- 6. t_customs_case
-- 7. t_device_510k
-- 8. t_device_event
-- 9. t_device_recall
-- 10. t_device_registration
-- 11. t_devicematch_keywords
-- 12. t_guidance_document
-- 13. t_keyword
-- 14. (t_scheduled_crawler_config - 如果存在)

