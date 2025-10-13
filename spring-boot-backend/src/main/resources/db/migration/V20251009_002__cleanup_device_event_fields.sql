-- 清理 t_device_event 表中多余的字段
-- 创建时间: 2025-10-09
-- 说明: 删除数据库中有但实体类DeviceEventReport.java中没有的字段
-- 
-- 实体类应有的字段(13个)：
-- id, report_number, date_of_event, date_received, brand_name, generic_name,
-- manufacturer_name, device_class, risk_level, keywords, data_source, jd_country, create_time
--
-- 数据库实际有52个字段，需要删除39个多余字段

-- ========== 删除多余字段（39个） ==========

-- 1. 事件相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS adverse_event_flag;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS event_key;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS event_location;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS event_type;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS number_devices_in_event;

-- 2. 产品相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS brands_list;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS model_number;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_category;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_description;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_name_specific;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_problem_flag;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_problems_list;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS product_subcategory;

-- 3. 制造商相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS manufacturer_city;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS manufacturer_country;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS manufacturer_state;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS device_evaluated_by_manufacturer;

-- 4. 联系人字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS contact_person;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS contact_phone;

-- 5. 日期字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS date_added;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS date_report;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS date_report_to_fda;

-- 6. URL和详情字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS detail_url;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS image_url;

-- 7. MDR相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS mdr_report_key;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS mdr_text_action;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS mdr_text_description;

-- 8. 风险和措施相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS measures_description;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS risk_description;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS risk_type;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS risks_list;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS remedial_action_list;

-- 9. 报告相关字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS report_source_code;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS report_to_fda;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS report_to_manufacturer;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS source_type;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS type_of_report;

-- 10. 其他字段
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS medical_specialty;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS notifying_country;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS patient_count;
ALTER TABLE common_db.t_device_event DROP COLUMN IF EXISTS regulation_number;

-- ========== 删除多余的索引 ==========

-- 删除 event_type 的索引（字段已删除）
DROP INDEX IF EXISTS idx_event_type ON common_db.t_device_event;

-- ========== 验证最终结果 ==========

-- 查看清理后的表结构（应该只剩13个字段）
SELECT 
    COLUMN_NAME as '字段名',
    COLUMN_TYPE as '类型',
    IS_NULLABLE as '可空',
    COLUMN_COMMENT as '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'common_db'
  AND TABLE_NAME = 't_device_event'
ORDER BY ORDINAL_POSITION;

-- 期望的最终字段列表（13个）：
-- 1. id
-- 2. report_number
-- 3. date_of_event
-- 4. date_received
-- 5. brand_name
-- 6. generic_name
-- 7. manufacturer_name
-- 8. device_class
-- 9. risk_level
-- 10. keywords
-- 11. data_source
-- 12. jd_country
-- 13. create_time
