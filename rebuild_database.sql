-- =====================================================
-- 快速重建数据库脚本
-- 基于当前实体类生成
-- 创建时间: 2025-09-28
-- 版本: 1.0
-- =====================================================

-- 删除现有数据库并重新创建
DROP DATABASE IF EXISTS common_db;
CREATE DATABASE common_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE common_db;

-- =====================================================
-- 核心表结构
-- =====================================================

-- 1. 国家基础信息表
CREATE TABLE t_country (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL UNIQUE,
    country_name VARCHAR(50) NOT NULL,
    region VARCHAR(20),
    en_name VARCHAR(100),
    full_name VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_country_code (country_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 设备510K记录表
CREATE TABLE t_device_510k (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_class VARCHAR(10),
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords TEXT,
    trade_name VARCHAR(255),
    applicant VARCHAR(255),
    country_code VARCHAR(2),
    date_received DATE,
    device_name VARCHAR(255),
    k_number VARCHAR(32) UNIQUE,
    data_source VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    jd_country VARCHAR(20),
    crawl_time DATETIME,
    data_status VARCHAR(20),
    INDEX idx_k_number (k_number),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 设备事件报告表
CREATE TABLE t_device_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_number VARCHAR(64) NOT NULL UNIQUE,
    date_of_event DATE,
    date_received DATE,
    brand_name VARCHAR(255),
    generic_name VARCHAR(255),
    manufacturer_name VARCHAR(255),
    device_class VARCHAR(10),
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords TEXT,
    data_source VARCHAR(50),
    jd_country VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_number (report_number),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 设备召回记录表
CREATE TABLE t_device_recall (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cfres_id VARCHAR(100),
    product_description TEXT,
    recalling_firm VARCHAR(255),
    recall_status VARCHAR(100),
    event_date_posted DATE,
    device_name VARCHAR(255),
    product_code VARCHAR(50),
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords TEXT,
    data_source VARCHAR(50),
    country_code VARCHAR(20) NOT NULL,
    jd_country VARCHAR(20),
    INDEX idx_cfres_id (cfres_id),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 设备注册记录表
CREATE TABLE t_device_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_source VARCHAR(50) NOT NULL,
    jd_country VARCHAR(20) NOT NULL,
    registration_number TEXT NOT NULL,
    fei_number VARCHAR(50),
    manufacturer_name TEXT,
    device_name TEXT,
    proprietary_name LONGTEXT,
    device_class LONGTEXT,
    status_code VARCHAR(100),
    created_date VARCHAR(50),
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords LONGTEXT,
    crawl_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_data_source (data_source),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 医疗文档表
CREATE TABLE t_guidance_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type VARCHAR(20),
    title VARCHAR(500) NOT NULL,
    publication_date DATE,
    topic VARCHAR(255),
    guidance_status VARCHAR(50),
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords TEXT,
    document_url VARCHAR(1000),
    source_url VARCHAR(1000),
    data_source VARCHAR(50),
    jd_country VARCHAR(10),
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_time DATETIME,
    updated_time DATETIME,
    news_type VARCHAR(100),
    description TEXT,
    read_time VARCHAR(50),
    image_url VARCHAR(1000),
    image_alt VARCHAR(500),
    article_index INT,
    INDEX idx_document_type (document_type),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 海关过往判例表
CREATE TABLE t_customs_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(1000),
    case_date DATE,
    hs_code_used TEXT,
    ruling_result TEXT,
    violation_type VARCHAR(50),
    penalty_amount DECIMAL(12,2),
    data_source VARCHAR(255),
    jd_country VARCHAR(20) NOT NULL DEFAULT 'US',
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    keywords TEXT,
    INDEX idx_case_date (case_date),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 关键词表
CREATE TABLE t_keyword (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_keyword (keyword),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 设备匹配关键词表
CREATE TABLE t_devicematch_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL,
    keyword_type ENUM('NORMAL', 'BLACKLIST') NOT NULL DEFAULT 'NORMAL',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_keyword_type (keyword, keyword_type),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 爬虫断点续传表
CREATE TABLE t_crawler_checkpoint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crawler_type VARCHAR(100) NOT NULL,
    search_term VARCHAR(500),
    date_from VARCHAR(20),
    date_to VARCHAR(20),
    current_skip INT NOT NULL DEFAULT 0,
    total_fetched INT NOT NULL DEFAULT 0,
    target_total INT,
    batch_size INT NOT NULL,
    status ENUM('RUNNING', 'COMPLETED', 'FAILED', 'PAUSED') NOT NULL DEFAULT 'RUNNING',
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message VARCHAR(1000),
    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 爬虫状态表
CREATE TABLE t_crawler_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crawler_name VARCHAR(50) NOT NULL,
    crawler_type VARCHAR(50),
    last_crawl_time DATETIME,
    last_crawled_id VARCHAR(255),
    last_crawled_title VARCHAR(500),
    last_crawled_url VARCHAR(1000),
    last_crawled_publish_time DATETIME,
    last_crawled_count INT,
    total_crawled_count BIGINT DEFAULT 0,
    status ENUM('RUNNING', 'IDLE', 'ERROR', 'DISABLED') DEFAULT 'IDLE',
    last_error_message VARCHAR(1000),
    last_error_time DATETIME,
    consecutive_error_count INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_time DATETIME,
    updated_time DATETIME,
    remarks VARCHAR(1000),
    INDEX idx_crawler_name (crawler_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 爬虫数据表
CREATE TABLE t_crawler_data (
    id VARCHAR(50) PRIMARY KEY,
    source_name VARCHAR(255),
    title VARCHAR(500),
    url VARCHAR(1000),
    summary LONGTEXT,
    content LONGTEXT,
    country VARCHAR(100),
    type VARCHAR(100),
    product VARCHAR(500),
    publish_date VARCHAR(100),
    release_date JSON,
    execution_date JSON,
    crawl_time DATETIME,
    status ENUM('NEW', 'PROCESSING', 'PROCESSED', 'ERROR', 'DUPLICATE'),
    is_processed BOOLEAN,
    processed_time DATETIME,
    remarks TEXT,
    related BOOLEAN,
    matched_keywords TEXT,
    risk_level ENUM('HIGH', 'MEDIUM', 'LOW', 'NONE') DEFAULT 'MEDIUM',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_source_name (source_name),
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 每日国家风险统计表
CREATE TABLE daily_country_risk_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    country VARCHAR(100) NOT NULL,
    high_risk_count BIGINT NOT NULL DEFAULT 0,
    medium_risk_count BIGINT NOT NULL DEFAULT 0,
    low_risk_count BIGINT NOT NULL DEFAULT 0,
    no_risk_count BIGINT NOT NULL DEFAULT 0,
    total_count BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_stat_date_country (stat_date, country),
    INDEX idx_stat_date (stat_date),
    INDEX idx_country (country)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
-- 完成提示
-- =====================================================
SELECT 'Database rebuilt successfully!' as message;
SELECT 'Total tables created: 13' as table_count;
SELECT 'Ready for Spring Boot application startup' as status;
