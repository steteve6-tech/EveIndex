-- 为 t_product 表添加竞品信息相关字段
-- 用于支持从高风险数据生成竞品信息功能

-- 添加申请人名称字段
ALTER TABLE t_product ADD COLUMN applicant_name VARCHAR(100) COMMENT '申请人名称';

-- 添加品牌名称字段（贸易名称）
ALTER TABLE t_product ADD COLUMN brand_name VARCHAR(100) COMMENT '品牌名称（贸易名称）';

-- 添加设备代码字段（如K编号、注册编号等）
ALTER TABLE t_product ADD COLUMN device_code VARCHAR(50) COMMENT '设备代码（如K编号、注册编号等）';

-- 添加数据来源字段（从哪个高风险数据生成）
ALTER TABLE t_product ADD COLUMN data_source VARCHAR(30) COMMENT '数据来源（从哪个高风险数据生成）';

-- 添加原始数据ID字段（关联到原始高风险数据）
ALTER TABLE t_product ADD COLUMN source_data_id BIGINT COMMENT '原始数据ID（关联到原始高风险数据）';

-- 添加设备等级字段
ALTER TABLE t_product ADD COLUMN device_class VARCHAR(20) COMMENT '设备等级';

-- 添加设备描述字段
ALTER TABLE t_product ADD COLUMN device_description TEXT COMMENT '设备描述';

-- 添加备注信息字段
ALTER TABLE t_product ADD COLUMN remarks TEXT COMMENT '备注信息';

-- 添加索引以提高查询性能
CREATE INDEX idx_product_device_code ON t_product(device_code);
CREATE INDEX idx_product_data_source ON t_product(data_source);
CREATE INDEX idx_product_source_data_id ON t_product(source_data_id);
CREATE INDEX idx_product_device_class ON t_product(device_class);
CREATE INDEX idx_product_applicant_name ON t_product(applicant_name);
CREATE INDEX idx_product_brand_name ON t_product(brand_name);

-- 添加唯一约束，避免重复生成竞品信息
CREATE UNIQUE INDEX idx_product_unique_source ON t_product(data_source, source_data_id) 
WHERE data_source IS NOT NULL AND source_data_id IS NOT NULL;

-- 添加注释说明
ALTER TABLE t_product COMMENT = '产品基础信息表，支持竞品信息管理和从高风险数据生成竞品信息';
