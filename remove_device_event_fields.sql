-- 删除 t_device_event 表中的指定字段
-- 执行前请先备份数据库！

-- 删除基础字段
ALTER TABLE t_device_event DROP COLUMN event_type;
ALTER TABLE t_device_event DROP COLUMN type_of_report;
ALTER TABLE t_device_event DROP COLUMN date_report;
ALTER TABLE t_device_event DROP COLUMN source_type;
ALTER TABLE t_device_event DROP COLUMN report_source_code;

-- 删除设备信息字段
ALTER TABLE t_device_event DROP COLUMN model_number;
ALTER TABLE t_device_event DROP COLUMN manufacturer_city;
ALTER TABLE t_device_event DROP COLUMN manufacturer_state;
ALTER TABLE t_device_event DROP COLUMN manufacturer_country;
ALTER TABLE t_device_event DROP COLUMN medical_specialty;
ALTER TABLE t_device_event DROP COLUMN regulation_number;
ALTER TABLE t_device_event DROP COLUMN device_evaluated_by_manufacturer;

-- 删除报告内容字段
ALTER TABLE t_device_event DROP COLUMN mdr_text_description;
ALTER TABLE t_device_event DROP COLUMN mdr_text_action;
ALTER TABLE t_device_event DROP COLUMN contact_person;
ALTER TABLE t_device_event DROP COLUMN contact_phone;
ALTER TABLE t_device_event DROP COLUMN date_added;
ALTER TABLE t_device_event DROP COLUMN patient_count;

-- 删除 EU Safety Gate 特有字段
ALTER TABLE t_device_event DROP COLUMN product_name_specific;
ALTER TABLE t_device_event DROP COLUMN product_description;
ALTER TABLE t_device_event DROP COLUMN risk_type;
ALTER TABLE t_device_event DROP COLUMN risk_description;
ALTER TABLE t_device_event DROP COLUMN notifying_country;
ALTER TABLE t_device_event DROP COLUMN product_category;
ALTER TABLE t_device_event DROP COLUMN product_subcategory;
ALTER TABLE t_device_event DROP COLUMN measures_description;
ALTER TABLE t_device_event DROP COLUMN detail_url;
ALTER TABLE t_device_event DROP COLUMN image_url;
ALTER TABLE t_device_event DROP COLUMN brands_list;
ALTER TABLE t_device_event DROP COLUMN risks_list;

-- 删除 FDA 特有字段
ALTER TABLE t_device_event DROP COLUMN adverse_event_flag;
ALTER TABLE t_device_event DROP COLUMN date_report_to_fda;
ALTER TABLE t_device_event DROP COLUMN report_to_fda;
ALTER TABLE t_device_event DROP COLUMN report_to_manufacturer;
ALTER TABLE t_device_event DROP COLUMN mdr_report_key;
ALTER TABLE t_device_event DROP COLUMN event_location;
ALTER TABLE t_device_event DROP COLUMN event_key;
ALTER TABLE t_device_event DROP COLUMN number_devices_in_event;
ALTER TABLE t_device_event DROP COLUMN product_problem_flag;
ALTER TABLE t_device_event DROP COLUMN product_problems_list;
ALTER TABLE t_device_event DROP COLUMN remedial_action_list;

-- 验证删除结果
DESCRIBE t_device_event;
