-- ================================================================================
-- 医疗器械认证监控系统 - 数据库初始化脚本
-- ================================================================================
-- 版本: v2.0.0
-- 创建日期: 2025-01-20
-- 说明: 根据项目实际使用的实体类生成的完整数据库表结构
-- ================================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE common_db;

-- ================================================================================
-- 1. 认证新闻数据表
-- ================================================================================

-- 认证新闻数据表（SGS、UL、北测等新闻源）
DROP TABLE IF EXISTS t_crawler_data;
CREATE TABLE t_crawler_data (
    id VARCHAR(50) PRIMARY KEY COMMENT '数据ID',
    source_name VARCHAR(100) COMMENT '数据源名称（SGS/UL/BEICE）',
    title VARCHAR(1000) COMMENT '标题',
    url VARCHAR(1000) COMMENT 'URL链接',
    summary LONGTEXT COMMENT '内容摘要',
    content LONGTEXT COMMENT '详细内容',
    country VARCHAR(100) COMMENT '国家/地区',
    type VARCHAR(100) COMMENT '类型/分类',
    product VARCHAR(500) COMMENT '适用商品/产品',
    publish_date VARCHAR(50) COMMENT '发布时间',
    release_date JSON COMMENT '发布时间列表（JSON格式）',
    execution_date JSON COMMENT '执行时间列表（JSON格式）',
    crawl_time DATETIME COMMENT '爬取时间',
    status VARCHAR(20) COMMENT '数据状态（NEW/PROCESSING/PROCESSED/ERROR/DUPLICATE）',
    is_processed BOOLEAN DEFAULT FALSE COMMENT '是否已处理',
    processed_time DATETIME COMMENT '处理时间',
    remarks TEXT COMMENT '备注',
    related BOOLEAN COMMENT '是否相关',
    matched_keywords TEXT COMMENT '匹配的关键词',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '风险等级（HIGH/MEDIUM/LOW/NONE）',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    INDEX idx_source_name (source_name),
    INDEX idx_publish_date (publish_date),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status (status),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻数据表';

-- ================================================================================
-- 2. 医疗设备数据表
-- ================================================================================

-- 510K申请记录表
DROP TABLE IF EXISTS t_device_510k;
CREATE TABLE t_device_510k (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    device_class VARCHAR(10) COMMENT '设备类别（Class I/II/III）',
    trade_name VARCHAR(255) COMMENT '品牌名称',
    applicant VARCHAR(255) COMMENT '申请人/公司名称',
    country_code VARCHAR(2) COMMENT '国家代码',
    date_received DATE COMMENT 'FDA接收申请日期',
    device_name VARCHAR(255) COMMENT '设备名称',
    k_number VARCHAR(32) UNIQUE COMMENT '510K编号',

    -- 基础字段（继承自BaseDeviceEntity）
    risk_level VARCHAR(10) DEFAULT 'MEDIUM' COMMENT '风险等级（HIGH/MEDIUM/LOW/NONE）',
    keywords TEXT COMMENT '关键词（JSON或文本格式）',
    jd_country VARCHAR(20) NOT NULL COMMENT '数据来源国家',
    data_source VARCHAR(100) COMMENT '数据源（FDA等）',
    remark TEXT COMMENT '备注信息（AI判断原因等）',
    crawl_time DATETIME COMMENT '爬取时间',
    data_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '数据状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',

    INDEX idx_device_name (device_name(100)),
    INDEX idx_k_number (k_number),
    INDEX idx_applicant (applicant(100)),
    INDEX idx_date_received (date_received),
    INDEX idx_risk_level (risk_level),
    INDEX idx_jd_country (jd_country)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='510K申请记录表';

-- 设备注册记录表
DROP TABLE IF EXISTS t_device_registration;
CREATE TABLE t_device_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    registration_number TEXT NOT NULL COMMENT '主要标识符（US: K_number+pma_number, EU: udi_di）',
    fei_number VARCHAR(50) COMMENT '次要标识符（US: fei_number, EU: basic_udi_di）',
    manufacturer_name TEXT COMMENT '制造商名称',
    device_name TEXT COMMENT '设备名称',
    proprietary_name LONGTEXT COMMENT '专有名称/商标名称',
    device_class LONGTEXT COMMENT '设备类别',
    status_code VARCHAR(100) COMMENT '状态码',
    created_date VARCHAR(50) COMMENT '创建日期',

    -- 基础字段
    risk_level VARCHAR(10) DEFAULT 'MEDIUM',
    keywords TEXT,
    jd_country VARCHAR(20) NOT NULL,
    data_source VARCHAR(100),
    remark TEXT,
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME,

    INDEX idx_device_name (device_name(100)),
    INDEX idx_manufacturer (manufacturer_name(100)),
    INDEX idx_jd_country (jd_country),
    INDEX idx_status_code (status_code),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备注册记录表';

-- 设备召回记录表
DROP TABLE IF EXISTS t_device_recall;
CREATE TABLE t_device_recall (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    cfres_id VARCHAR(100) COMMENT '召回事件ID',
    product_description TEXT COMMENT '产品描述',
    recalling_firm VARCHAR(255) COMMENT '召回公司',
    recall_status VARCHAR(100) COMMENT '召回等级（CLASS I/II/III）',
    event_date_posted DATE COMMENT '召回发布日期',
    device_name VARCHAR(255) COMMENT '设备名称',
    product_code VARCHAR(50) COMMENT '产品代码',
    country_code VARCHAR(20) NOT NULL COMMENT '国家代码',

    -- 基础字段
    risk_level VARCHAR(10) DEFAULT 'MEDIUM',
    keywords TEXT,
    jd_country VARCHAR(20) NOT NULL,
    data_source VARCHAR(100),
    remark TEXT,
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME,

    INDEX idx_cfres_id (cfres_id),
    INDEX idx_device_name (device_name(100)),
    INDEX idx_event_date_posted (event_date_posted),
    INDEX idx_country_code (country_code),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备召回记录表';

-- 设备事件报告表
DROP TABLE IF EXISTS t_device_event;
CREATE TABLE t_device_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    report_number VARCHAR(64) NOT NULL UNIQUE COMMENT '报告编号（FDA/EU等）',
    date_of_event DATE COMMENT '事件发生日期',
    date_received DATE COMMENT '接收/发布日期',
    brand_name VARCHAR(255) COMMENT '品牌名称',
    generic_name VARCHAR(255) COMMENT '通用设备名称',
    manufacturer_name VARCHAR(255) COMMENT '制造商名称',
    device_class VARCHAR(10) COMMENT '设备类别',

    -- 基础字段
    risk_level VARCHAR(10) DEFAULT 'MEDIUM',
    keywords TEXT,
    jd_country VARCHAR(20) NOT NULL,
    data_source VARCHAR(100),
    remark TEXT,
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME,

    INDEX idx_report_number (report_number),
    INDEX idx_date_of_event (date_of_event),
    INDEX idx_manufacturer_name (manufacturer_name(100)),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备事件报告表';

-- 指导文档表
DROP TABLE IF EXISTS t_guidance_document;
CREATE TABLE t_guidance_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    document_type VARCHAR(20) COMMENT '文档类型（GUIDANCE/NEWS/NOTICE）',
    title VARCHAR(500) NOT NULL COMMENT '文档标题',
    publication_date DATE COMMENT '发布日期',
    topic VARCHAR(255) COMMENT '话题/主题',
    guidance_status VARCHAR(50) COMMENT '指导状态（Final/Draft/Withdrawn）',
    document_url VARCHAR(1000) COMMENT '指导文档下载URL',
    source_url VARCHAR(1000) COMMENT '原始数据来源页面URL',

    -- EU新闻特有字段
    news_type VARCHAR(100) COMMENT '新闻类型（EU特有）',
    description TEXT COMMENT '文章描述（EU特有）',
    read_time VARCHAR(50) COMMENT '阅读时间（EU特有）',
    image_url VARCHAR(1000) COMMENT '图片URL（EU特有）',
    image_alt VARCHAR(500) COMMENT '图片alt文本（EU特有）',
    article_index INT COMMENT '文章序号（EU特有）',

    -- 基础字段
    risk_level VARCHAR(10) DEFAULT 'MEDIUM',
    keywords TEXT,
    jd_country VARCHAR(20) NOT NULL,
    data_source VARCHAR(100),
    remark TEXT,
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME,

    INDEX idx_title (title(100)),
    INDEX idx_publication_date (publication_date),
    INDEX idx_document_type (document_type),
    INDEX idx_jd_country (jd_country),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指导文档表';

-- 海关判例表
DROP TABLE IF EXISTS t_customs_case;
CREATE TABLE t_customs_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    case_number VARCHAR(1000) COMMENT '判例编号/URL',
    case_date DATE COMMENT '判例日期',
    hs_code_used TEXT COMMENT '裁定使用的HS编码（多个编码用逗号分隔）',
    ruling_result TEXT COMMENT '裁定结果',
    violation_type VARCHAR(50) COMMENT '违规类型',
    penalty_amount DECIMAL(12,2) COMMENT '处罚金额',
    data_source VARCHAR(255) COMMENT '数据来源',

    -- 基础字段
    risk_level VARCHAR(10) DEFAULT 'MEDIUM',
    keywords TEXT,
    jd_country VARCHAR(20) NOT NULL,
    remark TEXT,
    crawl_time DATETIME,
    data_status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME,

    INDEX idx_case_date (case_date),
    INDEX idx_hs_code_used (hs_code_used(100)),
    INDEX idx_jd_country (jd_country),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='海关判例表';

-- ================================================================================
-- 3. 关键词管理表
-- ================================================================================

-- 设备匹配关键词表
DROP TABLE IF EXISTS t_devicematch_keywords;
CREATE TABLE t_devicematch_keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(200) NOT NULL COMMENT '关键词内容',
    keyword_type VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '关键词类型（normal/blacklist/whitelist）',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',

    UNIQUE KEY uk_keyword_type (keyword, keyword_type),
    INDEX idx_keyword_type (keyword_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备匹配关键词表';

-- ================================================================================
-- 4. 任务配置和日志表
-- ================================================================================

-- 统一任务配置表
DROP TABLE IF EXISTS t_unified_task_config;
CREATE TABLE t_unified_task_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    crawler_name VARCHAR(50) NOT NULL COMMENT '爬虫名称',
    country_code VARCHAR(10) COMMENT '国家代码',
    task_type VARCHAR(50) COMMENT '任务类型',
    parameters TEXT COMMENT '参数配置（JSON格式）',
    keywords TEXT COMMENT '关键词列表',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    description VARCHAR(500) COMMENT '任务描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    priority INT DEFAULT 5 COMMENT '优先级',
    timeout_minutes INT DEFAULT 30 COMMENT '超时时间(分钟)',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    last_execution_time DATETIME COMMENT '最后执行时间',
    next_execution_time DATETIME COMMENT '下次执行时间',
    last_execution_status VARCHAR(20) COMMENT '最后执行状态',
    last_execution_result TEXT COMMENT '最后执行结果',
    execution_count INT DEFAULT 0 COMMENT '执行次数',
    success_count INT DEFAULT 0 COMMENT '成功次数',
    failure_count INT DEFAULT 0 COMMENT '失败次数',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    created_by VARCHAR(50) COMMENT '创建者',
    updated_by VARCHAR(50) COMMENT '更新者',
    remark TEXT COMMENT '备注',

    UNIQUE KEY uk_task_name (task_name),
    INDEX idx_crawler_name (crawler_name),
    INDEX idx_country_code (country_code),
    INDEX idx_enabled (enabled),
    INDEX idx_next_execution_time (next_execution_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一任务配置表';

-- 统一任务执行日志表
DROP TABLE IF EXISTS t_unified_task_log;
CREATE TABLE t_unified_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    batch_no VARCHAR(50) COMMENT '执行批次号',
    task_name VARCHAR(100) COMMENT '任务名称',
    crawler_name VARCHAR(50) COMMENT '爬虫名称',
    country_code VARCHAR(10) COMMENT '国家代码',
    status VARCHAR(20) NOT NULL COMMENT '执行状态（SUCCESS/FAILED/RUNNING）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_seconds BIGINT COMMENT '执行时长(秒)',
    crawled_count INT DEFAULT 0 COMMENT '爬取数量',
    saved_count INT DEFAULT 0 COMMENT '保存数量',
    skipped_count INT DEFAULT 0 COMMENT '跳过数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    keywords_used TEXT COMMENT '使用的关键词',
    crawl_params TEXT COMMENT '爬取参数',
    result_message TEXT COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    is_manual BOOLEAN DEFAULT FALSE COMMENT '是否手动触发',
    triggered_by VARCHAR(50) COMMENT '触发者',
    execution_server VARCHAR(100) COMMENT '执行服务器',
    execution_ip VARCHAR(50) COMMENT '执行IP',
    created_at DATETIME COMMENT '创建时间',
    remark TEXT COMMENT '备注',

    INDEX idx_task_id (task_id),
    INDEX idx_crawler_name (crawler_name),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (task_id) REFERENCES t_unified_task_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一任务执行日志表';

-- 认证新闻任务配置表
DROP TABLE IF EXISTS t_cert_news_task_config;
CREATE TABLE t_cert_news_task_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_name VARCHAR(100) NOT NULL UNIQUE COMMENT '任务名称',
    crawler_type VARCHAR(50) NOT NULL COMMENT '爬虫类型（SGS/UL/BEICE）',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    keyword VARCHAR(200) COMMENT '搜索关键词（可选）',
    max_records INT DEFAULT 50 COMMENT '每次爬取的最大记录数',
    description VARCHAR(500) COMMENT '任务描述',
    last_execute_time DATETIME COMMENT '上次执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    last_execute_status VARCHAR(20) COMMENT '上次执行状态（SUCCESS/FAILED）',
    last_execute_message TEXT COMMENT '上次执行结果消息',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',

    INDEX idx_crawler_type (crawler_type),
    INDEX idx_enabled (enabled),
    INDEX idx_next_execute_time (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻任务配置表';

-- 认证新闻任务日志表
DROP TABLE IF EXISTS t_cert_news_task_log;
CREATE TABLE t_cert_news_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务配置ID',
    task_name VARCHAR(100) COMMENT '任务名称',
    crawler_type VARCHAR(50) COMMENT '爬虫类型',
    status VARCHAR(20) NOT NULL COMMENT '执行状态（SUCCESS/FAILED/RUNNING）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_seconds BIGINT COMMENT '执行时长(秒)',
    crawled_count INT DEFAULT 0 COMMENT '爬取数量',
    saved_count INT DEFAULT 0 COMMENT '保存数量',
    error_count INT DEFAULT 0 COMMENT '错误数量',
    result_message TEXT COMMENT '执行结果消息',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME COMMENT '创建时间',

    INDEX idx_task_id (task_id),
    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    FOREIGN KEY (task_id) REFERENCES t_cert_news_task_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻任务日志表';

-- ================================================================================
-- 5. 性能优化索引
-- ================================================================================

-- 510K表索引
CREATE INDEX idx_510k_country_code ON t_device_510k(country_code);
CREATE INDEX idx_510k_data_source ON t_device_510k(data_source);
CREATE INDEX idx_510k_crawl_time ON t_device_510k(crawl_time);

-- 注册记录表索引
CREATE INDEX idx_reg_created_date ON t_device_registration(created_date);
CREATE INDEX idx_reg_data_source ON t_device_registration(data_source);
CREATE INDEX idx_reg_crawl_time ON t_device_registration(crawl_time);

-- 召回记录表索引
CREATE INDEX idx_recall_recalling_firm ON t_device_recall(recalling_firm(100));
CREATE INDEX idx_recall_data_source ON t_device_recall(data_source);

-- 事件报告表索引
CREATE INDEX idx_event_date_received ON t_device_event(date_received);
CREATE INDEX idx_event_jd_country ON t_device_event(jd_country);
CREATE INDEX idx_event_data_source ON t_device_event(data_source);

-- 指导文档表索引
CREATE INDEX idx_guidance_guidance_status ON t_guidance_document(guidance_status);
CREATE INDEX idx_guidance_data_source ON t_guidance_document(data_source);

-- 海关判例表索引
CREATE INDEX idx_customs_data_source ON t_customs_case(data_source);

-- ================================================================================
-- 6. 初始化数据（可选）
-- ================================================================================

-- 插入默认关键词
INSERT INTO t_devicematch_keywords (keyword, keyword_type, enabled, created_time, updated_time) VALUES
('Skin Analyzer', 'normal', TRUE, NOW(), NOW()),
('3D Skin', 'normal', TRUE, NOW(), NOW()),
('Facial Imaging', 'normal', TRUE, NOW(), NOW()),
('Dermatoscope', 'normal', TRUE, NOW(), NOW()),
('Skin Scanner', 'normal', TRUE, NOW(), NOW()),
('AIMYSKIN', 'normal', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- ================================================================================
-- 完成
-- ================================================================================

-- 查看所有表
SHOW TABLES;

-- 查看表结构示例
-- DESCRIBE t_device_510k;
-- DESCRIBE t_unified_task_config;
-- DESCRIBE t_crawler_data;
