# 数据库初始化脚本

## 📋 概述

本目录包含医疗器械认证监控系统的数据库初始化脚本，根据项目实际使用的 JPA 实体类生成。

## 📁 文件说明

- `init_database_full.sql` - **完整的数据库初始化脚本（19张表）** ⭐ 推荐使用

## 🗄️ 数据库表结构（共19张表）

### 1. 认证新闻数据表（1张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_crawler_data` | 认证新闻数据（SGS、UL、北测等） | CertNewsData.java |

### 2. 医疗设备数据表（6张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_device_510k` | 510K申请记录 | Device510K.java |
| `t_device_registration` | 设备注册记录 | DeviceRegistrationRecord.java |
| `t_device_recall` | 设备召回记录 | DeviceRecallRecord.java |
| `t_device_event` | 设备事件报告 | DeviceEventReport.java |
| `t_guidance_document` | 指导文档和新闻 | GuidanceDocument.java |
| `t_customs_case` | 海关判例数据 | CustomsCase.java |

### 3. 关键词管理表（2张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_devicematch_keywords` | 设备匹配关键词（支持黑白名单） | DeviceMatchKeywords.java |
| `t_keyword` | 通用关键词库 | Keyword.java |

### 4. 任务配置和日志表（5张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_unified_task_config` | 统一任务配置 | UnifiedTaskConfig.java |
| `t_unified_task_log` | 统一任务执行日志 | UnifiedTaskLog.java |
| `t_cert_news_task_config` | 认证新闻任务配置 | CertNewsTaskConfig.java |
| `t_cert_news_task_log` | 认证新闻任务日志 | CertNewsTaskLog.java |
| `t_scheduled_crawler_config` | 旧版定时爬虫配置（已废弃） | ❌ 无实体类 |

### 5. AI判断任务表（1张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `ai_judge_task` | AI判断任务状态和进度 | AIJudgeTask.java |

### 6. 统计分析表（1张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `daily_country_risk_stats` | 每日国家风险数据统计 | CertNewsDailyCountryRiskStats.java |

### 7. 基础数据表（1张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_country` | 国家基础信息 | Country.java |

### 8. 爬虫状态管理表（3张）

| 表名 | 说明 | 实体类 |
|------|------|--------|
| `t_crawler_checkpoint` | 爬虫断点续传记录 | CrawlerCheckpoint.java |
| `t_crawler_state` | 爬虫执行状态和统计 | CrawlerState.java |
| `t_crawler_task_log` | 爬虫任务日志（通用） | ❌ 无实体类（动态使用） |

---

## 🚀 使用方法

### 方式一：命令行执行

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source D:/Project/AAArenew/AAArenew/database/init_database_full.sql

# 或者直接执行
mysql -u root -p < database/init_database_full.sql
```

### 方式二：使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到数据库服务器
3. 打开 `init_database_full.sql` 文件
4. 点击 "Execute" 执行脚本

### 方式三：使用 Docker/Podman

```bash
# 如果使用 Docker Compose
docker exec -i cert_mysql_prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} < database/init_database_full.sql

# 如果使用 Podman
podman exec -i cert_mysql_prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} < database/init_database_full.sql
```

---

## 📊 表设计说明

### 基础字段（继承自 BaseDeviceEntity）

所有设备数据表都包含以下通用字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `risk_level` | VARCHAR(10) | 风险等级（HIGH/MEDIUM/LOW/NONE） |
| `keywords` | TEXT | 关键词（JSON或文本格式） |
| `jd_country` | VARCHAR(20) | 数据来源国家 |
| `data_source` | VARCHAR(100) | 数据源（FDA/EUDAMED等） |
| `remark` | TEXT | 备注信息（AI判断原因等） |
| `crawl_time` | DATETIME | 爬取时间 |
| `data_status` | VARCHAR(20) | 数据状态（ACTIVE/INACTIVE） |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

### 索引优化

脚本中已经为常用查询字段创建了索引，包括：

- 国家代码索引
- 日期字段索引
- 风险等级索引
- 数据源索引
- 外键索引
- 状态字段索引

---

## 📋 表分类说明

### 核心业务表（12张）⭐ 必须保留

这些表是系统核心功能必需的：

```
t_crawler_data                    -- 认证新闻数据
t_device_510k                     -- 510K申请记录
t_device_registration             -- 设备注册记录
t_device_recall                   -- 设备召回记录
t_device_event                    -- 设备事件报告
t_guidance_document               -- 指导文档
t_customs_case                    -- 海关判例
t_devicematch_keywords            -- 设备匹配关键词
t_keyword                         -- 通用关键词
t_unified_task_config             -- 统一任务配置
t_unified_task_log                -- 统一任务日志
t_cert_news_task_config           -- 认证新闻任务配置
```

### 辅助功能表（6张）✅ 建议保留

提供重要的辅助功能：

```
ai_judge_task                     -- AI判断任务进度追踪
daily_country_risk_stats          -- 风险数据统计分析
t_country                         -- 国家基础信息
t_crawler_checkpoint              -- 断点续传支持
t_crawler_state                   -- 爬虫状态监控
t_cert_news_task_log              -- 新闻任务日志
```

### 可能废弃的表（1张）⚠️ 需要确认

```
t_scheduled_crawler_config        -- 旧版定时配置（可能已被 t_unified_task_config 替代）
```

**确认方法**：
```sql
-- 检查是否有数据
SELECT COUNT(*) FROM t_scheduled_crawler_config;

