-- ============================================
-- 认证新闻爬虫定时任务表
-- ============================================

-- 任务配置表
CREATE TABLE IF NOT EXISTS `t_cert_news_task_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `crawler_type` VARCHAR(50) NOT NULL COMMENT '爬虫类型: SGS, UL, BEICE',
  `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `keyword` VARCHAR(200) DEFAULT NULL COMMENT '搜索关键词（可选）',
  `max_records` INT DEFAULT 50 COMMENT '每次爬取的最大记录数',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '任务描述',
  `last_execute_time` DATETIME DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` DATETIME DEFAULT NULL COMMENT '下次执行时间',
  `last_execute_status` VARCHAR(20) DEFAULT NULL COMMENT '上次执行状态: SUCCESS, FAILED',
  `last_execute_message` TEXT DEFAULT NULL COMMENT '上次执行结果消息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name` (`task_name`),
  KEY `idx_crawler_type` (`crawler_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_next_execute_time` (`next_execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻爬虫定时任务配置表';

-- 任务执行日志表
CREATE TABLE IF NOT EXISTS `t_cert_news_task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '任务配置ID',
  `task_name` VARCHAR(100) DEFAULT NULL COMMENT '任务名称',
  `crawler_type` VARCHAR(50) DEFAULT NULL COMMENT '爬虫类型: SGS, UL, BEICE',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '执行状态: SUCCESS, FAILED',
  `success_count` INT DEFAULT 0 COMMENT '爬取成功数量',
  `error_count` INT DEFAULT 0 COMMENT '爬取失败数量',
  `message` TEXT DEFAULT NULL COMMENT '执行结果消息',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_crawler_type` (`crawler_type`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻爬虫任务执行日志表';

-- ============================================
-- 插入示例任务配置
-- ============================================

-- SGS 认证新闻定时任务（每天凌晨2点执行）
INSERT INTO `t_cert_news_task_config`
(`task_name`, `crawler_type`, `cron_expression`, `enabled`, `keyword`, `max_records`, `description`)
VALUES
('SGS认证新闻每日爬取', 'SGS', '0 0 2 * * ?', 1, NULL, 50, '每天凌晨2点自动爬取SGS认证相关新闻');

-- UL Solutions 新闻定时任务（每天凌晨3点执行）
INSERT INTO `t_cert_news_task_config`
(`task_name`, `crawler_type`, `cron_expression`, `enabled`, `keyword`, `max_records`, `description`)
VALUES
('UL认证新闻每日爬取', 'UL', '0 0 3 * * ?', 1, NULL, 50, '每天凌晨3点自动爬取UL Solutions认证相关新闻');

-- 北测新闻定时任务（每天凌晨4点执行）
INSERT INTO `t_cert_news_task_config`
(`task_name`, `crawler_type`, `cron_expression`, `enabled`, `keyword`, `max_records`, `description`)
VALUES
('北测认证新闻每日爬取', 'BEICE', '0 0 4 * * ?', 1, NULL, 50, '每天凌晨4点自动爬取北测认证相关新闻');

-- ============================================
-- Cron表达式说明
-- ============================================
-- 格式: 秒 分 时 日 月 星期
--
-- 示例:
-- 0 0 2 * * ?       每天凌晨2点执行
-- 0 0 */6 * * ?     每6小时执行一次
-- 0 0 9 * * MON     每周一上午9点执行
-- 0 0 1 1 * ?       每月1号凌晨1点执行
-- 0 0 0 * * ?       每天午夜执行
-- 0 */30 * * * ?    每30分钟执行一次
-- ============================================
