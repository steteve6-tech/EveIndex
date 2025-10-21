-- ========================================
-- 医疗设备认证监控系统 - 性能优化索引脚本
-- 版本: V1.0.1
-- 日期: 2025-01-14
-- 说明: 为高频查询字段添加索引以提升查询性能
-- ========================================

-- ========================================
-- 1. 认证新闻数据表 (t_crawler_data) 索引优化
-- ========================================

-- 复合索引：风险等级 + 数据源
CREATE INDEX IF NOT EXISTS idx_certnews_risk_source
ON t_crawler_data(risk_level, source_name)
WHERE deleted = 0;

-- 发布日期索引（用于时间范围查询）
CREATE INDEX IF NOT EXISTS idx_certnews_publish_date
ON t_crawler_data(publish_date DESC)
WHERE deleted = 0;

-- 爬取时间索引（用于获取最新数据）
CREATE INDEX IF NOT EXISTS idx_certnews_crawl_time
ON t_crawler_data(crawl_time DESC)
WHERE deleted = 0;

-- 国家代码索引
CREATE INDEX IF NOT EXISTS idx_certnews_country
ON t_crawler_data(country)
WHERE deleted = 0;

-- 复合索引：国家 + 风险等级 + 发布日期
CREATE INDEX IF NOT EXISTS idx_certnews_country_risk_date
ON t_crawler_data(country, risk_level, publish_date DESC)
WHERE deleted = 0;

-- 数据源 + 相关性状态 复合索引
CREATE INDEX IF NOT EXISTS idx_certnews_source_related
ON t_crawler_data(source_name, related)
WHERE deleted = 0;

-- 全文搜索准备：标题索引（MySQL 5.7+支持InnoDB全文索引）
-- CREATE FULLTEXT INDEX idx_certnews_title_fulltext ON t_crawler_data(title);

-- ========================================
-- 2. Device510K表 (t_device_510k) 索引优化
-- ========================================

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_510k_risk_country
ON t_device_510k(risk_level, jd_country)
WHERE data_status = 'ACTIVE';

-- K号唯一索引（如果不存在）
CREATE UNIQUE INDEX IF NOT EXISTS idx_510k_knumber_unique
ON t_device_510k(k_number);

-- 接收日期索引
CREATE INDEX IF NOT EXISTS idx_510k_date_received
ON t_device_510k(date_received DESC)
WHERE data_status = 'ACTIVE';

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_510k_crawl_time
ON t_device_510k(crawl_time DESC);

-- 数据源索引
CREATE INDEX IF NOT EXISTS idx_510k_data_source
ON t_device_510k(data_source);

-- 申请人名称索引（用于模糊搜索）
CREATE INDEX IF NOT EXISTS idx_510k_applicant
ON t_device_510k(applicant);

-- 设备类别索引
CREATE INDEX IF NOT EXISTS idx_510k_device_class
ON t_device_510k(device_class);

-- ========================================
-- 3. 设备召回表 (t_device_recall) 索引优化
-- ========================================

-- 召回事件ID唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_recall_cfresid_unique
ON t_device_recall(cfres_id);

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_recall_risk_country
ON t_device_recall(risk_level, jd_country);

-- 召回发布日期索引
CREATE INDEX IF NOT EXISTS idx_recall_date_posted
ON t_device_recall(event_date_posted DESC);

-- 召回等级索引
CREATE INDEX IF NOT EXISTS idx_recall_status
ON t_device_recall(recall_status);

-- 召回公司索引
CREATE INDEX IF NOT EXISTS idx_recall_firm
ON t_device_recall(recalling_firm);

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_recall_crawl_time
ON t_device_recall(crawl_time DESC);

-- ========================================
-- 4. 设备事件表 (t_device_event) 索引优化
-- ========================================

-- 报告编号唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_event_report_number_unique
ON t_device_event(report_number);

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_event_risk_country
ON t_device_event(risk_level, jd_country);

-- 事件日期索引
CREATE INDEX IF NOT EXISTS idx_event_date_of_event
ON t_device_event(date_of_event DESC);

-- 接收日期索引
CREATE INDEX IF NOT EXISTS idx_event_date_received
ON t_device_event(date_received DESC);

