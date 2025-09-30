-- 创建定时爬取配置表
CREATE TABLE IF NOT EXISTS `t_scheduled_crawler_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `module_name` varchar(50) NOT NULL COMMENT '爬虫模块名称',
  `crawler_name` varchar(50) NOT NULL COMMENT '爬虫名称',
  `country_code` varchar(10) DEFAULT NULL COMMENT '国家/地区代码',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用定时任务',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'Cron表达式',
  `description` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `crawl_params` text COMMENT '爬取参数配置（JSON格式）',
  `last_execution_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `next_execution_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `last_execution_status` varchar(20) DEFAULT NULL COMMENT '最后执行状态',
  `last_execution_result` text COMMENT '最后执行结果信息',
  `execution_count` bigint DEFAULT '0' COMMENT '执行次数统计',
  `success_count` bigint DEFAULT '0' COMMENT '成功执行次数统计',
  `failure_count` bigint DEFAULT '0' COMMENT '失败执行次数统计',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '逻辑删除字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_crawler_country` (`module_name`, `crawler_name`, `country_code`, `deleted`),
  KEY `idx_module_name` (`module_name`),
  KEY `idx_crawler_name` (`crawler_name`),
  KEY `idx_country_code` (`country_code`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_next_execution_time` (`next_execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时爬取配置表';

-- 删除现有配置数据（避免重复）
DELETE FROM `t_scheduled_crawler_config` WHERE `deleted` = 0;

-- 插入默认配置数据
INSERT INTO `t_scheduled_crawler_config` 
(`module_name`, `crawler_name`, `country_code`, `enabled`, `cron_expression`, `description`, `crawl_params`, `execution_count`, `success_count`, `failure_count`, `deleted`) 
VALUES
-- CertNewsData模块的3个爬虫配置
('certnewsdata', 'SGS', NULL, 1, '0 0 2 * * ?', '每天凌晨2点执行SGS爬虫', '{"batchSize": 50, "maxRecords": 200}', 0, 0, 0, 0),
('certnewsdata', 'UL', NULL, 1, '0 30 2 * * ?', '每天凌晨2点30分执行UL爬虫', '{"batchSize": 50, "maxRecords": 200}', 0, 0, 0, 0),
('certnewsdata', 'Beice', NULL, 1, '0 0 3 * * ?', '每天凌晨3点执行Beice爬虫', '{"batchSize": 30, "maxRecords": 150}', 0, 0, 0, 0),

-- 美国6个实体类的爬虫配置
('device510k', 'US_510K', 'US', 1, '0 0 4 * * ?', '每天凌晨4点执行美国510K爬虫', '{"batchSize": 100, "maxRecords": 500}', 0, 0, 0, 0),
('deviceeventreport', 'US_Event', 'US', 1, '0 30 4 * * ?', '每天凌晨4点30分执行美国事件报告爬虫', '{"batchSize": 100, "maxRecords": 500}', 0, 0, 0, 0),
('devicerecallrecord', 'US_Recall', 'US', 1, '0 0 5 * * ?', '每天凌晨5点执行美国召回记录爬虫', '{"batchSize": 100, "maxRecords": 500}', 0, 0, 0, 0),
('deviceregistrationrecord', 'US_Registration', 'US', 1, '0 30 5 * * ?', '每天凌晨5点30分执行美国注册记录爬虫', '{"batchSize": 100, "maxRecords": 500}', 0, 0, 0, 0),
('guidancedocument', 'US_Guidance', 'US', 1, '0 0 6 * * ?', '每天凌晨6点执行美国指导文档爬虫', '{"batchSize": 50, "maxRecords": 200}', 0, 0, 0, 0),
('customscase', 'US_CustomsCase', 'US', 1, '0 30 6 * * ?', '每天凌晨6点30分执行美国海关案例爬虫', '{"batchSize": 20, "maxRecords": 100}', 0, 0, 0, 0),

-- 欧盟4个实体类的爬虫配置
('devicerecallrecord', 'EU_Recall', 'EU', 1, '0 0 7 * * ?', '每天凌晨7点执行欧盟召回记录爬虫', '{"batchSize": 50, "maxRecords": 200}', 0, 0, 0, 0),
('deviceregistrationrecord', 'EU_Registration', 'EU', 1, '0 30 7 * * ?', '每天凌晨7点30分执行欧盟注册记录爬虫', '{"batchSize": 50, "maxRecords": 200}', 0, 0, 0, 0),
('guidancedocument', 'EU_Guidance', 'EU', 1, '0 0 8 * * ?', '每天凌晨8点执行欧盟指导文档爬虫', '{"batchSize": 30, "maxRecords": 150}', 0, 0, 0, 0),
('customscase', 'EU_CustomsCase', 'EU', 1, '0 30 8 * * ?', '每天凌晨8点30分执行欧盟海关案例爬虫', '{"batchSize": 20, "maxRecords": 100}', 0, 0, 0, 0);
