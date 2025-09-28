-- 删除完全没用的数据库表
-- 执行前请确保已备份数据库

-- 删除重复的指导文档表
DROP TABLE IF EXISTS t_guidance_document_2;

-- 删除数据变更日志表
DROP TABLE IF EXISTS t_data_change_log;

-- 删除风险评分相关表
DROP TABLE IF EXISTS t_risk_score;
DROP TABLE IF EXISTS t_risk_score_rule;
DROP TABLE IF EXISTS t_risk_weight_config;

-- 删除系统日志表
DROP TABLE IF EXISTS t_system_log;

-- 删除任务表
DROP TABLE IF EXISTS t_task;

-- 删除通知表
DROP TABLE IF EXISTS t_notification;

-- 删除数据源表
DROP TABLE IF EXISTS t_source;

-- 删除竞品信息表（如果存在）
DROP TABLE IF EXISTS competitor_info;

-- 删除通用设备注册表（如果存在）
DROP TABLE IF EXISTS t_common_device_registration;

-- 删除标准表
DROP TABLE IF EXISTS t_standard;

-- 删除产品相关表
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_product_category_code;
DROP TABLE IF EXISTS t_product_recall;
DROP TABLE IF EXISTS t_product_registration;

-- 删除法规通知表
DROP TABLE IF EXISTS t_regulation_notice;

-- 删除欧盟安全门预警表
DROP TABLE IF EXISTS eu_safety_gate_alerts;

-- 删除FDA 510K记录表
DROP TABLE IF EXISTS d_510k_records;

-- 删除相关的视图（如果存在）
DROP VIEW IF EXISTS v_risk_score_summary;
DROP VIEW IF EXISTS v_task_status;
DROP VIEW IF EXISTS v_notification_stats;

-- 删除相关的索引（如果存在）
-- 注意：删除表时会自动删除相关索引，这里只是记录

-- 清理完成提示
SELECT 'Unused tables dropped successfully' AS status;
