-- FDA 510K记录表结构
-- 表名: d_510k_records
-- 用途: 存储从FDA网站爬取的510K设备信息

CREATE TABLE IF NOT EXISTS `d_510k_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_name` varchar(500) NOT NULL COMMENT '设备名称',
  `applicant` varchar(300) DEFAULT NULL COMMENT '申请人/公司名称',
  `k_number` varchar(50) DEFAULT NULL COMMENT '510(K)编号',
  `decision_date` date DEFAULT NULL COMMENT '决策日期',
  `device_url` varchar(1000) DEFAULT NULL COMMENT '设备详情链接',
  `data_source` varchar(100) DEFAULT NULL COMMENT '数据来源',
  `country_code` varchar(10) DEFAULT NULL COMMENT '国家代码',
  `source_country` varchar(50) DEFAULT NULL COMMENT '来源国家',
  `crawl_time` datetime DEFAULT NULL COMMENT '爬取时间',
  `data_status` varchar(20) DEFAULT NULL COMMENT '数据状态',
  `decision_date_str` varchar(20) DEFAULT NULL COMMENT '原始决策日期字符串（用于调试）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remarks` varchar(1000) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_k_number` (`k_number`),
  KEY `idx_device_name` (`device_name`),
  KEY `idx_applicant` (`applicant`),
  KEY `idx_k_number` (`k_number`),
  KEY `idx_decision_date` (`decision_date`),
  KEY `idx_crawl_time` (`crawl_time`),
  KEY `idx_data_status` (`data_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FDA 510K记录表';

-- 插入示例数据（可选）
-- INSERT INTO `d_510k_records` (`device_name`, `applicant`, `k_number`, `decision_date`, `data_source`, `country_code`, `source_country`, `data_status`) 
-- VALUES ('示例设备', '示例公司', 'K123456', '2024-01-01', 'FDA 510K Database', 'US', 'US', 'ACTIVE');