-- 如果为0且代码中无引用，可以删除
-- DROP TABLE IF EXISTS t_scheduled_crawler_config;
```

---

## ⚠️ 注意事项

1. **数据库编码**：所有表使用 `utf8mb4` 编码，支持完整的 Unicode 字符
2. **排序规则**：使用 `utf8mb4_unicode_ci` 排序规则
3. **主键策略**：大部分表使用自增主键（`AUTO_INCREMENT`）
4. **唯一约束**：关键业务字段设置了唯一索引，防止数据重复
5. **外键约束**：任务日志表与任务配置表建立了外键关系（`ON DELETE CASCADE`）
6. **默认值**：重要字段设置了合理的默认值
7. **废弃表**：`t_scheduled_crawler_config` 已被标记为废弃，保留仅为兼容旧数据

---

## 🛠️ 维护建议

### 定期备份

```bash
# 备份整个数据库
mysqldump -u root -p common_db > backup_$(date +%Y%m%d).sql

# 备份特定表
mysqldump -u root -p common_db t_device_510k t_device_registration > device_backup.sql

# 只备份表结构
mysqldump -u root -p --no-data common_db > structure_only.sql
```

### 清理旧数据

```sql
-- 删除1年前的任务日志
DELETE FROM t_unified_task_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- 删除低风险且超过6个月的数据
DELETE FROM t_crawler_data WHERE risk_level = 'LOW' AND crawl_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);

-- 清理已完成的断点续传记录（超过30天）
DELETE FROM t_crawler_checkpoint WHERE status = 'COMPLETED' AND last_updated < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### 查看表统计

```sql
-- 查看所有表的记录数和大小
SELECT
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '说明',
    TABLE_ROWS AS '记录数',
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS '数据大小(MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS '索引大小(MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS '总大小(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
ORDER BY TABLE_ROWS DESC;

-- 查看各类型表的数量
SELECT
    CASE
        WHEN TABLE_NAME LIKE 't_device_%' THEN '设备数据表'
        WHEN TABLE_NAME LIKE '%task%' THEN '任务管理表'
        WHEN TABLE_NAME LIKE '%crawler%' THEN '爬虫管理表'
        WHEN TABLE_NAME LIKE '%keyword%' THEN '关键词表'
        ELSE '其他表'
    END AS '表分类',
    COUNT(*) AS '表数量'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
GROUP BY 表分类;
```

### 性能优化

```sql
-- 分析表（更新统计信息）
ANALYZE TABLE t_device_510k;
ANALYZE TABLE t_device_registration;
ANALYZE TABLE t_crawler_data;

-- 优化表（整理碎片）
OPTIMIZE TABLE t_device_510k;
OPTIMIZE TABLE t_unified_task_log;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';
```

---

## 📝 版本历史

### v2.0.0 (2025-01-20)

**完整版（19张表）**：
- ✅ 包含所有核心业务表（12张）
- ✅ 包含所有辅助功能表（6张）
- ✅ 保留旧版配置表（1张，标记为废弃）
- ✅ 新增AI判断任务表
- ✅ 新增统计分析表
- ✅ 新增爬虫状态管理表
- ✅ 优化索引策略
- ✅ 完善注释说明
- ✅ 添加初始化数据

### v1.0.0 (2024-09-30)

- 🎉 初始版本（12张核心表）

---

## 🔗 相关文档

- [系统维护文档](../SYSTEM_MAINTENANCE_GUIDE.md)
- [Podman 部署指南](../PODMAN_DEPLOYMENT_GUIDE.md)
- [README](../README.md)

---

## 💡 常见问题

### Q1: 执行脚本时出现 "Table already exists" 错误？

A: 脚本中已经包含 `DROP TABLE IF EXISTS` 语句，会自动删除已存在的表。如果仍有问题，请手动删除表后重新执行。

### Q2: 如何查看表结构？

A: 使用以下命令：
```sql
DESCRIBE t_device_510k;
SHOW CREATE TABLE t_device_510k;
```

### Q3: 如何修改表结构？

A: 不建议直接修改表结构，请通过 JPA 实体类修改，然后重新生成 SQL 脚本。

### Q4: 数据库性能优化建议？

A:
1. 定期分析和优化表：`ANALYZE TABLE t_device_510k;`
2. 重建索引：`OPTIMIZE TABLE t_device_510k;`
3. 监控慢查询日志
4. 合理使用分区表（大数据量时）
5. 定期清理旧日志数据

### Q5: t_scheduled_crawler_config 表是否需要删除？

A:
- 该表没有对应的实体类，可能是旧版本遗留
- 建议先查询是否有数据：`SELECT COUNT(*) FROM t_scheduled_crawler_config;`
- 如果没有数据且代码中无引用，可以安全删除
- 当前脚本保留该表以兼容可能存在的旧数据

### Q6: 如何验证所有表都创建成功？

A:
```sql
-- 查看表数量（应该是19张）
SELECT COUNT(*) AS '表数量' FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'common_db';

-- 查看所有表
SHOW TABLES;

-- 查看表的详细信息
SELECT TABLE_NAME, TABLE_COMMENT, CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'common_db'
ORDER BY TABLE_NAME;
```

### Q7: 如何处理数据迁移？

A: 如果从旧版本升级：
1. 备份现有数据：`mysqldump -u root -p common_db > backup.sql`
2. 导出关键数据到CSV
3. 执行新的初始化脚本
4. 导入备份的数据
5. 验证数据完整性

---

## 🎯 表依赖关系

```
外键关系：
t_unified_task_config (id) ← t_unified_task_log (task_id)

逻辑关联：
t_country ↔ t_device_* (通过 jd_country 字段)
t_keyword ↔ t_device_* (通过 keywords 字段)
t_devicematch_keywords ↔ t_device_* (用于数据匹配)
t_unified_task_config ↔ t_crawler_checkpoint (任务断点)
t_crawler_state ↔ 所有爬虫 (状态监控)
```

---

**维护者**: 开发团队
**更新日期**: 2025-01-20
**版本**: v2.0.0 (完整版 - 19张表)
