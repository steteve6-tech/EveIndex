-- 欧盟安全门预警系统数据表
-- 对应Safety Gate Alert数据

CREATE TABLE IF NOT EXISTS `eu_safety_gate_alerts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `alert_number` VARCHAR(100) NOT NULL COMMENT '预警编号',
    `publication_date` DATE COMMENT '发布日期',
    `product` VARCHAR(500) COMMENT '产品名称',
    `product_description` TEXT COMMENT '产品描述',
    `product_model` VARCHAR(200) COMMENT '产品型号',
    `brand` VARCHAR(200) COMMENT '主要品牌',
    `category` VARCHAR(200) COMMENT '产品类别',
    `risk` VARCHAR(200) COMMENT '主要风险',
    `risk_type` VARCHAR(200) COMMENT '风险类型',
    `country` VARCHAR(100) COMMENT '产品来源国家',
    `notifying_country` VARCHAR(100) COMMENT '通知国家',
    `description` TEXT COMMENT '预警描述',
    `measures` TEXT COMMENT '采取的措施',
    `url` VARCHAR(1000) COMMENT '详情链接',
    `crawl_time` DATETIME COMMENT '爬取时间',
    `data_source` VARCHAR(100) DEFAULT 'Safety Gate Alert' COMMENT '数据源',
    `jd_country` VARCHAR(10) DEFAULT 'EU' COMMENT '国家代码',
    `keywords` TEXT COMMENT '关键词',
    `risk_level` ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM' COMMENT '风险等级',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_alert_number` (`alert_number`),
    KEY `idx_publication_date` (`publication_date`),
    KEY `idx_product` (`product`),
    KEY `idx_brand` (`brand`),
    KEY `idx_category` (`category`),
    KEY `idx_risk_type` (`risk_type`),
    KEY `idx_country` (`country`),
    KEY `idx_notifying_country` (`notifying_country`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_crawl_time` (`crawl_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_jd_country` (`jd_country`),
    KEY `idx_data_source` (`data_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='欧盟安全门预警系统数据表';

-- 创建复合索引（如果不存在）
CREATE INDEX IF NOT EXISTS `idx_country_category` ON `eu_safety_gate_alerts` (`country`, `category`);
CREATE INDEX IF NOT EXISTS `idx_publication_date_country` ON `eu_safety_gate_alerts` (`publication_date`, `country`);
CREATE INDEX IF NOT EXISTS `idx_risk_level_publication_date` ON `eu_safety_gate_alerts` (`risk_level`, `publication_date`);

-- 创建全文索引（用于关键词搜索，如果不存在）
CREATE FULLTEXT INDEX IF NOT EXISTS `ft_product_search` ON `eu_safety_gate_alerts` (`product`, `product_description`);
CREATE FULLTEXT INDEX IF NOT EXISTS `ft_brand_search` ON `eu_safety_gate_alerts` (`brand`);
CREATE FULLTEXT INDEX IF NOT EXISTS `ft_risk_search` ON `eu_safety_gate_alerts` (`risk`, `risk_type`);
CREATE FULLTEXT INDEX IF NOT EXISTS `ft_description_search` ON `eu_safety_gate_alerts` (`description`, `measures`);

-- 插入示例数据（可选）
INSERT INTO `eu_safety_gate_alerts` (
    `alert_number`, `publication_date`, `product`, 
    `product_description`, `brand`, `category`, `risk`, `country`, 
    `notifying_country`, `description`, `measures`, `url`, `crawl_time`, 
    `data_source`, `jd_country`, `risk_level`
) VALUES (
    'A12/00001/25', '2025-01-15', 'Skin Analysis Device',
    'Advanced skin analysis equipment for medical and cosmetic use', 'VisiaLab',
    'Electrical appliances and equipment', 'Electrical shock', 'Italy',
    'Germany', 'Device may cause electrical shock due to faulty wiring',
    'Product recall and replacement', 'https://ec.europa.eu/safety-gate-alerts/...',
    NOW(), 'Safety Gate Alert', 'EU', 'HIGH'
);

-- 创建视图：按国家统计预警数量（如果不存在）
CREATE OR REPLACE VIEW `v_eu_alerts_by_country` AS
SELECT 
    `country`,
    COUNT(*) as `alert_count`,
    COUNT(CASE WHEN `risk_level` = 'HIGH' THEN 1 END) as `high_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'MEDIUM' THEN 1 END) as `medium_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'LOW' THEN 1 END) as `low_risk_count`,
    MAX(`publication_date`) as `latest_alert_date`
