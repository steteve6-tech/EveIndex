-- 快速修复远程数据库表结构
-- 解决 "too many columns" 错误
-- 执行前请先备份数据库！

-- 1. 删除 t_device_510k 表的已删除字段
ALTER TABLE t_device_510k 
DROP COLUMN IF EXISTS decision_date,
DROP COLUMN IF EXISTS device_url,
DROP COLUMN IF EXISTS product_code,
DROP COLUMN IF EXISTS regulation_number,
DROP COLUMN IF EXISTS openfda;

-- 2. 删除 d_510k_records 表的已删除字段
ALTER TABLE d_510k_records 
DROP COLUMN IF EXISTS decision_date,
DROP COLUMN IF EXISTS device_url;

-- 3. 删除 t_device_event 表的已删除字段（41个字段）
ALTER TABLE t_device_event 
DROP COLUMN IF EXISTS adverse_event_flag,
DROP COLUMN IF EXISTS event_type,
DROP COLUMN IF EXISTS brands_list,
DROP COLUMN IF EXISTS contact_person,
DROP COLUMN IF EXISTS contact_phone,
DROP COLUMN IF EXISTS date_added,
DROP COLUMN IF EXISTS date_report,
DROP COLUMN IF EXISTS date_report_to_fda,
DROP COLUMN IF EXISTS detail_url,
DROP COLUMN IF EXISTS image_url,
DROP COLUMN IF EXISTS device_evaluated_by_manufacturer,
DROP COLUMN IF EXISTS event_key,
DROP COLUMN IF EXISTS event_location,
DROP COLUMN IF EXISTS manufacturer_city,
DROP COLUMN IF EXISTS manufacturer_country,
DROP COLUMN IF EXISTS manufacturer_state,
DROP COLUMN IF EXISTS mdr_report_key,
DROP COLUMN IF EXISTS mdr_text_action,
DROP COLUMN IF EXISTS mdr_text_description,
DROP COLUMN IF EXISTS measures_description,
DROP COLUMN IF EXISTS medical_specialty,
DROP COLUMN IF EXISTS model_number,
DROP COLUMN IF EXISTS notifying_country,
DROP COLUMN IF EXISTS number_devices_in_event,
DROP COLUMN IF EXISTS patient_count,
DROP COLUMN IF EXISTS product_category,
DROP COLUMN IF EXISTS product_description,
DROP COLUMN IF EXISTS product_name_specific,
DROP COLUMN IF EXISTS product_problem_flag,
DROP COLUMN IF EXISTS product_problems_list,
DROP COLUMN IF EXISTS product_subcategory,
DROP COLUMN IF EXISTS regulation_number,
DROP COLUMN IF EXISTS remedial_action_list,
DROP COLUMN IF EXISTS report_source_code,
DROP COLUMN IF EXISTS report_to_fda,
DROP COLUMN IF EXISTS report_to_manufacturer,
DROP COLUMN IF EXISTS risk_description,
DROP COLUMN IF EXISTS risk_type,
DROP COLUMN IF EXISTS risks_list,
DROP COLUMN IF EXISTS source_type,
DROP COLUMN IF EXISTS type_of_report;

-- 4. 删除 t_device_registration 表的已删除字段
ALTER TABLE t_device_registration 
DROP COLUMN IF EXISTS risk_class;

-- 5. 删除 t_crawler_data 表的已删除字段
ALTER TABLE t_crawler_data 
DROP COLUMN IF EXISTS risk_description;

-- 6. 删除不存在的表
DROP TABLE IF EXISTS t_device_pma;

-- 7. 验证表结构
DESCRIBE t_device_510k;
DESCRIBE d_510k_records;
DESCRIBE t_device_event;
DESCRIBE t_device_registration;
DESCRIBE t_crawler_data;

-- 显示执行结果
SELECT 'Table structure fix completed successfully' as result;


