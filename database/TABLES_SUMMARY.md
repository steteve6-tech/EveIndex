# 数据库表结构快速参考

## 📊 总览

**数据库名称**: `common_db`
**总表数**: 19张
**版本**: v2.0.0
**更新日期**: 2025-01-20

---

## 📋 完整表清单

| # | 表名 | 分类 | 实体类 | 状态 | 说明 |
|---|------|------|--------|------|------|
| 1 | `t_crawler_data` | 认证新闻 | ✅ CertNewsData | 核心 | SGS、UL、北测等新闻数据 |
| 2 | `t_device_510k` | 设备数据 | ✅ Device510K | 核心 | FDA 510K申请记录 |
| 3 | `t_device_registration` | 设备数据 | ✅ DeviceRegistrationRecord | 核心 | 设备注册记录 |
| 4 | `t_device_recall` | 设备数据 | ✅ DeviceRecallRecord | 核心 | 设备召回记录 |
| 5 | `t_device_event` | 设备数据 | ✅ DeviceEventReport | 核心 | 设备不良事件报告 |
| 6 | `t_guidance_document` | 设备数据 | ✅ GuidanceDocument | 核心 | FDA/EU指导文档和新闻 |
| 7 | `t_customs_case` | 设备数据 | ✅ CustomsCase | 核心 | 海关归类判例 |
| 8 | `t_devicematch_keywords` | 关键词 | ✅ DeviceMatchKeywords | 核心 | 设备匹配关键词（黑白名单） |
| 9 | `t_keyword` | 关键词 | ✅ Keyword | 核心 | 通用关键词库 |
| 10 | `t_unified_task_config` | 任务管理 | ✅ UnifiedTaskConfig | 核心 | 统一爬虫任务配置 |
| 11 | `t_unified_task_log` | 任务管理 | ✅ UnifiedTaskLog | 核心 | 统一任务执行日志 |
| 12 | `t_cert_news_task_config` | 任务管理 | ✅ CertNewsTaskConfig | 核心 | 认证新闻任务配置 |
| 13 | `t_cert_news_task_log` | 任务管理 | ✅ CertNewsTaskLog | 辅助 | 认证新闻任务日志 |
| 14 | `ai_judge_task` | AI功能 | ✅ AIJudgeTask | 辅助 | AI判断任务进度追踪 |
| 15 | `daily_country_risk_stats` | 统计分析 | ✅ CertNewsDailyCountryRiskStats | 辅助 | 每日国家风险统计 |
| 16 | `t_country` | 基础数据 | ✅ Country | 辅助 | 国家基础信息 |
| 17 | `t_crawler_checkpoint` | 爬虫管理 | ✅ CrawlerCheckpoint | 辅助 | 爬虫断点续传 |
| 18 | `t_crawler_state` | 爬虫管理 | ✅ CrawlerState | 辅助 | 爬虫执行状态监控 |
| 19 | `t_scheduled_crawler_config` | 任务管理 | ❌ 无 | 废弃 | 旧版定时配置（已废弃） |

---

## 🎯 表分类统计

| 分类 | 表数量 | 说明 |
|------|--------|------|
| **认证新闻** | 1 | 新闻数据源 |
| **设备数据** | 6 | 510K、注册、召回、事件、文档、海关 |
| **关键词管理** | 2 | 设备匹配、通用关键词 |
| **任务管理** | 5 | 任务配置和执行日志 |
| **AI功能** | 1 | AI判断任务追踪 |
| **统计分析** | 1 | 风险数据统计 |
| **基础数据** | 1 | 国家信息 |
| **爬虫管理** | 2 | 断点续传、状态监控 |
| **总计** | **19** | - |

---

## 🔑 核心表与辅助表

### 核心业务表（12张）⭐ 必须保留

```
1. t_crawler_data              -- 认证新闻数据
2. t_device_510k               -- 510K申请
3. t_device_registration       -- 设备注册
4. t_device_recall             -- 设备召回
5. t_device_event              -- 设备事件
6. t_guidance_document         -- 指导文档
7. t_customs_case              -- 海关判例
8. t_devicematch_keywords      -- 设备关键词
9. t_keyword                   -- 通用关键词
10. t_unified_task_config      -- 任务配置
11. t_unified_task_log         -- 任务日志
12. t_cert_news_task_config    -- 新闻任务配置
```

### 辅助功能表（6张）✅ 建议保留

```
13. t_cert_news_task_log       -- 新闻任务日志
14. ai_judge_task              -- AI判断任务
15. daily_country_risk_stats   -- 风险统计
16. t_country                  -- 国家信息
17. t_crawler_checkpoint       -- 断点续传
18. t_crawler_state            -- 爬虫状态
```

### 废弃表（1张）⚠️

```
19. t_scheduled_crawler_config -- 旧版配置（无实体类）
```

---

## 📈 表索引统计

