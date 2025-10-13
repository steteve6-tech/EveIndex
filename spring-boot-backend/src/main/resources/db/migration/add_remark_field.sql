-- 为6个设备数据表添加备注字段
-- 用于存储AI判断原因、人工审核意见等信息

-- 注意：如果字段已存在，执行会报错，可以忽略或手动检查

-- 1. 为t_device_510k表添加备注字段
ALTER TABLE t_device_510k ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 2. 为t_device_event表添加备注字段
ALTER TABLE t_device_event ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 3. 为t_device_recall表添加备注字段
ALTER TABLE t_device_recall ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 4. 为t_device_registration表添加备注字段
ALTER TABLE t_device_registration ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 5. 为t_guidance_document表添加备注字段
ALTER TABLE t_guidance_document ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 6. 为t_customs_case表添加备注字段
ALTER TABLE t_customs_case ADD COLUMN remark TEXT COMMENT '备注信息（AI判断原因、人工审核意见等）';

-- 说明：
-- 该字段将用于记录：
-- 1. AI判断结果：是否为测肤仪相关设备、判断理由、置信度
-- 2. 人工审核意见：审核人员的评价和修正意见
-- 3. 数据处理历史：记录数据经过的处理步骤
-- 4. 其他备注信息：任何需要记录的额外信息

