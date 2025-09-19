-- 更新 t_device_registration 表字段长度
-- 解决数据截断问题

-- 更新 registration_number 字段长度从 200 到 500
ALTER TABLE t_device_registration MODIFY COLUMN registration_number VARCHAR(500) NOT NULL;

-- 更新 fei_number 字段长度从 100 到 200
ALTER TABLE t_device_registration MODIFY COLUMN fei_number VARCHAR(200);

-- 更新 manufacturer_name 字段长度从 255 到 500
ALTER TABLE t_device_registration MODIFY COLUMN manufacturer_name VARCHAR(500);

-- 更新 device_name 字段长度从 255 到 500
ALTER TABLE t_device_registration MODIFY COLUMN device_name VARCHAR(500);

-- 更新 proprietary_name 字段长度从 255 到 1000
ALTER TABLE t_device_registration MODIFY COLUMN proprietary_name VARCHAR(1000);

-- 更新 device_class 字段长度从 50 到 100
ALTER TABLE t_device_registration MODIFY COLUMN device_class VARCHAR(100);

-- 更新 risk_class 字段长度从 50 到 100
ALTER TABLE t_device_registration MODIFY COLUMN risk_class VARCHAR(100);
