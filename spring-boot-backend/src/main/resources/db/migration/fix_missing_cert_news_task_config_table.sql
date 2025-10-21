-- 创建认证新闻定时任务配置表
CREATE TABLE IF NOT EXISTS `t_cert_news_task_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `crawler_type` varchar(50) NOT NULL COMMENT '爬虫类型(SGS/UL/BEICE)',
  `keyword` varchar(500) DEFAULT NULL COMMENT '搜索关键词',
  `max_records` int DEFAULT 100 COMMENT '最大记录数',
  `cron_expression` varchar(100) NOT NULL COMMENT 'Cron表达式',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `description` text COMMENT '任务描述',
  `last_execute_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `last_execute_status` varchar(20) DEFAULT NULL COMMENT '最后执行状态',
  `last_execute_message` text COMMENT '最后执行消息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_crawler_type` (`crawler_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证新闻定时任务配置表';

-- 插入默认任务配置
INSERT INTO `t_cert_news_task_config` 
(`task_name`, `crawler_type`, `keyword`, `max_records`, `cron_expression`, `enabled`, `description`)
VALUES
('SGS新闻爬虫', 'SGS', 'medical device,certification', 50, '0 0 2 * * ?', 1, '每天凌晨2点爬取SGS认证新闻'),
('UL新闻爬虫', 'UL', 'medical device,certification', 50, '0 0 3 * * ?', 1, '每天凌晨3点爬取UL认证新闻'),
('北测新闻爬虫', 'BEICE', 'medical device,certification', 50, '0 0 4 * * ?', 1, '每天凌晨4点爬取北测认证新闻')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

