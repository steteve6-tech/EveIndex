-- 创建爬虫任务执行日志表
-- 用于记录每次任务执行的详细信息

CREATE TABLE t_crawler_task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    task_id BIGINT NOT NULL COMMENT '任务配置ID',
    task_name VARCHAR(100) COMMENT '任务名称（冗余字段）',
    crawler_name VARCHAR(50) COMMENT '爬虫名称（冗余字段）',
    country_code VARCHAR(10) COMMENT '国家代码（冗余字段）',
    batch_no VARCHAR(50) COMMENT '执行批次号',
    status VARCHAR(20) NOT NULL COMMENT '执行状态：SUCCESS/FAILED/RUNNING/TIMEOUT/CANCELLED/PARTIAL_SUCCESS',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_seconds BIGINT COMMENT '执行耗时（秒）',
    crawled_count INT COMMENT '爬取数据量',
    saved_count INT COMMENT '成功保存数量',
    skipped_count INT COMMENT '跳过/重复数量',
    failed_count INT COMMENT '失败数量',
    keywords_used TEXT COMMENT '使用的关键词列表（JSON数组）',
    crawl_params TEXT COMMENT '爬取参数（JSON格式）',
    result_message TEXT COMMENT '执行结果消息',
    error_message TEXT COMMENT '错误信息',
    error_stack TEXT COMMENT '错误堆栈',
    retry_times INT DEFAULT 0 COMMENT '重试次数',
    is_manual TINYINT(1) DEFAULT 0 COMMENT '是否手动触发',
    triggered_by VARCHAR(50) COMMENT '触发人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_batch_no (batch_no),
    INDEX idx_country_code (country_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫任务执行日志表';