FROM `eu_safety_gate_alerts`
GROUP BY `country`
ORDER BY `alert_count` DESC;

-- 创建视图：按产品类别统计预警数量（如果不存在）
CREATE OR REPLACE VIEW `v_eu_alerts_by_category` AS
SELECT 
    `category`,
    COUNT(*) as `alert_count`,
    COUNT(CASE WHEN `risk_level` = 'HIGH' THEN 1 END) as `high_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'MEDIUM' THEN 1 END) as `medium_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'LOW' THEN 1 END) as `low_risk_count`,
    MAX(`publication_date`) as `latest_alert_date`
FROM `eu_safety_gate_alerts`
WHERE `category` IS NOT NULL
GROUP BY `category`
ORDER BY `alert_count` DESC;

-- 创建视图：按风险类型统计预警数量（如果不存在）
CREATE OR REPLACE VIEW `v_eu_alerts_by_risk_type` AS
SELECT 
    `risk_type`,
    COUNT(*) as `alert_count`,
    COUNT(CASE WHEN `risk_level` = 'HIGH' THEN 1 END) as `high_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'MEDIUM' THEN 1 END) as `medium_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'LOW' THEN 1 END) as `low_risk_count`,
    MAX(`publication_date`) as `latest_alert_date`
FROM `eu_safety_gate_alerts`
WHERE `risk_type` IS NOT NULL
GROUP BY `risk_type`
ORDER BY `alert_count` DESC;

-- 创建视图：月度预警统计（如果不存在）
CREATE OR REPLACE VIEW `v_eu_alerts_monthly` AS
SELECT 
    YEAR(`publication_date`) as `year`,
    MONTH(`publication_date`) as `month`,
    COUNT(*) as `alert_count`,
    COUNT(CASE WHEN `risk_level` = 'HIGH' THEN 1 END) as `high_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'MEDIUM' THEN 1 END) as `medium_risk_count`,
    COUNT(CASE WHEN `risk_level` = 'LOW' THEN 1 END) as `low_risk_count`
FROM `eu_safety_gate_alerts`
WHERE `publication_date` IS NOT NULL
GROUP BY YEAR(`publication_date`), MONTH(`publication_date`)
ORDER BY `year` DESC, `month` DESC;

-- 创建存储过程：清理过期数据（如果不存在）
DELIMITER //
DROP PROCEDURE IF EXISTS `sp_cleanup_old_eu_alerts`//
CREATE PROCEDURE `sp_cleanup_old_eu_alerts`(IN days_to_keep INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 删除指定天数之前的数据
    DELETE FROM `eu_safety_gate_alerts` 
    WHERE `crawl_time` < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    -- 记录清理结果
    SELECT ROW_COUNT() as `deleted_records`;
    
    COMMIT;
END //
DELIMITER ;

-- 创建存储过程：获取预警统计信息（如果不存在）
DELIMITER //
DROP PROCEDURE IF EXISTS `sp_get_eu_alerts_statistics`//
CREATE PROCEDURE `sp_get_eu_alerts_statistics`()
BEGIN
    SELECT 
        'Total Alerts' as `metric`,
        COUNT(*) as `value`
    FROM `eu_safety_gate_alerts`
    
    UNION ALL
    
    SELECT 
        'High Risk Alerts' as `metric`,
        COUNT(*) as `value`
    FROM `eu_safety_gate_alerts`
    WHERE `risk_level` = 'HIGH'
    
    UNION ALL
    
    SELECT 
        'Countries' as `metric`,
        COUNT(DISTINCT `country`) as `value`
    FROM `eu_safety_gate_alerts`
        WHERE `country` IS NOT NULL
    
    UNION ALL
    
    SELECT 
        'Categories' as `metric`,
        COUNT(DISTINCT `category`) as `value`
    FROM `eu_safety_gate_alerts`
    WHERE `category` IS NOT NULL
    
    UNION ALL
    
    SELECT 
        'Latest Alert Date' as `metric`,
        MAX(`publication_date`) as `value`
    FROM `eu_safety_gate_alerts`
    WHERE `publication_date` IS NOT NULL;
END //
DELIMITER ;