-- 制造商索引
CREATE INDEX IF NOT EXISTS idx_event_manufacturer
ON t_device_event(manufacturer_name);

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_event_crawl_time
ON t_device_event(crawl_time DESC);

-- ========================================
-- 5. 设备注册表 (t_device_registration) 索引优化
-- ========================================

-- 注册号索引
CREATE INDEX IF NOT EXISTS idx_registration_number
ON t_device_registration(registration_number(100));

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_registration_risk_country
ON t_device_registration(risk_level, jd_country);

-- 制造商索引
CREATE INDEX IF NOT EXISTS idx_registration_manufacturer
ON t_device_registration(manufacturer_name);

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_registration_crawl_time
ON t_device_registration(crawl_time DESC);

-- 数据源索引
CREATE INDEX IF NOT EXISTS idx_registration_data_source
ON t_device_registration(data_source);

-- ========================================
-- 6. 指导文档表 (t_guidance_document) 索引优化
-- ========================================

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_guidance_risk_country
ON t_guidance_document(risk_level, jd_country);

-- 发布日期索引
CREATE INDEX IF NOT EXISTS idx_guidance_publication_date
ON t_guidance_document(publication_date DESC);

-- 文档类型索引
CREATE INDEX IF NOT EXISTS idx_guidance_doc_type
ON t_guidance_document(document_type);

-- 指导状态索引
CREATE INDEX IF NOT EXISTS idx_guidance_status
ON t_guidance_document(guidance_status);

-- 话题索引
CREATE INDEX IF NOT EXISTS idx_guidance_topic
ON t_guidance_document(topic);

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_guidance_crawl_time
ON t_guidance_document(crawl_time DESC);

-- 数据源索引
CREATE INDEX IF NOT EXISTS idx_guidance_data_source
ON t_guidance_document(data_source);

-- ========================================
-- 7. 海关判例表 (t_customs_case) 索引优化
-- ========================================

-- 复合索引：风险等级 + 国家
CREATE INDEX IF NOT EXISTS idx_customs_risk_country
ON t_customs_case(risk_level, jd_country);

-- 判例编号索引
CREATE INDEX IF NOT EXISTS idx_customs_case_number
ON t_customs_case(case_number);

-- 爬取时间索引
CREATE INDEX IF NOT EXISTS idx_customs_crawl_time
ON t_customs_case(crawl_time DESC);

-- 数据源索引
CREATE INDEX IF NOT EXISTS idx_customs_data_source
ON t_customs_case(data_source);

-- ========================================
-- 8. 统一任务配置表 (t_unified_task_config) 索引优化
-- ========================================

-- 国家代码索引
CREATE INDEX IF NOT EXISTS idx_task_country_code
ON t_unified_task_config(country_code);

-- 爬虫名称索引
CREATE INDEX IF NOT EXISTS idx_task_crawler_name
ON t_unified_task_config(crawler_name);

-- 启用状态索引
CREATE INDEX IF NOT EXISTS idx_task_enabled
ON t_unified_task_config(enabled);

-- 复合索引：国家 + 爬虫名称 + 启用状态
CREATE INDEX IF NOT EXISTS idx_task_country_crawler_enabled
ON t_unified_task_config(country_code, crawler_name, enabled);

-- ========================================
-- 9. AI判断任务表 (t_ai_judge_task) 索引优化
-- ========================================

-- 任务ID唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_ai_task_id_unique
ON t_ai_judge_task(task_id);

-- 任务状态索引
CREATE INDEX IF NOT EXISTS idx_ai_task_status
ON t_ai_judge_task(status);

-- 任务类型索引
CREATE INDEX IF NOT EXISTS idx_ai_task_type
ON t_ai_judge_task(task_type);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_ai_task_create_time
ON t_ai_judge_task(create_time DESC);

-- ========================================
-- 脚本执行完成
-- ========================================

-- 查看已创建的索引（供验证）
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
--     INDEX_TYPE
-- FROM INFORMATION_SCHEMA.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME IN ('t_crawler_data', 't_device_510k', 't_device_recall',
--                      't_device_event', 't_device_registration', 't_guidance_document',
--                      't_customs_case', 't_unified_task_config', 't_ai_judge_task')
-- GROUP BY TABLE_NAME, INDEX_NAME
-- ORDER BY TABLE_NAME, INDEX_NAME;
