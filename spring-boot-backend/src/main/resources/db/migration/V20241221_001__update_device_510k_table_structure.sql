-- 更新Device510K表结构
-- 删除未使用的字段，添加trade_name字段

-- 添加新的trade_name字段
ALTER TABLE t_device_510k ADD COLUMN trade_name VARCHAR(255);

-- 删除未使用的字段
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS address_1;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS address_2;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS city;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS state;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS zip_code;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS postal_code;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS address;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS advisory_committee;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS advisory_committee_description;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS review_advisory_committee;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS contact;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS contact_person;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS decision_description;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS statement_or_summary;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS decision_result;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS regulation_number;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS meta;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS openfda;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS decision_code;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS clearance_type;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS expedited_review_flag;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS third_party_flag;

-- 删除decisionDate、productCode、deviceGeneralName字段
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS decision_date;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS product_code;
ALTER TABLE t_device_510k DROP COLUMN IF EXISTS device_general_name;

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_device_510k_trade_name ON t_device_510k(trade_name);
CREATE INDEX IF NOT EXISTS idx_device_510k_device_class ON t_device_510k(device_class);
