CREATE TABLE d_510k_records
(
    id                BIGINT AUTO_INCREMENT  NOT NULL COMMENT '主键ID',
    device_name       VARCHAR(500) NOT NULL COMMENT '设备名称',
    applicant         VARCHAR(300) NULL COMMENT '申请人/公司名称',
    k_number          VARCHAR(50) NULL COMMENT '510(K)编号',
    k_number_url      VARCHAR(1000) NULL COMMENT 'K号详情链接',
    data_source       VARCHAR(100) NULL COMMENT '数据来源',
    country_code      VARCHAR(10) NULL COMMENT '国家代码',
    source_country    VARCHAR(50) NULL COMMENT '来源国家',
    crawl_time        datetime NULL COMMENT '爬取时间',
    data_status       VARCHAR(20) NULL COMMENT '数据状态',
    page_num          INT NULL COMMENT '页码（爬取时的页码）',
    decision_date_str VARCHAR(20) NULL COMMENT '原始决策日期字符串（用于调试）',
    create_time       datetime DEFAULT NOW() NULL COMMENT '创建时间',
    update_time       datetime DEFAULT NOW() NULL COMMENT '更新时间',
    remarks           VARCHAR(1000) NULL COMMENT '备注',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='FDA 510K记录表';

CREATE TABLE daily_country_risk_stats
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    country           VARCHAR(100) NOT NULL,
    created_at        datetime     NOT NULL,
    deleted           BIT(1)       NOT NULL,
    high_risk_count   BIGINT       NOT NULL,
    low_risk_count    BIGINT       NOT NULL,
    medium_risk_count BIGINT       NOT NULL,
    no_risk_count     BIGINT       NOT NULL,
    stat_date         date         NOT NULL,
    total_count       BIGINT       NOT NULL,
    updated_at        datetime     NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE eu_safety_gate_alerts
(
) COMMENT ='欧盟安全门预警系统数据表';

CREATE TABLE t_country
(
    id           BIGINT AUTO_INCREMENT   NOT NULL COMMENT '主键',
    country_code CHAR(2)     NOT NULL COMMENT '国家编码（如US/CN/EU）',
    country_name VARCHAR(50) NOT NULL COMMENT '国家名称',
    region       VARCHAR(20) NULL COMMENT '所属地区（如北美/亚太）',
    create_time  timestamp DEFAULT NOW() NULL COMMENT '创建时间',
    en_name      VARCHAR(100) NULL COMMENT '英文缩写（如us）',
    full_name    VARCHAR(100) NULL COMMENT '全名',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='国家基础信息表';

CREATE TABLE t_crawler_checkpoint
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    crawler_type  VARCHAR(100)  NOT NULL,
    search_term   VARCHAR(500) NULL,
    date_from     VARCHAR(20) NULL,
    date_to       VARCHAR(20) NULL,
    current_skip  INT DEFAULT 0 NOT NULL,
    total_fetched INT DEFAULT 0 NOT NULL,
    target_total  INT           NOT NULL,
    batch_size    INT           NOT NULL,
    status        ENUM          NOT NULL,
    last_updated  datetime      NOT NULL,
    created_time  datetime      NOT NULL,
    error_message VARCHAR(1000) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_crawler_data
(
    id               VARCHAR(36) NOT NULL,
    content          LONGTEXT NULL COMMENT '详细内容',
    country          VARCHAR(255) NULL,
    crawl_time       datetime NULL,
    created_at       datetime NULL,
    deleted          INT NULL,
    is_processed     BIT(1) NULL,
    processed_time   datetime NULL,
    publish_date     VARCHAR(255) NULL,
    release_date     JSON NULL COMMENT '发布时间列表（JSON格式）',
    execution_date   JSON NULL COMMENT '执行时间列表（JSON格式）',
    remarks          LONGTEXT NULL COMMENT '备注信息',
    source_name      VARCHAR(255) NULL,
    status           ENUM NULL,
    summary          LONGTEXT NULL COMMENT '内容摘要',
    title            VARCHAR(255) NULL,
    type             VARCHAR(255) NULL,
    product          VARCHAR(500) NULL COMMENT '适用商品/产品',
    updated_at       datetime NULL,
    url              VARCHAR(255) NULL,
    related          TINYINT(1)   NULL COMMENT '是否相关：true-相关，false-不相关，null-未确定',
    matched_keywords LONGTEXT NULL,
    risk_level       ENUM NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_crawler_state
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    consecutive_error_count   INT NULL,
    crawler_name              VARCHAR(50) NOT NULL,
    crawler_type              VARCHAR(50) NULL,
    created_time              datetime NULL,
    enabled                   BIT(1) NULL,
    last_crawl_time           datetime NULL,
    last_crawled_count        INT NULL,
    last_crawled_id           VARCHAR(255) NULL,
    last_crawled_publish_time datetime NULL,
    last_crawled_title        VARCHAR(500) NULL,
    last_crawled_url          VARCHAR(1000) NULL,
    last_error_message        VARCHAR(1000) NULL,
    last_error_time           datetime NULL,
    remarks                   VARCHAR(1000) NULL,
    status                    ENUM NULL,
    total_crawled_count       BIGINT NULL,
    updated_time              datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_customs_case
(
    id             BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键',
    case_number    VARCHAR(50) NULL COMMENT '判例编号',
    case_date      date NULL COMMENT '判例日期',
    hs_code_used   LONGTEXT NULL COMMENT '裁定使用的HS编码（多个编码用逗号分隔）',
    ruling_result  LONGTEXT NULL COMMENT '裁定结果（如归类认定/处罚决定）',
    violation_type VARCHAR(50) NULL COMMENT '违规类型（如标签不符/归类错误）',
    penalty_amount DECIMAL(12, 2) NULL COMMENT '处罚金额（如有）',
    crawl_time     datetime NULL,
    data_source    VARCHAR(255) NULL,
    data_status    VARCHAR(20) NULL,
    jd_country     VARCHAR(10) NULL,
    keywords       JSON NULL,
    risk_level     ENUM NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='海关过往判例表';

CREATE TABLE t_data_change_log
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    change_description   VARCHAR(255) NULL,
    change_type          TINYINT NULL,
    created_at           datetime NULL,
    deleted              INT NULL,
    entity_id            BIGINT NULL,
    entity_type          VARCHAR(255) NULL,
    field_name           VARCHAR(255) NULL,
    is_notified          BIT(1) NULL,
    new_value            VARCHAR(255) NULL,
    notification_sent_at datetime NULL,
    old_value            VARCHAR(255) NULL,
    source_name          VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_device_510k
(
    id                BIGINT AUTO_INCREMENT        NOT NULL COMMENT '主键ID',
    device_class      VARCHAR(10) NULL COMMENT '设备类别',
    risk_level        VARCHAR(10) DEFAULT 'MEDIUM' NULL COMMENT '风险等级',
    keywords          LONGTEXT NULL COMMENT '关键词数组（JSON格式存储）',
    trade_name        VARCHAR(255) NULL COMMENT '品牌名称',
    applicant         VARCHAR(255) NULL COMMENT '申请人',
    country_code      VARCHAR(2) NULL COMMENT '国家代码',
    date_received     date NULL COMMENT '接收日期',
    device_name       VARCHAR(255) NULL COMMENT '设备名称',
    k_number          VARCHAR(32) NULL COMMENT 'K号',
    data_source       VARCHAR(50) NULL COMMENT '数据源',
    create_time       timestamp   DEFAULT NOW() NULL COMMENT '创建时间（自动生成）',
    jd_country        VARCHAR(20) NULL COMMENT '京东国家',
    crawl_time        timestamp NULL COMMENT '爬取时间',
    data_status       VARCHAR(20) NULL COMMENT '数据状态',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='设备510K记录表 - 存储FDA 510K设备审批数据';

CREATE TABLE t_device_event
(
    id                               BIGINT AUTO_INCREMENT        NOT NULL,
    report_number                    VARCHAR(64) NOT NULL COMMENT '报告编号 (FDA: report_number, EU: alert_number)',
    date_of_event                    date NULL COMMENT '事件发生日期',
    date_received                    date NULL COMMENT '接收日期 (FDA: date_received, EU: publication_date)',
    brand_name                       VARCHAR(255) NULL COMMENT '品牌名称 (FDA: device.manufacturer_name, EU: brand)',
    generic_name                     VARCHAR(255) NULL COMMENT '通用名称 (FDA: device.generic_name, EU: product)',
    manufacturer_name                VARCHAR(255) NULL COMMENT '制造商名称',
    device_class                     VARCHAR(10) NULL COMMENT '设备类别',
    risk_level                       VARCHAR(10) DEFAULT 'MEDIUM' NULL COMMENT '风险等级 (LOW/MEDIUM/HIGH)',
    keywords                         LONGTEXT NULL COMMENT '关键词（JSON字符串或分号分隔）',
    data_source                      VARCHAR(50) NULL COMMENT '数据源',
    jd_country                       VARCHAR(20) NULL COMMENT '判定国家（如 US/CN/EU 等）',
    create_time                      datetime    DEFAULT NOW() NULL COMMENT '创建时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='设备事件报告表 - 支持FDA和EU数据源';


CREATE TABLE t_device_recall
(
    id                  BIGINT AUTO_INCREMENT        NOT NULL COMMENT '主键ID',
    cfres_id            VARCHAR(100) NULL COMMENT '召回事件ID（从FDA网站URL提取）',
    product_description LONGTEXT NULL COMMENT '产品描述',
    recalling_firm      VARCHAR(255) NULL COMMENT '召回公司',
    recall_status       VARCHAR(100) NULL COMMENT '召回等级（CLASS I/II/III）',
    event_date_posted   date NULL COMMENT '召回发布日期',
    device_name         VARCHAR(255) NULL COMMENT '设备名称（复用产品描述）',
    product_code        VARCHAR(50) NULL COMMENT '产品代码（D_recall设置为空）',
    risk_level          VARCHAR(10) DEFAULT 'MEDIUM' NULL COMMENT '风险等级 (LOW/MEDIUM/HIGH)',
    keywords            LONGTEXT NULL COMMENT '关键词（JSON格式存储）',
    data_source         VARCHAR(50) NULL COMMENT '数据源',
    country_code        VARCHAR(20) NOT NULL COMMENT '国家代码',
    jd_country          VARCHAR(20) NULL COMMENT '数据适用国家',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='医疗器械召回记录表 - 适配D_recall爬虫数据结构';

CREATE TABLE t_device_registration
(
    id                  BIGINT AUTO_INCREMENT     NOT NULL COMMENT '主键ID',
    data_source         VARCHAR(50) NOT NULL COMMENT '数据源（US_FDA, EU_EUDAMED）',
    jd_country          VARCHAR(20) NOT NULL COMMENT '数据源国家（US, EU）',
    registration_number LONGTEXT    NOT NULL COMMENT '主要标识符（US: K_number+pma_number, EU: udi_di）',
    fei_number          VARCHAR(50) NULL COMMENT '次要标识符（US: fei_number, EU: basic_udi_di）',
    manufacturer_name   LONGTEXT NULL COMMENT '制造商名称',
    device_name         LONGTEXT NULL COMMENT '设备名称',
    proprietary_name    LONGTEXT NULL COMMENT '专有名称/商标名称',
    device_class        LONGTEXT NULL COMMENT '设备类别',
    status_code         VARCHAR(100) NULL COMMENT '状态码',
    created_date        VARCHAR(50) NULL COMMENT '创建日期',
    risk_level          ENUM     DEFAULT 'MEDIUM' NULL COMMENT '风险等级评估',
    keywords            LONGTEXT NULL COMMENT '关键词（JSON数组）',
    crawl_time          datetime NULL COMMENT '爬取时间',
    create_time         datetime DEFAULT NOW() NULL COMMENT '创建时间',
    update_time         datetime DEFAULT NOW() NULL COMMENT '更新时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='设备注册记录共有数据表';

CREATE TABLE t_devicematch_keywords
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_time datetime     NOT NULL,
    enabled      BIT(1)       NOT NULL,
    keyword      VARCHAR(255) NOT NULL,
    keyword_type ENUM         NOT NULL,
    updated_time datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_guidance_document
(
    id               BIGINT AUTO_INCREMENT        NOT NULL COMMENT '主键ID',
    title            VARCHAR(500) NOT NULL COMMENT '指导文档标题',
    publication_date date NULL COMMENT '发布日期',
    topic            VARCHAR(255) NULL COMMENT '话题/主题',
    guidance_status  VARCHAR(50) NULL COMMENT '指导状态（Final, Draft, Withdrawn等）',
    risk_level       ENUM        DEFAULT 'MEDIUM' NULL COMMENT '风险等级：HIGH-高, MEDIUM-中, LOW-低',
    keywords         JSON NULL COMMENT '关键词数组，JSON格式存储',
    document_url     VARCHAR(1000) NULL COMMENT '指导文档URL',
    source_url       VARCHAR(1000) NULL COMMENT '数据来源URL',
    crawl_time       datetime NULL COMMENT '爬取时间',
    data_status      VARCHAR(20) DEFAULT 'ACTIVE' NULL COMMENT '数据状态（ACTIVE, INACTIVE等）',
    created_time     datetime    DEFAULT NOW() NULL COMMENT '创建时间',
    updated_time     datetime    DEFAULT NOW() NULL COMMENT '更新时间',
    data_source      VARCHAR(50) NULL,
    jd_country       VARCHAR(10) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='指导文档表 - 存储FDA等机构的指导文档，包含风险等级和关键词';

CREATE TABLE t_guidance_document_2
(
    id               BIGINT NULL,
    title            VARCHAR(500) NULL,
    publication_date date NULL,
    topic            VARCHAR(255) NULL,
    guidance_status  VARCHAR(50) NULL,
    document_url     VARCHAR(1000) NULL,
    source_url       VARCHAR(1000) NULL,
    crawl_time       datetime NULL,
    data_status      VARCHAR(20) NULL,
    created_time     datetime NULL,
    updated_time     datetime NULL,
    data_source      VARCHAR(50) NULL,
    jd_country       VARCHAR(10) NULL
);

CREATE TABLE t_keyword
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_time  datetime     NOT NULL,
    `description` VARCHAR(255) NULL,
    enabled       BIT(1)       NOT NULL,
    keyword       VARCHAR(255) NOT NULL,
    sort_order    INT NULL,
    updated_time  datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_notification
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    content           VARCHAR(255) NULL,
    created_at        datetime NULL,
    deleted           INT NULL,
    is_read           BIT(1) NULL,
    notification_type TINYINT NULL,
    priority          TINYINT NULL,
    read_time         datetime NULL,
    recipient         VARCHAR(255) NULL,
    title             VARCHAR(255) NULL,
    updated_at        datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_product
(
    id                 BIGINT AUTO_INCREMENT   NOT NULL COMMENT '主键',
    product_name       VARCHAR(100) NOT NULL COMMENT '产品名称（如Visia皮肤分析仪）',
    brand              VARCHAR(50) NULL COMMENT '品牌',
    model              VARCHAR(50) NULL COMMENT '型号',
    product_type       VARCHAR(30) NULL COMMENT '产品类型（如家用/专业医疗设备）',
    is_active          INT NULL,
    create_time        timestamp DEFAULT NOW() NULL COMMENT '创建时间',
    update_time        timestamp DEFAULT NOW() NULL COMMENT '更新时间',
    applicant_name     VARCHAR(100) NULL,
    brand_name         VARCHAR(100) NULL,
    data_source        VARCHAR(30) NULL,
    device_code        VARCHAR(50) NULL,
    device_description LONGTEXT NULL,
    remarks            LONGTEXT NULL,
    risk_level         VARCHAR(20) NULL,
    source_data_id     BIGINT NULL,
    usage_scope        LONGTEXT NULL,
    device_class       VARCHAR(20) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='产品基础信息表';

CREATE TABLE t_product_category_code
(
    id               BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键',
    product_id       BIGINT NOT NULL COMMENT '产品ID（关联t_product）',
    country_id       BIGINT NOT NULL COMMENT '国家ID（关联t_country）',
    hs_code          VARCHAR(20) NULL COMMENT 'HS编码（如9018、8543.70）',
    reg_code         VARCHAR(20) NULL COMMENT '监管分类代码（如FDA的Product Code）',
    code_description VARCHAR(200) NULL COMMENT '代码描述',
    is_default       BIT(1) NULL,
    effective_date   date NULL COMMENT '生效日期',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='产品分类代码表（HS编码+监管分类）';

CREATE TABLE t_product_recall
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    affected_quantity    INT NULL,
    authority_notice_url VARCHAR(500) NULL,
    cfres_id             VARCHAR(100) NULL,
    country_code         VARCHAR(20) NOT NULL,
    event_date_initiated date NULL,
    firm_fei_number      VARCHAR(50) NULL,
    k_numbers            LONGTEXT NULL,
    pma_numbers          LONGTEXT NULL,
    product_code         VARCHAR(100) NULL,
    product_description  LONGTEXT NULL,
    recall_date          date NULL,
    recall_level         VARCHAR(50) NULL,
    recall_number        VARCHAR(100) NULL,
    recall_reason        LONGTEXT NULL,
    recall_status        VARCHAR(100) NULL,
    recalling_firm       VARCHAR(500) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_product_registration
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    country_id        BIGINT      NOT NULL,
    create_time       datetime NULL,
    expire_date       date NULL,
    issuing_authority VARCHAR(50) NULL,
    product_id        BIGINT      NOT NULL,
    reg_category      VARCHAR(30) NULL,
    reg_date          date NULL,
    reg_number        VARCHAR(50) NULL,
    reg_status        VARCHAR(20) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_regulation_notice
(
    id                    BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键',
    country_id            BIGINT       NOT NULL COMMENT '国家ID（关联t_country）',
    notice_title          VARCHAR(200) NOT NULL COMMENT '法规通知标题',
    notice_number         VARCHAR(50) NULL COMMENT '法规编号（如FDA的Federal Register编号）',
    `发布机构`            VARCHAR(100) NULL COMMENT '发布机构（如FDA/NMPA/EU Commission）',
    effective_date        date NULL COMMENT '生效日期',
    content_summary       LONGTEXT NULL COMMENT '内容摘要（如分类调整/要求更新）',
    related_hs_codes      VARCHAR(200) NULL COMMENT '关联HS编码（逗号分隔）',
    related_product_types VARCHAR(200) NULL COMMENT '关联产品类型',
    notice_url            VARCHAR(255) NULL COMMENT '官方通知URL',
    publish_time          timestamp NULL COMMENT '发布时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) COMMENT ='相关法规通知表';

CREATE TABLE t_risk_score
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    calculate_time   datetime NOT NULL,
    country_id       BIGINT   NOT NULL,
    created_at       datetime NOT NULL,
    deleted          BIT(1)   NOT NULL,
    dimension_scores LONGTEXT NULL,
    product_id       BIGINT   NOT NULL,
    remarks          VARCHAR(500) NULL,
    risk_level       VARCHAR(20) NULL,
    total_score      INT      NOT NULL,
    updated_at       datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_risk_score_rule
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    condition_expression VARCHAR(500) NOT NULL,
    country_id           BIGINT NULL,
    created_at           datetime     NOT NULL,
    deleted              BIT(1)       NOT NULL,
    `description`        VARCHAR(200) NULL,
    dimension            VARCHAR(50)  NOT NULL,
    is_enabled           BIT(1)       NOT NULL,
    priority             INT          NOT NULL,
    score                INT          NOT NULL,
    updated_at           datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_risk_weight_config
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    country_id     BIGINT NULL,
    created_at     datetime    NOT NULL,
    deleted        BIT(1)      NOT NULL,
    `description`  VARCHAR(200) NULL,
    dimension_name VARCHAR(50) NOT NULL,
    is_enabled     BIT(1)      NOT NULL,
    updated_at     datetime NULL,
    weight         INT         NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_source
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    config_json        VARCHAR(255) NULL,
    crawl_status       VARCHAR(255) NULL,
    created_at         datetime NULL,
    deleted            INT NULL,
    `description`      VARCHAR(255) NULL,
    error_count        INT NULL,
    is_active          BIT(1) NULL,
    last_crawl_time    datetime NULL,
    last_error_message VARCHAR(255) NULL,
    source_name        VARCHAR(255) NULL,
    source_type        TINYINT NULL,
    source_url         VARCHAR(255) NULL,
    update_frequency   TINYINT NULL,
    updated_at         datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_standard
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    compliance_deadline date NULL,
    created_at          datetime NULL,
    deleted             INT NULL,
    `description`       LONGTEXT NULL,
    download_url        VARCHAR(255) NULL,
    effective_date      VARCHAR(255) NULL,
    frequency_bands     VARCHAR(255) NULL,
    is_monitored        BIT(1) NULL,
    keywords            VARCHAR(255) NULL,
    matched_profiles    VARCHAR(255) NULL,
    power_limits        VARCHAR(255) NULL,
    product_types       VARCHAR(255) NULL,
    published_date      VARCHAR(255) NULL,
    raw_excerpt         LONGTEXT NULL,
    regulatory_impact   ENUM NULL,
    risk_level          ENUM NULL,
    risk_score DOUBLE NULL,
    scope               LONGTEXT NULL,
    standard_number     VARCHAR(255) NULL,
    standard_status     ENUM NULL,
    test_methods        LONGTEXT NULL,
    title               VARCHAR(255) NULL,
    transition_end      VARCHAR(255) NULL,
    updated_at          datetime NULL,
    version             VARCHAR(255) NULL,
    countries           JSON NULL,
    country             VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_system_log
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime NULL,
    deleted        INT NULL,
    details        VARCHAR(255) NULL,
    execution_time BIGINT NULL,
    ip_address     VARCHAR(255) NULL,
    log_level      TINYINT NULL,
    log_status     TINYINT NULL,
    log_type       TINYINT NULL,
    message        VARCHAR(255) NULL,
    user_agent     VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE t_task
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    assignee         VARCHAR(255) NOT NULL,
    created_at       datetime     NOT NULL,
    deleted          INT          NOT NULL,
    `description`    LONGTEXT NULL,
    due_date         VARCHAR(255) NULL,
    priority         ENUM NULL,
    related_standard VARCHAR(255) NULL,
    remarks          LONGTEXT NULL,
    status           ENUM         NOT NULL,
    title            VARCHAR(255) NOT NULL,
    updated_at       datetime     NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE t_devicematch_keywords
    ADD CONSTRAINT UK8p8xajkhv7p0q1ejeqx1pfi9g UNIQUE (keyword, keyword_type);

ALTER TABLE t_keyword
    ADD CONSTRAINT UK_f6re6v8jc7adwwctnqq4t4ut8 UNIQUE (keyword);

ALTER TABLE daily_country_risk_stats
    ADD CONSTRAINT UKkc47muhrg5l1de7pglbj786jn UNIQUE (stat_date, country);

ALTER TABLE t_device_event
    ADD CONSTRAINT report_number UNIQUE (report_number);

ALTER TABLE t_country
    ADD CONSTRAINT uk_code UNIQUE (country_code);

ALTER TABLE d_510k_records
    ADD CONSTRAINT uk_k_number UNIQUE (k_number);

ALTER TABLE t_device_510k
    ADD CONSTRAINT uk_k_number UNIQUE (k_number);


ALTER TABLE t_guidance_document
    ADD CONSTRAINT uk_title_url UNIQUE (title, document_url);

CREATE INDEX idx_applicant ON t_device_510k (applicant);

CREATE INDEX idx_applicant ON t_device_510k (applicant);

CREATE INDEX idx_brand_name ON t_device_event (brand_name);

CREATE INDEX idx_case_date ON t_customs_case (case_date);

CREATE INDEX idx_cfres_id ON t_device_recall (cfres_id);

CREATE INDEX idx_country_code ON t_device_recall (country_code);

CREATE INDEX idx_country_code ON t_device_recall (country_code);

CREATE INDEX idx_crawl_time ON t_guidance_document (crawl_time);

CREATE INDEX idx_crawl_time ON t_guidance_document (crawl_time);

CREATE INDEX idx_crawl_time ON t_guidance_document (crawl_time);

CREATE INDEX idx_crawl_time ON t_guidance_document (crawl_time);

CREATE INDEX idx_create_time ON t_device_registration (create_time);

CREATE INDEX idx_create_time ON t_device_registration (create_time);

CREATE INDEX idx_create_time ON t_device_registration (create_time);

CREATE INDEX idx_create_time ON t_device_registration (create_time);

CREATE INDEX idx_data_source ON t_device_registration (data_source);

CREATE INDEX idx_data_source ON t_device_registration (data_source);

CREATE INDEX idx_data_source ON t_device_registration (data_source);

CREATE INDEX idx_data_source ON t_device_registration (data_source);

CREATE INDEX idx_data_source_country ON t_device_registration (data_source, jd_country);

CREATE INDEX idx_data_status ON t_guidance_document (data_status);

CREATE INDEX idx_data_status ON t_guidance_document (data_status);







CREATE INDEX idx_device_class ON t_device_registration (device_class);

CREATE INDEX idx_device_class ON t_device_registration (device_class);

CREATE INDEX idx_device_class ON t_device_registration (device_class);

CREATE INDEX idx_device_name ON t_device_registration (device_name);

CREATE INDEX idx_device_name ON t_device_registration (device_name);

CREATE INDEX idx_device_name ON t_device_registration (device_name);

CREATE INDEX idx_effective_date ON t_regulation_notice (effective_date);

CREATE INDEX idx_event_date_posted ON t_device_recall (event_date_posted);

CREATE INDEX idx_event_type ON t_device_event (event_type);

CREATE INDEX idx_fei_number ON t_device_registration (fei_number);

CREATE INDEX idx_guidance_document_risk_level ON t_guidance_document (risk_level);

CREATE INDEX idx_hs_code ON t_product_category_code (hs_code);

CREATE INDEX idx_jd_country ON t_device_registration (jd_country);

CREATE INDEX idx_jd_country ON t_device_registration (jd_country);

CREATE INDEX idx_jd_country ON t_device_registration (jd_country);

CREATE INDEX idx_manufacturer_name ON t_device_registration (manufacturer_name);

CREATE INDEX idx_manufacturer_name ON t_device_registration (manufacturer_name);

CREATE INDEX idx_name ON t_product (product_name);




CREATE INDEX idx_proprietary_name ON t_device_registration (proprietary_name);

CREATE INDEX idx_recall_status ON t_device_recall (recall_status);

CREATE INDEX idx_recalling_firm ON t_device_recall (recalling_firm);

CREATE INDEX idx_registration_fei ON t_device_registration (registration_number, fei_number);

CREATE INDEX idx_registration_number ON t_device_registration (registration_number);

CREATE INDEX idx_regulation_number ON t_device_510k (regulation_number);


CREATE INDEX idx_risk_level ON t_device_registration (risk_level);

CREATE INDEX idx_risk_level ON t_device_registration (risk_level);

CREATE INDEX idx_risk_level ON t_device_registration (risk_level);

CREATE INDEX idx_risk_level ON t_device_registration (risk_level);

CREATE INDEX idx_status_code ON t_device_registration (status_code);

CREATE INDEX idx_title ON t_guidance_document (title);

CREATE INDEX idx_trade_name ON t_device_510k (trade_name);

CREATE INDEX idx_update_time ON t_device_registration (update_time);

ALTER TABLE t_product_recall
    ADD CONSTRAINT FK1l9o9qaylbd0ubvd183c3xoak FOREIGN KEY (country_code) REFERENCES t_country (country_code) ON DELETE NO ACTION;

CREATE INDEX FK1l9o9qaylbd0ubvd183c3xoak ON t_product_recall (country_code);

ALTER TABLE t_product_registration
    ADD CONSTRAINT FK1ob39l17sm7yd6wgcf9b3j5cs FOREIGN KEY (product_id) REFERENCES t_product (id) ON DELETE NO ACTION;

CREATE INDEX FK1ob39l17sm7yd6wgcf9b3j5cs ON t_product_registration (product_id);

ALTER TABLE t_product_registration
    ADD CONSTRAINT FKa9esgc55yti1rqxpvsn7259tv FOREIGN KEY (country_id) REFERENCES t_country (id) ON DELETE NO ACTION;

CREATE INDEX FKa9esgc55yti1rqxpvsn7259tv ON t_product_registration (country_id);

ALTER TABLE t_product_category_code
    ADD CONSTRAINT t_product_category_code_ibfk_1 FOREIGN KEY (product_id) REFERENCES t_product (id) ON DELETE NO ACTION;

CREATE INDEX product_id ON t_product_category_code (product_id);

ALTER TABLE t_product_category_code
    ADD CONSTRAINT t_product_category_code_ibfk_2 FOREIGN KEY (country_id) REFERENCES t_country (id) ON DELETE NO ACTION;

CREATE INDEX country_id ON t_regulation_notice (country_id);

ALTER TABLE t_regulation_notice
    ADD CONSTRAINT t_regulation_notice_ibfk_1 FOREIGN KEY (country_id) REFERENCES t_country (id) ON DELETE NO ACTION;

CREATE INDEX country_id ON t_regulation_notice (country_id);

CREATE VIEW v_eu_alerts_by_category AS
select `common_db`.`eu_safety_gate_alerts`.`category`                                            AS `category`,
       count(0)                                                                                  AS `alert_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'HIGH') then 1 end)) AS `high_risk_count`,
       count((case
                  when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'MEDIUM')
                      then 1 end))                                                               AS `medium_risk_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'LOW') then 1 end))  AS `low_risk_count`,
       max(`common_db`.`eu_safety_gate_alerts`.`publication_date`)                               AS `latest_alert_date`
from `common_db`.`eu_safety_gate_alerts`
where ((`common_db`.`eu_safety_gate_alerts`.`data_status` = 'ACTIVE') and
       (`common_db`.`eu_safety_gate_alerts`.`category` is not null))
group by `common_db`.`eu_safety_gate_alerts`.`category`
order by `alert_count` desc;

-- comment on column v_eu_alerts_by_category.category not supported: 产品类别

-- comment on column v_eu_alerts_by_category.latest_alert_date not supported: 发布日期;

CREATE VIEW v_eu_alerts_by_country AS
select `common_db`.`eu_safety_gate_alerts`.`country`                                             AS `country`,
       count(0)                                                                                  AS `alert_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'HIGH') then 1 end)) AS `high_risk_count`,
       count((case
                  when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'MEDIUM')
                      then 1 end))                                                               AS `medium_risk_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'LOW') then 1 end))  AS `low_risk_count`,
       max(`common_db`.`eu_safety_gate_alerts`.`publication_date`)                               AS `latest_alert_date`
from `common_db`.`eu_safety_gate_alerts`
where (`common_db`.`eu_safety_gate_alerts`.`data_status` = 'ACTIVE')
group by `common_db`.`eu_safety_gate_alerts`.`country`
order by `alert_count` desc;

-- comment on column v_eu_alerts_by_country.country not supported: 产品来源国家

-- comment on column v_eu_alerts_by_country.latest_alert_date not supported: 发布日期;

CREATE VIEW v_eu_alerts_by_risk_type AS
select `common_db`.`eu_safety_gate_alerts`.`risk_type`                                           AS `risk_type`,
       count(0)                                                                                  AS `alert_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'HIGH') then 1 end)) AS `high_risk_count`,
       count((case
                  when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'MEDIUM')
                      then 1 end))                                                               AS `medium_risk_count`,
       count((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'LOW') then 1 end))  AS `low_risk_count`,
       max(`common_db`.`eu_safety_gate_alerts`.`publication_date`)                               AS `latest_alert_date`
from `common_db`.`eu_safety_gate_alerts`
where ((`common_db`.`eu_safety_gate_alerts`.`data_status` = 'ACTIVE') and
       (`common_db`.`eu_safety_gate_alerts`.`risk_type` is not null))
group by `common_db`.`eu_safety_gate_alerts`.`risk_type`
order by `alert_count` desc;

-- comment on column v_eu_alerts_by_risk_type.risk_type not supported: 风险类型

-- comment on column v_eu_alerts_by_risk_type.latest_alert_date not supported: 发布日期;

CREATE VIEW v_eu_alerts_monthly AS
select
        year (`common_db`.`eu_safety_gate_alerts`.`publication_date`) AS `year`,
        month (`common_db`.`eu_safety_gate_alerts`.`publication_date`) AS `month`,
        count (0) AS `alert_count`,
        count ((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'HIGH') then 1 end)) AS `high_risk_count`,
        count ((case
        when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'MEDIUM')
        then 1 end)) AS `medium_risk_count`,
        count ((case when (`common_db`.`eu_safety_gate_alerts`.`risk_level` = 'LOW') then 1 end)) AS `low_risk_count`
        from `common_db`.`eu_safety_gate_alerts`
        where ((`common_db`.`eu_safety_gate_alerts`.`data_status` = 'ACTIVE') and
        (`common_db`.`eu_safety_gate_alerts`.`publication_date` is not null))
        group by year (`common_db`.`eu_safety_gate_alerts`.`publication_date`),
        month (`common_db`.`eu_safety_gate_alerts`.`publication_date`)
        order by `year` desc, `month` desc;