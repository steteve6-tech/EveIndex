-- =====================================================
-- 认证监控系统数据库建立脚本
-- 基于当前实体类生成
-- 创建时间: 2025-09-28
-- 版本: 1.0
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS common_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE common_db;

-- =====================================================
-- 1. 国家基础信息表
-- =====================================================
DROP TABLE IF EXISTS t_country;
CREATE TABLE t_country (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    country_code VARCHAR(2) NOT NULL UNIQUE COMMENT '国家编码（如US/CN/EU）',
    country_name VARCHAR(50) NOT NULL COMMENT '国家名称',
    region VARCHAR(20) COMMENT '所属地区（如北美/亚太）',
    en_name VARCHAR(100) COMMENT '英文缩写（如us）',
    full_name VARCHAR(100) COMMENT '全名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_country_code (country_code),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国家基础信息表';

-- =====================================================
-- 2. 设备510K记录表
-- =====================================================
DROP TABLE IF EXISTS t_device_510k;
CREATE TABLE t_device_510k (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_class VARCHAR(10) COMMENT '设备类别',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级',
    keywords TEXT COMMENT '关键词数组（JSON格式存储）',
    trade_name VARCHAR(255) COMMENT '品牌名称',
    applicant VARCHAR(255) COMMENT '申请人',
    country_code VARCHAR(2) COMMENT '国家代码',
    date_received DATE COMMENT '接收日期',
    device_name VARCHAR(255) COMMENT '设备名称',
    k_number VARCHAR(32) UNIQUE COMMENT 'K号',
    data_source VARCHAR(50) COMMENT '数据源',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（自动生成）',
    jd_country VARCHAR(20) COMMENT '京东国家',
    crawl_time DATETIME COMMENT '爬取时间',
    data_status VARCHAR(20) COMMENT '数据状态',
    INDEX idx_k_number (k_number),
    INDEX idx_device_class (device_class),
    INDEX idx_risk_level (risk_level),
    INDEX idx_country_code (country_code),
    INDEX idx_data_source (data_source),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备510K记录表';

-- =====================================================
-- 3. 设备事件报告表
-- =====================================================
DROP TABLE IF EXISTS t_device_event;
CREATE TABLE t_device_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_number VARCHAR(64) NOT NULL UNIQUE COMMENT '核心业务标识 - 支持多种编号格式',
    date_of_event DATE COMMENT '事件日期',
    date_received DATE COMMENT '接收日期',
    brand_name VARCHAR(255) COMMENT '品牌名称',
    generic_name VARCHAR(255) COMMENT '通用名称',
    manufacturer_name VARCHAR(255) COMMENT '制造商名称',
    device_class VARCHAR(10) COMMENT '设备类别',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级',
    keywords TEXT COMMENT '关键词数组（JSON字符串或分号分隔）',
    data_source VARCHAR(50) COMMENT '数据源',
    jd_country VARCHAR(20) COMMENT '用于判定数据所属国家（如 US/CN/EU 等）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_number (report_number),
    INDEX idx_risk_level (risk_level),
    INDEX idx_device_class (device_class),
    INDEX idx_data_source (data_source),
    INDEX idx_jd_country (jd_country),
    INDEX idx_date_received (date_received)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备事件报告表';

-- =====================================================
-- 4. 设备召回记录表
-- =====================================================
DROP TABLE IF EXISTS t_device_recall;
CREATE TABLE t_device_recall (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    cfres_id VARCHAR(100) COMMENT '召回事件ID（从FDA网站URL提取）',
    product_description TEXT COMMENT '产品描述',
    recalling_firm VARCHAR(255) COMMENT '召回公司',
    recall_status VARCHAR(100) COMMENT '召回等级（CLASS I/II/III）',
    event_date_posted DATE COMMENT '召回发布日期',
    device_name VARCHAR(255) COMMENT '设备名称（复用产品描述）',
    product_code VARCHAR(50) COMMENT '产品代码（D_recall设置为空）',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级（根据召回等级计算）',
    keywords TEXT COMMENT '关键词（JSON格式）',
    data_source VARCHAR(50) COMMENT '数据源',
    country_code VARCHAR(20) NOT NULL COMMENT '国家代码',
    jd_country VARCHAR(20) COMMENT '数据适用国家',
    INDEX idx_cfres_id (cfres_id),
    INDEX idx_recall_status (recall_status),
    INDEX idx_risk_level (risk_level),
    INDEX idx_country_code (country_code),
    INDEX idx_data_source (data_source),
    INDEX idx_event_date_posted (event_date_posted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗器械召回记录表';

-- =====================================================
-- 5. 设备注册记录表
-- =====================================================
DROP TABLE IF EXISTS t_device_registration;
CREATE TABLE t_device_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_source VARCHAR(50) NOT NULL COMMENT '数据源',
    jd_country VARCHAR(20) NOT NULL COMMENT '数据源国家',
    registration_number TEXT NOT NULL COMMENT '主要标识符（US: K_number+pma_number, EU: udi_di）',
    fei_number VARCHAR(50) COMMENT '次要标识符（US: fei_number, EU: basic_udi_di）',
    manufacturer_name TEXT COMMENT '制造商名称',
    device_name TEXT COMMENT '设备名称',
    proprietary_name LONGTEXT COMMENT '专有名称/商标名称',
    device_class LONGTEXT COMMENT '设备类别',
    status_code VARCHAR(100) COMMENT '状态码',
    created_date VARCHAR(50) COMMENT '创建日期',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级评估',
    keywords LONGTEXT COMMENT '关键词（JSON数组）',
    crawl_time DATETIME COMMENT '爬取时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_data_source (data_source),
    INDEX idx_jd_country (jd_country),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status_code (status_code),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备注册记录共有数据表';

-- =====================================================
-- 6. 医疗文档表
-- =====================================================
DROP TABLE IF EXISTS t_guidance_document;
CREATE TABLE t_guidance_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    document_type VARCHAR(20) COMMENT '文档类型（GUIDANCE-指导文档, NEWS-新闻, NOTICE-通知等）',
    title VARCHAR(500) NOT NULL COMMENT '文档标题',
    publication_date DATE COMMENT '发布日期',
    topic VARCHAR(255) COMMENT '话题/主题',
    guidance_status VARCHAR(50) COMMENT '指导状态（Final, Draft, Withdrawn等）',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级',
    keywords TEXT COMMENT '关键词数组（JSON格式存储）',
    document_url VARCHAR(1000) COMMENT '指导文档URL',
    source_url VARCHAR(1000) COMMENT '数据来源URL',
    data_source VARCHAR(50) COMMENT '数据来源（如 FDA、CBP 等）',
    jd_country VARCHAR(10) COMMENT '来源国家（如 US、EU 等）',
    crawl_time DATETIME COMMENT '爬取时间',
    data_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '数据状态（ACTIVE, INACTIVE等）',
    created_time DATETIME COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    -- EU 新闻特有字段
    news_type VARCHAR(100) COMMENT '新闻类型（EU特有）',
    description TEXT COMMENT '文章描述（EU特有）',
    read_time VARCHAR(50) COMMENT '阅读时间（EU特有）',
    image_url VARCHAR(1000) COMMENT '图片URL（EU特有）',
    image_alt VARCHAR(500) COMMENT '图片alt文本（EU特有）',
    article_index INT COMMENT '文章序号（EU特有）',
    INDEX idx_document_type (document_type),
    INDEX idx_risk_level (risk_level),
    INDEX idx_data_source (data_source),
    INDEX idx_jd_country (jd_country),
    INDEX idx_publication_date (publication_date),
    INDEX idx_guidance_status (guidance_status),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗文档表';

-- =====================================================
-- 7. 海关过往判例表
-- =====================================================
DROP TABLE IF EXISTS t_customs_case;
CREATE TABLE t_customs_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    case_number VARCHAR(1000) COMMENT '判例编号/URL',
    case_date DATE COMMENT '判例日期',
    hs_code_used TEXT COMMENT '裁定使用的HS编码（多个编码用逗号分隔）',
    ruling_result TEXT COMMENT '裁定结果（如归类认定/处罚决定）',
    violation_type VARCHAR(50) COMMENT '违规类型（如标签不符/归类错误）',
    penalty_amount DECIMAL(12,2) COMMENT '处罚金额（如有）',
    data_source VARCHAR(255) COMMENT '数据来源',
    jd_country VARCHAR(20) NOT NULL DEFAULT 'US' COMMENT '数据来源国家（如US/CN/EU等）',
    crawl_time DATETIME COMMENT '爬取时间',
    data_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '数据状态',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级',
    keywords TEXT COMMENT '关键词数组（JSON格式存储）',
    INDEX idx_case_number (case_number(255)),
    INDEX idx_case_date (case_date),
    INDEX idx_violation_type (violation_type),
    INDEX idx_risk_level (risk_level),
    INDEX idx_jd_country (jd_country),
    INDEX idx_data_source (data_source),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='海关过往判例表';

-- =====================================================
-- 8. 关键词表
-- =====================================================
DROP TABLE IF EXISTS t_keyword;
CREATE TABLE t_keyword (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关键词ID',
    keyword VARCHAR(255) NOT NULL UNIQUE COMMENT '关键词内容',
    description TEXT COMMENT '关键词描述',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_keyword (keyword),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词表';

-- =====================================================
-- 9. 设备匹配关键词表
-- =====================================================
DROP TABLE IF EXISTS t_devicematch_keywords;
CREATE TABLE t_devicematch_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    keyword VARCHAR(255) NOT NULL COMMENT '关键词内容',
    keyword_type ENUM('NORMAL', 'BLACKLIST') NOT NULL DEFAULT 'NORMAL' COMMENT '关键词类型',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_keyword_type (keyword, keyword_type),
    INDEX idx_keyword (keyword),
    INDEX idx_keyword_type (keyword_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备匹配关键词表';

-- =====================================================
-- 10. 爬虫断点续传表
-- =====================================================
DROP TABLE IF EXISTS t_crawler_checkpoint;
CREATE TABLE t_crawler_checkpoint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    crawler_type VARCHAR(100) NOT NULL COMMENT '爬虫类型标识',
    search_term VARCHAR(500) COMMENT '搜索条件（用于标识不同的爬取任务）',
    date_from VARCHAR(20) COMMENT '时间范围开始日期',
    date_to VARCHAR(20) COMMENT '时间范围结束日期',
    current_skip INT NOT NULL DEFAULT 0 COMMENT '当前爬取的偏移量（skip值）',
    total_fetched INT NOT NULL DEFAULT 0 COMMENT '已爬取的总记录数',
    target_total INT COMMENT '目标总记录数（null表示爬取所有数据）',
    batch_size INT NOT NULL COMMENT '批次大小',
    status ENUM('RUNNING', 'COMPLETED', 'FAILED', 'PAUSED') NOT NULL DEFAULT 'RUNNING' COMMENT '爬取状态',
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    error_message VARCHAR(1000) COMMENT '错误信息（如果爬取失败）',
    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status),
    INDEX idx_last_updated (last_updated),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫断点续传表';

-- =====================================================
-- 11. 爬虫状态表
-- =====================================================
DROP TABLE IF EXISTS t_crawler_state;
CREATE TABLE t_crawler_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    crawler_name VARCHAR(50) NOT NULL COMMENT '爬虫名称（如：SGS, UL）',
    crawler_type VARCHAR(50) COMMENT '爬虫类型（如：certification, news）',
    last_crawl_time DATETIME COMMENT '最后爬取时间',
    last_crawled_id VARCHAR(255) COMMENT '最后爬取的数据ID或标识',
    last_crawled_title VARCHAR(500) COMMENT '最后爬取的数据标题',
    last_crawled_url VARCHAR(1000) COMMENT '最后爬取的数据URL',
    last_crawled_publish_time DATETIME COMMENT '最后爬取的数据发布时间',
    last_crawled_count INT COMMENT '最后爬取的数据数量',
    total_crawled_count BIGINT DEFAULT 0 COMMENT '累计爬取总数',
    status ENUM('RUNNING', 'IDLE', 'ERROR', 'DISABLED') DEFAULT 'IDLE' COMMENT '爬虫状态',
    last_error_message VARCHAR(1000) COMMENT '最后错误信息',
    last_error_time DATETIME COMMENT '最后错误时间',
    consecutive_error_count INT DEFAULT 0 COMMENT '连续错误次数',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    remarks VARCHAR(1000) COMMENT '备注信息',
    INDEX idx_crawler_name (crawler_name),
    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status),
    INDEX idx_enabled (enabled),
    INDEX idx_last_crawl_time (last_crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫状态表';

-- =====================================================
-- 12. 爬虫数据表
-- =====================================================
DROP TABLE IF EXISTS t_crawler_data;
CREATE TABLE t_crawler_data (
    id VARCHAR(50) PRIMARY KEY COMMENT '数据ID',
    source_name VARCHAR(255) COMMENT '数据源名称（如：UL Solutions, SGS等）',
    title VARCHAR(500) COMMENT '标题',
    url VARCHAR(1000) COMMENT 'URL链接',
    summary LONGTEXT COMMENT '内容摘要',
    content LONGTEXT COMMENT '详细内容',
    country VARCHAR(100) COMMENT '国家/地区',
    type VARCHAR(100) COMMENT '类型/分类',
    product VARCHAR(500) COMMENT '适用商品/产品',
    publish_date VARCHAR(100) COMMENT '发布时间',
    release_date JSON COMMENT '发布时间列表（JSON格式存储）',
    execution_date JSON COMMENT '执行时间列表（JSON格式存储）',
    crawl_time DATETIME COMMENT '爬取时间',
    status ENUM('NEW', 'PROCESSING', 'PROCESSED', 'ERROR', 'DUPLICATE') COMMENT '数据状态',
    is_processed BOOLEAN COMMENT '是否已处理',
    processed_time DATETIME COMMENT '处理时间',
    remarks TEXT COMMENT '备注',
    related BOOLEAN COMMENT '是否相关',
    matched_keywords TEXT COMMENT '匹配的关键词',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM' COMMENT '风险等级',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除字段',
    INDEX idx_source_name (source_name),
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level),
    INDEX idx_related (related),
    INDEX idx_crawl_time (crawl_time),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫数据表';

-- =====================================================
-- 13. 每日国家风险统计表
-- =====================================================
DROP TABLE IF EXISTS daily_country_risk_stats;
CREATE TABLE daily_country_risk_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    country VARCHAR(100) NOT NULL COMMENT '国家名称',
    high_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '高风险数据数量',
    medium_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '中风险数据数量',
    low_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '低风险数据数量',
    no_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '无风险数据数量',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '总数据数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_stat_date_country (stat_date, country),
    INDEX idx_stat_date (stat_date),
    INDEX idx_country (country),
    INDEX idx_total_count (total_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日国家高风险数据统计表';

-- =====================================================
-- 插入基础数据
-- =====================================================

-- 插入国家基础数据
INSERT INTO t_country (country_code, country_name, region, en_name, full_name) VALUES
('US', '美国', '北美', 'us', 'United States of America'),
('CN', '中国', '亚太', 'cn', 'People\'s Republic of China'),
('EU', '欧盟', '欧洲', 'eu', 'European Union'),
('JP', '日本', '亚太', 'jp', 'Japan'),
('KR', '韩国', '亚太', 'kr', 'Republic of Korea'),
('CA', '加拿大', '北美', 'ca', 'Canada'),
('AU', '澳大利亚', '亚太', 'au', 'Australia'),
('GB', '英国', '欧洲', 'gb', 'United Kingdom'),
('DE', '德国', '欧洲', 'de', 'Germany'),
('FR', '法国', '欧洲', 'fr', 'France');

-- 插入基础关键词数据
INSERT INTO t_keyword (keyword, description, enabled, sort_order) VALUES
('Skin', '皮肤相关设备', TRUE, 1),
('Analyzer', '分析仪', TRUE, 2),
('3D', '三维技术', TRUE, 3),
('AI', '人工智能', TRUE, 4),
('AIMYSKIN', 'AI皮肤分析', TRUE, 5),
('Facial', '面部相关', TRUE, 6),
('Detector', '检测器', TRUE, 7),
('Scanner', '扫描仪', TRUE, 8),
('Care', '护理设备', TRUE, 9),
('Portable', '便携式设备', TRUE, 10);

-- 插入设备匹配关键词数据
INSERT INTO t_devicematch_keywords (keyword, keyword_type, enabled) VALUES
('Skin', 'NORMAL', TRUE),
('Analyzer', 'NORMAL', TRUE),
('3D', 'NORMAL', TRUE),
('AI', 'NORMAL', TRUE),
('AIMYSKIN', 'NORMAL', TRUE),
('Facial', 'NORMAL', TRUE),
('Detector', 'NORMAL', TRUE),
('Scanner', 'NORMAL', TRUE),
('Care', 'NORMAL', TRUE),
('Portable', 'NORMAL', TRUE);

-- =====================================================
-- 创建视图
-- =====================================================

-- 创建风险统计视图
CREATE OR REPLACE VIEW v_risk_summary AS
SELECT 
    'HIGH' as risk_level,
    COUNT(*) as count
FROM t_device_510k WHERE risk_level = 'HIGH'
UNION ALL
SELECT 
    'MEDIUM' as risk_level,
    COUNT(*) as count
FROM t_device_510k WHERE risk_level = 'MEDIUM'
UNION ALL
SELECT 
    'LOW' as risk_level,
    COUNT(*) as count
FROM t_device_510k WHERE risk_level = 'LOW'
UNION ALL
SELECT 
    'NONE' as risk_level,
    COUNT(*) as count
FROM t_device_510k WHERE risk_level = 'NONE';

-- 创建爬虫状态统计视图
CREATE OR REPLACE VIEW v_crawler_status_summary AS
SELECT 
    crawler_name,
    status,
    COUNT(*) as count,
    MAX(last_crawl_time) as last_crawl_time
FROM t_crawler_state
GROUP BY crawler_name, status;

-- =====================================================
-- 创建存储过程
-- =====================================================

DELIMITER //

-- 创建更新风险统计的存储过程
CREATE PROCEDURE UpdateRiskStats(IN p_country VARCHAR(100), IN p_date DATE)
BEGIN
    DECLARE high_count BIGINT DEFAULT 0;
    DECLARE medium_count BIGINT DEFAULT 0;
    DECLARE low_count BIGINT DEFAULT 0;
    DECLARE no_count BIGINT DEFAULT 0;
    DECLARE total_count BIGINT DEFAULT 0;
    
    -- 统计各风险等级数量
    SELECT COUNT(*) INTO high_count FROM t_crawler_data 
    WHERE country = p_country AND risk_level = 'HIGH' AND deleted = 0;
    
    SELECT COUNT(*) INTO medium_count FROM t_crawler_data 
    WHERE country = p_country AND risk_level = 'MEDIUM' AND deleted = 0;
    
    SELECT COUNT(*) INTO low_count FROM t_crawler_data 
    WHERE country = p_country AND risk_level = 'LOW' AND deleted = 0;
    
    SELECT COUNT(*) INTO no_count FROM t_crawler_data 
    WHERE country = p_country AND risk_level = 'NONE' AND deleted = 0;
    
    SET total_count = high_count + medium_count + low_count + no_count;
    
    -- 插入或更新统计记录
    INSERT INTO daily_country_risk_stats 
    (stat_date, country, high_risk_count, medium_risk_count, low_risk_count, no_risk_count, total_count)
    VALUES (p_date, p_country, high_count, medium_count, low_count, no_count, total_count)
    ON DUPLICATE KEY UPDATE
        high_risk_count = high_count,
        medium_risk_count = medium_count,
        low_risk_count = low_count,
        no_risk_count = no_count,
        total_count = total_count,
        updated_at = CURRENT_TIMESTAMP;
END //

DELIMITER ;

-- =====================================================
-- 创建触发器
-- =====================================================

-- 创建更新统计的触发器
DELIMITER //

CREATE TRIGGER tr_crawler_data_risk_update
AFTER UPDATE ON t_crawler_data
FOR EACH ROW
BEGIN
    IF OLD.risk_level != NEW.risk_level OR OLD.deleted != NEW.deleted THEN
        CALL UpdateRiskStats(NEW.country, CURDATE());
    END IF;
END //

CREATE TRIGGER tr_crawler_data_risk_insert
AFTER INSERT ON t_crawler_data
FOR EACH ROW
BEGIN
    CALL UpdateRiskStats(NEW.country, CURDATE());
END //

DELIMITER ;

-- =====================================================
-- 完成提示
-- =====================================================
SELECT 'Database schema created successfully!' as message;
SELECT 'Total tables created: 13' as table_count;
SELECT 'Views created: 2' as view_count;
SELECT 'Stored procedures created: 1' as procedure_count;
SELECT 'Triggers created: 2' as trigger_count;