| 表名 | 索引数量 | 主要索引字段 |
|------|----------|-------------|
| `t_device_510k` | 9 | k_number, device_name, date_received, risk_level |
| `t_device_registration` | 8 | device_name, manufacturer_name, status_code |
| `t_device_recall` | 7 | cfres_id, event_date_posted, device_name |
| `t_device_event` | 7 | report_number, date_of_event, manufacturer |
| `t_guidance_document` | 7 | title, publication_date, document_type |
| `t_unified_task_config` | 5 | task_name(UK), crawler_name, enabled |
| `t_unified_task_log` | 6 | task_id(FK), status, start_time |
| `t_crawler_data` | 5 | source_name, risk_level, status |

---

## 🔗 表关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      核心业务层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 认证新闻数据  │  │ 设备数据(6张) │  │ 关键词(2张)  │      │
│  │ t_crawler_   │  │ t_device_*    │  │ t_keyword*   │      │
│  │ data         │  │               │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓ 被管理
┌─────────────────────────────────────────────────────────────┐
│                      任务管理层                              │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ t_unified_task_  │ 1     * │ t_unified_task_  │         │
│  │ config           ├────────→│ log              │         │
│  │ (配置)           │   FK    │ (日志)           │         │
│  └──────────────────┘         └──────────────────┘         │
│                                                              │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ t_cert_news_task │         │ t_cert_news_task │         │
│  │ _config          │         │ _log             │         │
│  └──────────────────┘         └──────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                           ↓ 支持
┌─────────────────────────────────────────────────────────────┐
│                      辅助功能层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ AI判断任务    │  │ 断点续传     │  │ 爬虫状态     │      │
│  │ ai_judge_    │  │ t_crawler_   │  │ t_crawler_   │      │
│  │ task         │  │ checkpoint   │  │ state        │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ 风险统计     │  │ 国家信息     │                        │
│  │ daily_country│  │ t_country    │                        │
│  │ _risk_stats  │  │              │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 💾 存储空间预估

基于典型数据量的存储空间预估：

| 表名 | 预计行数 | 预估大小 | 说明 |
|------|----------|---------|------|
| `t_device_510k` | 10万+ | 500MB | 历史数据积累 |
| `t_device_registration` | 50万+ | 2GB | 数据量最大 |
| `t_crawler_data` | 5万+ | 200MB | 定期清理 |
| `t_unified_task_log` | 10万+ | 300MB | 需定期归档 |
| `t_device_recall` | 5万+ | 200MB | 增长稳定 |
| **其他表** | - | 100MB | 辅助数据 |
| **总计** | - | **≈3.3GB** | 1年数据量 |

---

## 🛡️ 数据保护级别

| 级别 | 表名 | 备份策略 |
|------|------|---------|
| **高** | 设备数据表(6张) | 每日全量备份 |
| **高** | 任务配置表(2张) | 每日全量备份 |
| **中** | 认证新闻数据 | 每周全量备份 |
| **中** | 关键词表(2张) | 每周全量备份 |
| **低** | 任务日志表(3张) | 定期归档即可 |
| **低** | 统计和状态表 | 可重新生成 |

---

## 🔧 维护清单

### 每日维护
- [ ] 检查任务执行日志
- [ ] 监控数据增长
- [ ] 查看错误告警

### 每周维护
- [ ] 备份核心数据表
- [ ] 清理过期日志（>30天）
- [ ] 检查索引性能

### 每月维护
- [ ] 分析表碎片
- [ ] 优化慢查询
- [ ] 归档历史数据
- [ ] 查看存储空间

### 每季度维护
- [ ] 全量数据备份
- [ ] 清理废弃数据
- [ ] 性能测试
- [ ] 容量规划

---

## 📊 快速查询

### 检查表状态
```sql
-- 查看所有表
SHOW TABLES;

-- 查看表数量（应该是19）
SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'common_db';

-- 查看表大小
SELECT
    TABLE_NAME,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS 'Size(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;
```

### 检查数据量
```sql
-- 各表记录数
SELECT
    TABLE_NAME,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
ORDER BY TABLE_ROWS DESC;
```

### 检查索引
```sql
-- 查看某表的所有索引
SHOW INDEX FROM t_device_510k;

-- 查看未使用的索引（需开启性能模式）
SELECT * FROM sys.schema_unused_indexes WHERE object_schema = 'common_db';
```

---

## 🎯 下一步操作

1. ✅ 执行 `init_database_full.sql` 创建所有19张表
2. ✅ 验证表结构：`SHOW TABLES;`
3. ✅ 检查初始数据：`SELECT * FROM t_keyword;`
4. ✅ 配置定时备份
5. ⚠️ 确认是否需要删除 `t_scheduled_crawler_config`

---

**文档版本**: v2.0.0
**最后更新**: 2025-01-20
**维护者**: 开发团队
