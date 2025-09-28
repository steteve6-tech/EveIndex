-- 创建缺失的表
-- 执行前请先备份数据库！

-- 创建 t_device_pma 表
CREATE TABLE IF NOT EXISTS t_device_pma
(
    id                             BIGINT AUTO_INCREMENT NOT NULL,
    advisory_committee_description VARCHAR(255) NULL,
    ao_statement                   LONGTEXT NULL,
    applicant                      VARCHAR(255) NULL,
    create_time                    datetime NULL,
    data_source                    VARCHAR(50) NULL,
    date_received                  date NULL,
    decision_code                  VARCHAR(50) NULL,
    decision_date                  date NULL,
    device_class                   VARCHAR(10) NULL,
    expedited_review_flag          VARCHAR(10) NULL,
    full_address                   VARCHAR(512) NULL,
    generic_name                   VARCHAR(255) NULL,
    pma_number                     VARCHAR(32) NOT NULL,
    product_code                   VARCHAR(20) NULL,
    supplement_number              VARCHAR(32) NULL,
    supplement_reason              VARCHAR(255) NULL,
    supplement_type                VARCHAR(100) NULL,
    trade_name                     VARCHAR(255) NULL,
    jd_country                     VARCHAR(20) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uk_pma_supplement UNIQUE (pma_number, supplement_number)
);

-- 创建相关索引
CREATE INDEX IF NOT EXISTS idx_pma_number ON t_device_pma (pma_number);
CREATE INDEX IF NOT EXISTS idx_product_code ON t_device_pma (product_code);
CREATE INDEX IF NOT EXISTS idx_decision_date ON t_device_pma (decision_date);
CREATE INDEX IF NOT EXISTS idx_date_received ON t_device_pma (date_received);
CREATE INDEX IF NOT EXISTS idx_jd_country ON t_device_pma (jd_country);

-- 验证表创建结果
DESCRIBE t_device_pma;
