-- ================================================================================
-- 医疗器械认证监控系统 - 完整数据库初始化脚本（19张表）
-- ================================================================================
-- 版本: v2.0.0
-- 创建日期: 2025-01-20
-- 说明: 包含项目所有19张表的完整结构
-- ================================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE common_db;

-- ================================================================================
-- 1. 认证新闻数据表（1张）
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
-- 2. 医疗设备数据表（6张）
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
    INDEX idx_jd_country (jd_country),
    INDEX idx_country_code (country_code),
    INDEX idx_data_source (data_source),
    INDEX idx_crawl_time (crawl_time)
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
    INDEX idx_risk_level (risk_level),
    INDEX idx_created_date (created_date),
    INDEX idx_data_source (data_source),
    INDEX idx_crawl_time (crawl_time)
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
    INDEX idx_risk_level (risk_level),
    INDEX idx_recalling_firm (recalling_firm(100)),
    INDEX idx_data_source (data_source)
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
    INDEX idx_risk_level (risk_level),
    INDEX idx_date_received (date_received),
    INDEX idx_jd_country (jd_country),
    INDEX idx_data_source (data_source)
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
    INDEX idx_risk_level (risk_level),
    INDEX idx_guidance_status (guidance_status),
    INDEX idx_data_source (data_source)
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
    INDEX idx_risk_level (risk_level),
    INDEX idx_data_source (data_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='海关判例表';

-- ================================================================================
-- 3. 关键词管理表（2张）
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

-- 通用关键词表
DROP TABLE IF EXISTS t_keyword;
CREATE TABLE t_keyword (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(255) NOT NULL UNIQUE COMMENT '关键词内容',
    description VARCHAR(500) COMMENT '关键词描述',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',

    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用关键词表';

-- ================================================================================
-- 4. 任务配置和日志表（5张）
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
    task_id BIGINT COMMENT '任务配置ID',
    task_name VARCHAR(100) COMMENT '任务名称',
    crawler_type VARCHAR(50) COMMENT '爬虫类型',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status VARCHAR(20) COMMENT '执行状态（SUCCESS/FAILED/RUNNING）',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    error_count INT DEFAULT 0 COMMENT '错误数量',
    message TEXT COMMENT '执行结果消息',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL COMMENT '创建时间',

    INDEX idx_task_id (task_id),
    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证新闻任务日志表';

-- 旧版定时爬虫配置表（可能已废弃，保留以兼容旧数据）
DROP TABLE IF EXISTS t_scheduled_crawler_config;
CREATE TABLE t_scheduled_crawler_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawler_name VARCHAR(100) NOT NULL COMMENT '爬虫名称',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    description VARCHAR(500) COMMENT '描述',
    parameters TEXT COMMENT '参数配置（JSON格式）',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',

    INDEX idx_crawler_name (crawler_name),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旧版定时爬虫配置表（已废弃，请使用t_unified_task_config）';

-- ================================================================================
-- 5. AI判断任务表（1张）
-- ================================================================================

-- AI判断任务表
DROP TABLE IF EXISTS ai_judge_task;
CREATE TABLE ai_judge_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id VARCHAR(100) NOT NULL UNIQUE COMMENT '任务ID（UUID）',
    task_type VARCHAR(50) COMMENT '任务类型（CERT_NEWS/DEVICE_DATA）',
    status VARCHAR(20) COMMENT '任务状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）',
    total_count INT COMMENT '总数据量',
    processed_count INT DEFAULT 0 COMMENT '已处理数量',
    related_count INT DEFAULT 0 COMMENT '相关数量（高风险）',
    unrelated_count INT DEFAULT 0 COMMENT '不相关数量（低风险）',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    keyword_count INT DEFAULT 0 COMMENT '提取的关键词数量',
    filter_params TEXT COMMENT '筛选条件（JSON格式）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME COMMENT '创建时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '完成时间',
    update_time DATETIME COMMENT '最后更新时间',

    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI判断任务表';

-- ================================================================================
-- 6. 统计分析表（1张）
-- ================================================================================

-- 每日国家风险数据统计表
DROP TABLE IF EXISTS daily_country_risk_stats;
CREATE TABLE daily_country_risk_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    country VARCHAR(100) NOT NULL COMMENT '国家名称',
    high_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '高风险数据数量',
    medium_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '中风险数据数量',
    low_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '低风险数据数量',
    no_risk_count BIGINT NOT NULL DEFAULT 0 COMMENT '无风险数据数量',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '总数据数量',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',

    UNIQUE KEY uk_stat_date_country (stat_date, country),
    INDEX idx_stat_date (stat_date),
    INDEX idx_country (country),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日国家风险数据统计表';

-- ================================================================================
-- 7. 基础数据表（1张）
-- ================================================================================

-- 国家基础信息表
DROP TABLE IF EXISTS t_country;
CREATE TABLE t_country (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    country_code VARCHAR(2) NOT NULL UNIQUE COMMENT '国家编码（如US/CN/EU）',
    country_name VARCHAR(50) NOT NULL COMMENT '国家名称',
    region VARCHAR(20) COMMENT '所属地区（如北美/亚太）',
    en_name VARCHAR(100) COMMENT '英文缩写（如us）',
    full_name VARCHAR(100) COMMENT '全名',
    create_time DATETIME COMMENT '创建时间',

    INDEX idx_country_code (country_code),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国家基础信息表';

-- ================================================================================
-- 8. 爬虫状态管理表（3张）
-- ================================================================================

-- 爬虫断点续传表
DROP TABLE IF EXISTS t_crawler_checkpoint;
CREATE TABLE t_crawler_checkpoint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawler_type VARCHAR(100) NOT NULL COMMENT '爬虫类型标识',
    search_term VARCHAR(500) COMMENT '搜索条件',
    date_from VARCHAR(20) COMMENT '时间范围开始日期',
    date_to VARCHAR(20) COMMENT '时间范围结束日期',
    current_skip INT NOT NULL DEFAULT 0 COMMENT '当前爬取的偏移量（skip值）',
    total_fetched INT NOT NULL DEFAULT 0 COMMENT '已爬取的总记录数',
    target_total INT COMMENT '目标总记录数（null表示爬取所有数据）',
    batch_size INT NOT NULL COMMENT '批次大小',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '爬取状态（RUNNING/COMPLETED/FAILED/PAUSED）',
    last_updated DATETIME NOT NULL COMMENT '最后更新时间',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    error_message VARCHAR(1000) COMMENT '错误信息',

    INDEX idx_crawler_type (crawler_type),
    INDEX idx_status (status),
    INDEX idx_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫断点续传表';

-- 爬虫状态表
DROP TABLE IF EXISTS t_crawler_state;
CREATE TABLE t_crawler_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawler_name VARCHAR(50) NOT NULL COMMENT '爬虫名称（如：SGS, UL）',
    crawler_type VARCHAR(50) COMMENT '爬虫类型（如：certification, news）',
    last_crawl_time DATETIME COMMENT '最后爬取时间',
    last_crawled_id VARCHAR(255) COMMENT '最后爬取的数据ID或标识',
    last_crawled_title VARCHAR(500) COMMENT '最后爬取的数据标题',
    last_crawled_url VARCHAR(1000) COMMENT '最后爬取的数据URL',
    last_crawled_publish_time DATETIME COMMENT '最后爬取的数据发布时间',
    last_crawled_count INT COMMENT '最后爬取的数据数量',
    total_crawled_count BIGINT DEFAULT 0 COMMENT '累计爬取总数',
    status VARCHAR(20) DEFAULT 'IDLE' COMMENT '爬虫状态（RUNNING/IDLE/ERROR/DISABLED）',
    last_error_message VARCHAR(1000) COMMENT '最后错误信息',
    last_error_time DATETIME COMMENT '最后错误时间',
    consecutive_error_count INT DEFAULT 0 COMMENT '连续错误次数',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_time DATETIME COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    remarks VARCHAR(1000) COMMENT '备注信息',

    INDEX idx_crawler_name (crawler_name),
    INDEX idx_status (status),
    INDEX idx_enabled (enabled),
    INDEX idx_last_crawl_time (last_crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫状态表';

-- 爬虫任务日志表（通用）
DROP TABLE IF EXISTS t_crawler_task_log;
CREATE TABLE t_crawler_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawler_name VARCHAR(100) COMMENT '爬虫名称',
    task_type VARCHAR(50) COMMENT '任务类型',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status VARCHAR(20) COMMENT '执行状态（SUCCESS/FAILED/RUNNING）',
    data_count INT DEFAULT 0 COMMENT '数据数量',
    error_count INT DEFAULT 0 COMMENT '错误数量',
    message TEXT COMMENT '执行消息',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME COMMENT '创建时间',

    INDEX idx_crawler_name (crawler_name),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务日志表';

-- ================================================================================
-- 9. 初始化数据
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

-- 插入通用关键词
INSERT INTO t_keyword (keyword, description, enabled, sort_order, created_time, updated_time) VALUES
('Skin', '皮肤相关', TRUE, 1, NOW(), NOW()),
('Analyzer', '分析仪', TRUE, 2, NOW(), NOW()),
('3D Imaging', '3D成像', TRUE, 3, NOW(), NOW()),
('Medical Device', '医疗器械', TRUE, 4, NOW(), NOW()),
('FDA', '美国食品药品监督管理局', TRUE, 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- 插入国家基础数据
INSERT INTO t_country (country_code, country_name, region, en_name, full_name, create_time) VALUES
('US', '美国', '北美', 'United States', 'United States of America', NOW()),
('EU', '欧盟', '欧洲', 'European Union', 'European Union', NOW()),
('CN', '中国', '亚太', 'China', 'People\'s Republic of China', NOW()),
('JP', '日本', '亚太', 'Japan', 'Japan', NOW()),
('KR', '韩国', '亚太', 'Korea', 'Republic of Korea', NOW()),
('TW', '台湾', '亚太', 'Taiwan', 'Taiwan', NOW())
ON DUPLICATE KEY UPDATE country_name = VALUES(country_name);

-- ================================================================================
-- 完成
-- ================================================================================

-- 查看所有表
SELECT TABLE_NAME, TABLE_COMMENT, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
ORDER BY TABLE_NAME;

-- 显示完成信息
SELECT '数据库初始化完成！共创建19张表。' AS message;
