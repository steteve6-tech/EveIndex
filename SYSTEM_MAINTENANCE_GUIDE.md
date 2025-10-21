# 医疗器械认证监控系统 - 维护文档

## 目录

- [系统概述](#系统概述)
- [技术架构](#技术架构)
- [环境配置](#环境配置)
- [系统功能说明](#系统功能说明)
- [维护操作](#维护操作)
- [爬虫系统](#爬虫系统)
- [AI 模块](#ai-模块)
- [日志系统](#日志系统)
- [故障排查](#故障排查)
- [数据库管理](#数据库管理)
- [部署指南](#部署指南)
- [性能优化](#性能优化)
- [安全建议](#安全建议)

---

## 系统概述

### 项目背景

认证风险预警系统是一个用于监控医疗器械认证信息的智能化平台，帮助企业及时了解全球医疗器械认证标准变化、产品注册动态和风险信息。

### 核心功能

- **认证新闻监控**：自动抓取 SGS、UL、北测等认证机构的最新资讯
- **设备数据监控**：监控美国、欧盟、日本、韩国、台湾等地区的医疗器械注册、召回、事件报告等数据
- **AI 智能审计**：利用 GPT-4o 模型自动判断数据的相关性和风险等级
- **多语言翻译**：集成火山引擎翻译服务，支持韩文等语言的自动翻译
- **定时任务调度**：支持爬虫任务的定时执行和参数预设
- **数据可视化**：提供风险数据统计和分析展示

### 项目信息

- **项目名称**：医疗器械认证监控系统
- **版本**：v2.0.0
- **最后更新**：2025-01-20

---

## 技术架构

### 后端技术栈

- **框架**：Spring Boot 3.2.0
- **JDK 版本**：Java 17
- **ORM**：Spring Data JPA
- **数据库**：MySQL 8.0+
- **缓存**：Redis
- **数据库连接池**：Druid 1.2.20
- **HTTP 客户端**：Jsoup（爬虫）、Apache HttpClient
- **AI 服务**：
  - OpenAI GPT-4o（数据分析判断）
  - 火山引擎翻译 API（多语言翻译）
- **日志**：Logback
- **工具库**：Hutool、Lombok

### 前端技术栈

- **框架**：Vue 3.4.0
- **构建工具**：Vite 6.0.1
- **语言**：TypeScript 5.3.0
- **UI 框架**：Ant Design Vue 4.2.6
- **图表**：ECharts 5.4.3
- **状态管理**：Pinia 2.1.7
- **路由**：Vue Router 4.2.5
- **HTTP 客户端**：Axios 1.11.0
- **日期处理**：Day.js 1.11.10

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层 (Vue 3)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ 数据展示 │  │ AI 审计  │  │ 爬虫管理 │  │ 任务调度 │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP/REST API
┌─────────────────────────────────────────────────────────────┐
│                    后端层 (Spring Boot 3)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Controller  │  │   Service    │  │  Repository  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  爬虫引擎    │  │  AI 服务     │  │  定时任务    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
              ↓                    ↓                   ↓
┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐
│   MySQL 数据库   │  │  Redis 缓存      │  │  外部 API    │
│  - 设备数据      │  │  - 任务队列      │  │  - OpenAI    │
│  - 新闻数据      │  │  - 会话缓存      │  │  - 火山引擎  │
│  - 任务日志      │  └──────────────────┘  └──────────────┘
└──────────────────┘
```

---

## 环境配置

### 1. 环境变量配置

#### 创建生产环境配置文件

```bash
# 复制环境变量模板
cp .env.prod.example .env.prod

# 编辑配置文件
vim .env.prod  # 或使用 nano
```

#### 必须配置的环境变量

**数据库配置**

```env
# 数据库连接
DATABASE_PASSWORD=your_strong_database_password_here_2024
MYSQL_ROOT_PASSWORD=your_strong_root_password_here_2024
MYSQL_DATABASE=common_db
MYSQL_USER=cert_user
```

**CORS 配置**

```env
# 配置为实际域名
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**OpenAI API 配置**

```env
# GPT-4o API（用于 AI 智能审核）
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_MODEL=gpt-4o
OPENAI_BASE_URL=https://api.openai.com/v1
OPENAI_TIMEOUT=60
```

**火山引擎翻译配置**

```env
# 火山引擎翻译服务（用于韩文翻译）
VOLCENGINE_ACCESS_KEY=your_volcengine_access_key_here
VOLCENGINE_SECRET_KEY=your_volcengine_secret_key_here
VOLCENGINE_REGION=cn-beijing
```

**Redis 配置**

```env
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_strong_redis_password_here_2024
```

#### 可选配置

**Druid 数据库监控**

```env
DRUID_MONITOR_ENABLED=true
DRUID_ALLOW_IP=127.0.0.1,your_server_ip_here
DRUID_USERNAME=admin
DRUID_PASSWORD=your_strong_druid_password_here_2024
```

**邮件通知**

```env
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password_or_auth_code_here
EMAIL_NOTIFICATION_ENABLED=false
```

### 2. 配置文件说明

详细配置说明请参考 `.env.prod.example` 文件中的注释。

---

## 系统功能说明

### 认证更新风险预警模块

#### 1. 风险数据统计展示

系统首页展示各认证机构的数据统计，包括：
- 数据总量
- 高风险/低风险分布
- 最新更新时间

#### 2. 各国家风险数据总览

按国家/地区展示风险数据分布：
- SGS 新闻
- UL Solutions 新闻
- 北测新闻

#### 3. 数据管理与审核

**手动审核流程**：

1. 查看具体数据详情
2. 点击源网址进入原始网站进行判断
3. 若数据相关，保留并标记为高风险
4. 若数据不相关，设置为低风险

**关键词黑名单**：
- 不相关数据可添加到黑名单
- 避免后续重复爬取

#### 4. AI 辅助判断

**输入数据**：
- `title`：标题
- `content`：内容
- `summary`：总结

**输出结果**：
```json
{
  "isRelated": true,
  "confidence": 90.0,
  "reason": "新闻涉及RoHS认证标准的更新，属于无线电子设备认证标准范畴",
  "extractedKeywords": ["RoHS", "GB 26572-2025", "SAMR"]
}
```

**执行步骤**：
1. 选择风险等级、数据源、判断数量
2. 点击"执行 AI 判断"
3. AI 自动分析并标记相关性
4. 相关数据设置为高风险，不相关数据设置为低风险

### 医疗认证风险预警模块

#### 数据类型

- **510K 申请**：美国医疗器械上市前通知
- **注册记录**：设备注册信息
- **召回记录**：产品召回信息
- **事件报告**：不良事件报告
- **指导文档**：监管指南和标准
- **海关案例**：进出口归类案例

#### AI 判断流程

1. 查看各国家高风险数据统计
2. 选择国家、风险等级、数据类型、判断数量
3. 点击"AI 判断"执行
4. AI 分析设备名称、制造商、专有名称等信息
5. 自动标记相关性和风险等级

**判断标准**：
- 设备是否为测肤仪/皮肤分析仪相关
- 是否涉及皮肤检测、诊断、分析功能
- 排除非相关医疗设备

---

## 维护操作

### 日志系统

#### 日志配置

**Logback 配置** (`logback-spring.xml`)：

```xml
<configuration>
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出（按日期滚动） -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>./logs/certification-monitor.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>./logs/certification-monitor.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 项目日志级别 -->
    <logger name="com.certification" level="DEBUG">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
</configuration>
```

**应用配置** (`application.yml`)：

```yaml
logging:
  level:
    com.certification: DEBUG    # 项目日志：详细调试
    org.springframework.web: INFO
    org.hibernate.SQL: OFF      # 关闭 SQL 日志
    root: WARN                  # 根日志：警告级别
  file:
    name: certification-monitor.log
    path: ./logs/
```

#### 日志存储位置

**文件日志**：`spring-boot-backend/logs/`
- 应用启动和初始化日志
- 爬虫注册和执行日志
- 翻译服务日志
- 任务执行日志

**数据库日志**：`t_unified_task_log` 表
- 爬虫任务执行历史
- 任务结果和状态
- 错误信息

#### 日志清理

**定时清理任务**：
- 执行时间：每天凌晨 3:00
- 清理规则：删除 30 天前的日志文件
- 实现类：`ScheduledTaskService.cleanupOldLogs()`

**手动清理日志**：

```bash
# 删除 30 天前的日志
find ./logs -name "*.log" -mtime +30 -delete

# 查看日志文件大小
du -sh ./logs/*

# 压缩归档旧日志
tar -czf logs-backup-$(date +%Y%m%d).tar.gz ./logs/*.log
```

### 系统监控

#### Druid 数据库监控

访问地址：`http://your-domain:8080/druid/login.html`

监控指标：
- SQL 执行统计
- 慢查询分析
- 数据库连接池状态
- 事务统计

#### 应用性能监控

关键指标：
- JVM 内存使用率
- 线程池状态
- 爬虫任务执行时间
- API 响应时间

### 定时任务管理

#### 查看定时任务

在前端"任务管理"界面可以查看和管理所有定时任务：
- 任务名称和描述
- Cron 表达式
- 下次执行时间
- 启用/禁用状态

#### 修改定时任务

```sql
-- 查看任务配置
SELECT * FROM t_unified_task_config WHERE enabled = 1;

-- 修改任务执行时间
UPDATE t_unified_task_config
SET cron_expression = '0 0 2 * * ?'
WHERE task_name = 'US_510K_DAILY';

-- 禁用任务
UPDATE t_unified_task_config
SET enabled = 0
WHERE id = 1;
```

---

## 爬虫系统

### 爬虫统计

| 模块 | 爬虫数量 | 说明 |
|------|---------|------|
| 认证新闻模块 | 3 个 | SGS、UL、北测新闻爬虫 |
| 设备数据模块 | 30+ 个 | 美国、欧盟、日本、韩国、台湾、中国 |
| 支持国家/地区 | 6 个 | US、EU、JP、KR、TW、CN |

### 认证新闻模块爬虫

#### 1. SGS 新闻爬虫 (`SgsCrawler`)

- **数据源**：SGS 官网
- **文件路径**：`com.certification.crawler.certification.SgsCrawler`
- **数据表**：`t_cert_news_data`
- **爬取内容**：认证标准更新、产品认证公告

#### 2. UL Solutions 爬虫 (`ULCrawler`)

- **数据源**：UL Solutions 官网
- **文件路径**：`com.certification.crawler.certification.ULCrawler`
- **数据表**：`t_cert_news_data`
- **爬取内容**：安全认证资讯、标准变更

#### 3. 北测新闻爬虫 (`BeiceCrawler`)

- **数据源**：北测官网
- **文件路径**：`com.certification.crawler.certification.BeiceCrawler`
- **数据表**：`t_cert_news_data`
- **爬取内容**：国内认证动态、检测资讯

### 设备数据模块爬虫

#### 美国 (US)

| 爬虫类型 | 类名 | 数据表 | 说明 |
|---------|------|--------|------|
| 510K 申请 | `US_510K` | `t_device_510k` | FDA 上市前通知 |
| 注册记录 | `US_registration` | `t_device_registration_record` | 设备注册信息 |
| 召回记录 | `US_recall_api` | `t_device_recall_record` | 产品召回信息 |
| 事件报告 | `US_event_api` | `t_device_event_report` | 不良事件报告 |
| 指导文档 | `US_Guidance` | `t_guidance_document` | FDA 监管指南 |

#### 日本 (JP)

| 爬虫类型 | 类名 | 数据表 | 说明 |
|---------|------|--------|------|
| 召回记录 | `Jprecall` | `t_device_recall_record` | PMDA 召回信息 |
| 注册记录 | `JpRegistration` | `t_device_registration_record` | 医疗器械注册 |
| 指导文档 | `JpGuidance` | `t_guidance_document` | 监管指南 |

#### 韩国 (KR)

| 爬虫类型 | 类名 | 数据表 | 说明 |
|---------|------|--------|------|
| 注册记录 | `KR_regstration` | `t_device_registration_record` | 医疗器械许可 |
| 召回记录 | `KrRecall` | `t_device_recall_record` | 召回公告 |
| 海关案例 | `Kr_customcase` | `t_customs_case` | 通关归类案例 |
| 事件报告 | `KrEvent` | `t_device_event_report` | 不良事件 |
| 指导文档 | `KrGuidance` | `t_guidance_document` | 法规指南 |

**特殊说明**：韩国爬虫集成了火山引擎翻译 API，自动将韩文翻译为英文。

#### 台湾 (TW)

| 爬虫类型 | 文件位置 | 数据表 | 说明 |
|---------|---------|--------|------|
| 注册记录 | `tw/TwRegistration` | `t_device_registration_record` | TFDA 医疗器械许可 |
| 海关案例 | `tw/TwCustomsCase` | `t_customs_case` | 进出口归类 |
| 事件报告 | `tw/TwEvent` | `t_device_event_report` | 不良事件通报 |
| 指导文档 | `tw/TwGuidance` | `t_guidance_document` | 法规公告 |

**特殊说明**：台湾 FDA 网站需要处理验证码，已集成 2Captcha 服务。详见：`台湾FDA验证码识别方案使用指南.md`

### 统一爬虫管理

#### 爬虫调度平台

在前端"爬虫管理"界面可以：
1. 选择国家和爬虫类型
2. 设置爬取参数（关键词、数量、日期范围等）
3. 测试执行
4. 保存为预设配置
5. 设置定时任务

#### 配置预设

示例配置：

```json
{
  "crawlerName": "US_510K",
  "searchTerm": "skin analyzer",
  "maxRecords": 100,
  "fieldKeywords": {
    "keywords": ["dermatology", "facial", "skin imaging"]
  },
  "dateRange": {
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
  }
}
```

#### 任务调度

Cron 表达式示例：

```
# 每天凌晨 2 点执行
0 0 2 * * ?

# 每周一上午 10 点执行
0 0 10 ? * MON

# 每月 1 号凌晨 1 点执行
0 0 1 1 * ?
```

### 新增爬虫开发指南

#### 步骤 1：创建爬虫实现类

**位置**：`com.certification.crawler.countrydata.{country}/`

**模板代码**：

```java
package com.certification.crawler.countrydata.jp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Component
public class JpNewCrawler {

    @Autowired
    private YourRepository repository;

    @Autowired
    private TranslateAI translateAI;  // 如需翻译

    // 数据模型
    @Data
    public static class YourDataModel {
        private String field1;
        private String field2;
        // ... 其他字段
    }

    /**
     * 主要爬取方法
     * @param keyword 搜索关键词
     * @param maxRecords 最大记录数
     * @return 执行结果
     */
    public String crawlByKeyword(String keyword, int maxRecords) {
        log.info("开始爬取，关键词: {}", keyword);

        try {
            // 1. 发送 HTTP 请求
            List<YourDataModel> dataList = fetchData(keyword, maxRecords);

            // 2. 解析数据
            // 3. 保存到数据库
            return saveToDatabase(dataList);

        } catch (Exception e) {
            log.error("爬取失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * HTTP 请求方法
     */
    private List<YourDataModel> fetchData(String keyword, int maxRecords) {
        // 实现 HTTP 请求和数据解析
        // 使用 Jsoup 或 HttpClient
        return new ArrayList<>();
    }

    /**
     * 保存到数据库
     */
    private String saveToDatabase(List<YourDataModel> dataList) {
        int saved = 0;
        int duplicates = 0;

        for (YourDataModel data : dataList) {
            // 检查重复（根据唯一标识）
            // 保存数据
            saved++;
        }

        return String.format("保存成功: %d 条新记录, 跳过重复: %d 条", saved, duplicates);
    }
}
```

#### 步骤 2：创建适配器类

**位置**：`com.certification.service.crawler.adapter/`

**模板代码**：

```java
package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.jp.JpNewCrawler;
import com.certification.service.crawler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("JP_NewType_Adapter")  // Bean 名称必须唯一
public class JPNewTypeAdapter implements ICrawlerExecutor {

    @Autowired
    private JpNewCrawler crawler;

    @Override
    public String getCrawlerName() {
        return "JP_NewType";  // 爬虫名称
    }

    @Override
    public String getCountryCode() {
        return "JP";  // 国家代码
    }

    @Override
    public String getCrawlerType() {
        return "NEW_TYPE";  // 爬虫类型
    }

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行 JP_NewType 爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            // 提取参数
            String keyword = extractKeyword(params);
            int maxRecords = params.getMaxRecords() != null ?
                            params.getMaxRecords() : 100;

            // 调用爬虫
            String resultMsg = crawler.crawlByKeyword(keyword, maxRecords);

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            // 解析结果
            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("JP_NewType 爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }

    private String extractKeyword(CrawlerParams params) {
        if (params.getFieldKeywords() != null
            && params.getFieldKeywords().containsKey("keywords")) {
            List<String> keywords = params.getFieldKeywords().get("keywords");
            if (!keywords.isEmpty()) {
                return keywords.get(0);
            }
        }
        return params.getSearchTerm();
    }

    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        // 添加验证逻辑
        return true;
    }

    @Override
    public String getDescription() {
        return "日本新类型医疗器械数据爬虫";
    }
}
```

#### 步骤 3：自动注册

使用 `@Component` 注解的适配器会被 Spring 自动扫描，`CrawlerRegistryService` 会在启动时自动注册。

**验证注册**：

```bash
# 启动应用后，查看日志
# 应该能看到：
注册爬虫: JP_NewType (JP_NEW_TYPE) - 日本新类型医疗器械数据爬虫
```

#### 步骤 4：前端调用

```javascript
// POST /api/unified-crawler/execute
{
  "crawlerName": "JP_NewType",
  "searchTerm": "skin analyzer",
  "maxRecords": 100
}
```

### 爬虫维护指南

#### 场景 1：网站结构变更

**问题**：目标网站 HTML 结构或 API 接口变更，导致解析失败

**诊断**：

```java
log.error("解析失败，未找到数据表格");
log.warn("找到 0 行数据");
```

**解决步骤**：

1. 访问目标网站，检查页面结构
2. 使用浏览器开发者工具查看 HTML
3. 更新 CSS 选择器或 XPath
4. 测试并验证

**示例**：

```java
// 修改前
Elements rows = doc.select("table.old-class tbody tr");

// 修改后（网站结构变更）
Elements rows = doc.select("table.new-class tbody tr");
```

#### 场景 2：API 参数变更

**问题**：目标 API 的请求参数或响应格式变更

**解决步骤**：

1. 使用浏览器 Network 工具抓包
2. 查看新的请求参数格式
3. 更新爬虫代码中的参数构建逻辑
4. 更新响应解析逻辑

**示例**：

```java
// 修改前
params.put("search", keyword);

// 修改后（API 参数名变更）
params.put("searchKeyword", keyword);
```

#### 场景 3：翻译服务异常

**问题**：火山引擎翻译服务签名失败或配额不足

**诊断**：

```
ERROR: 火山引擎翻译服务签名验证失败！
DEBUG: 翻译服务签名错误，已禁用，返回原文
```

**解决方案**：

1. 检查密钥配置（参考：`火山引擎翻译配置说明.md`）
2. 验证账号余额和配额
3. 临时禁用翻译：不配置密钥，系统自动降级

**配置检查**：

```yaml
volcengine:
  translate:
    access-key: YOUR_ACCESS_KEY
    secret-key: YOUR_SECRET_KEY
    region: cn-beijing
```

#### 场景 4：数据库字段调整

**问题**：需要添加新字段或修改现有字段

**步骤**：

1. 修改实体类（添加字段）
2. 创建数据库迁移 SQL
3. 更新爬虫的数据映射逻辑
4. 测试数据保存

**示例**：

```java
// 1. 实体类添加字段
@Column(name = "new_field")
private String newField;

// 2. 爬虫添加映射
entity.setNewField(src.getNewFieldValue());

// 3. SQL 迁移
ALTER TABLE t_device_registration ADD COLUMN new_field VARCHAR(255);
```

#### 场景 5：性能优化

**常见问题**：
- 爬取速度慢
- 数据库保存慢
- 内存占用高

**优化方案 1：批量保存**

```java
// 优化前：逐条保存
for (Data data : dataList) {
    repository.save(convertToEntity(data));
}

// 优化后：批量保存
List<Entity> entities = dataList.stream()
    .map(this::convertToEntity)
    .collect(Collectors.toList());
repository.saveAll(entities);
```

**优化方案 2：分批处理**

```java
int BATCH_SIZE = 50;
for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
    int end = Math.min(i + BATCH_SIZE, dataList.size());
    List<Data> batch = dataList.subList(i, end);
    processBatch(batch);
}
```

**优化方案 3：增加重试机制**

```java
private Document fetchWithRetry(String url, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            return Jsoup.connect(url)
                .timeout(30000)
                .get();
        } catch (IOException e) {
            if (i == maxRetries - 1) throw new RuntimeException(e);
            try {
                Thread.sleep(2000 * (i + 1));  // 指数退避
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    return null;
}
```

---

## AI 模块

### AI 服务概述

系统集成了两种 AI 服务：

1. **OpenAI GPT-4o**：用于数据相关性判断和风险评估
2. **火山引擎翻译 API**：用于韩文等多语言翻译

### GPT-4o 配置

**配置文件**：`application.yml`

```yaml
openai:
  api:
    key: ${OPENAI_API_KEY}      # 从环境变量获取
    model: gpt-4o               # 模型版本
    base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
    timeout: 60                 # 超时时间（秒）
```

**环境变量**：

```env
OPENAI_API_KEY=sk-your-api-key-here
OPENAI_BASE_URL=https://api.openai.com/v1  # 可配置代理地址
```

### 火山引擎翻译配置

**配置文件**：`application.yml`

```yaml
volcengine:
  translate:
    access-key: ${VOLCENGINE_ACCESS_KEY}
    secret-key: ${VOLCENGINE_SECRET_KEY}
    region: cn-beijing
```

**详细配置说明**：参考 `火山引擎翻译配置说明.md`

### 认证新闻数据 AI 判断

#### 输入数据

- `title`：新闻标题
- `content`：新闻内容
- `summary`：新闻摘要
- `country`：国家/地区

#### Prompt 模板

系统使用精心设计的 Prompt，判断新闻是否与无线电子设备认证标准相关。

**关键判断标准**：

1. 明确提到以下认证之一：
   - FCC 认证（美国）
   - RoHS 认证（限制有害物质）
   - SRRC 认证（中国无线设备型号核准）
   - CE/RED 认证（欧盟）
   - EN 18031 认证（欧盟无线电设备网络安全）

2. 涉及安全/技术标准：
   - GB4706.1（家用电器安全通用要求）
   - GB4706.15（皮肤及毛发护理器具安全）
   - EN 301 489、EN 300 328（无线设备 EMC/射频标准）

3. 认证相关动态：
   - 设备通过/更新认证
   - 认证标准发布/修订
   - 认证机构公告/要求变更

#### 输出格式

```json
{
  "isRelated": true,
  "confidence": 0.9,
  "reason": "新闻涉及 RoHS 认证标准的更新，属于无线电子设备认证标准范畴",
  "extractedKeywords": ["RoHS", "GB 26572-2025", "SAMR"]
}
```

#### 实现位置

- 文件：`com/certification/service/ai/AISmartAuditService.java`
- 方法：`buildCertificationPrompt()`

### 医疗设备数据 AI 判断

#### 数据类型与输入字段

| 数据类型 | 输入字段 | 判断重点 |
|---------|---------|---------|
| 510K 申请 | deviceName, manufacturer, intendedUse | 是否为皮肤检测设备 |
| 注册记录 | deviceName, manufacturer, proprietaryName | 设备类型和用途 |
| 召回记录 | deviceName, manufacturer, recallReason | 产品相关性 |
| 事件报告 | deviceName, manufacturer, eventType | 设备分类 |
| 指导文档 | title, description | 法规相关性 |
| 海关案例 | hsCode, caseInfo, description | HS 编码归类 |

#### Prompt 策略

系统为不同数据类型设计了专门的 Prompt：

**1. 设备数据 Prompt**（默认）

判断设备是否为测肤仪/皮肤分析仪相关，包括：
- 皮肤成像系统（Skin Imaging System）
- 面部分析仪（Facial Analysis Device）
- 皮肤镜（Dermatoscope）
- 3D 皮肤扫描仪（3D Skin Scanner）
- 色素检测设备（Pigmentation Detector）
- 皮肤弹性/水分测试仪

**2. 指导文档 Prompt**

判断法规文档是否与测肤仪相关：
- 皮肤检测、分析、成像设备的法规
- 皮肤科医疗设备的监管指南
- 面部成像、皮肤镜等设备的标准
- 皮肤健康评估设备的技术要求

**3. 海关案例 Prompt**

判断海关案例是否涉及测肤仪，关注 HS 编码：
- 9018：医疗仪器及器具
- 8543：具有独立功能的电气设备
- 9031.49：光学测量或检验仪器
- 9027：分析检验仪器
- 8525：图像采集设备

#### 实现位置

- 文件：`com/certification/service/ai/strategy/`
  - `ApplicationJudgeStrategy.java`（510K）
  - `RegistrationJudgeStrategy.java`（注册记录）
  - `RecallJudgeStrategy.java`（召回记录）
  - `EventJudgeStrategy.java`（事件报告）
  - `DocumentJudgeStrategy.java`（指导文档）
  - `CustomsJudgeStrategy.java`（海关案例）

### AI 翻译服务

#### 使用场景

爬取韩国等非英文数据源时，自动翻译为英文：

**翻译字段**：
- `deviceName`：设备名称
- `manufacturer`：制造商
- `description`：描述

**翻译结果格式**：

```
原文 + 翻译
例如：(주)준영메디칼 -> (주)준영메디칼 Junyoung Medical Co., Ltd.
```

#### 实现代码

**文件位置**：`com/certification/analysis/analysisByai/TranslateAI.java`

**核心方法**：

```java
/**
 * 翻译单个文本
 * @param text 待翻译的文本
 * @param sourceLanguage 源语言代码（如 "ko"）
 * @param targetLanguage 目标语言代码（如 "en"）
 * @return 翻译后的文本，失败时返回原文
 */
public String translateSingleText(String text, String sourceLanguage, String targetLanguage) {
    if (text == null || text.trim().isEmpty()) {
        return text;
    }

    // 如果签名错误，直接返回原文
    if (signatureError) {
        log.debug("翻译服务签名错误，已禁用，返回原文");
        return text;
    }

    if (!serviceAvailable || translateApi == null) {
        log.debug("翻译服务未初始化或不可用，返回原文");
        return text;
    }

    try {
        TranslateTextRequest request = new TranslateTextRequest();
        request.setSourceLanguage(sourceLanguage);
        request.setTargetLanguage(targetLanguage);
        request.setTextList(Arrays.asList(text));

        TranslateTextResponse response = translateApi.translateText(request);

        if (response != null && response.getTranslationList() != null
            && !response.getTranslationList().isEmpty()) {
            String translation = response.getTranslationList().get(0).getTranslation();
            if (translation != null && !translation.trim().isEmpty()) {
                log.debug("翻译成功：{} -> {}", text, translation);
                return translation;
            }
        }
    } catch (ApiException e) {
        if (isSignatureError(e)) {
            signatureError = true;
            serviceAvailable = false;
            log.error("火山引擎翻译服务签名验证失败！密钥配置错误，已禁用翻译功能。");
            log.error("详细说明请查看：spring-boot-backend/火山引擎翻译配置说明.md");
        }
    }

    return text;  // 失败时返回原文
}
```

#### 错误处理

**签名错误**：
- 检测到签名错误后，自动禁用翻译服务
- 后续请求直接返回原文，不再调用 API
- 避免重复失败和 API 配额消耗

**降级策略**：
- 如果未配置翻译密钥，系统自动跳过翻译
- 不影响爬虫的正常执行

---

## 数据库管理

### 数据库架构

#### 核心数据表

**1. 设备数据表**

```sql
-- 510K 申请记录
CREATE TABLE t_device_510k (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    k_number VARCHAR(50) UNIQUE,
    device_name VARCHAR(500),
    manufacturer VARCHAR(500),
    decision_date DATE,
    product_code VARCHAR(20),
    risk_level VARCHAR(20),
    ai_judgment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 设备注册记录
CREATE TABLE t_device_registration_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_number VARCHAR(100) UNIQUE,
    device_name VARCHAR(500),
    manufacturer VARCHAR(500),
    country_code VARCHAR(10),
    registration_date DATE,
    risk_level VARCHAR(20),
    ai_judgment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 召回记录
CREATE TABLE t_device_recall_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recall_number VARCHAR(100) UNIQUE,
    device_name VARCHAR(500),
    manufacturer VARCHAR(500),
    recall_date DATE,
    recall_reason TEXT,
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 事件报告
CREATE TABLE t_device_event_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_number VARCHAR(100) UNIQUE,
    device_name VARCHAR(500),
    manufacturer VARCHAR(500),
    event_date DATE,
    event_type VARCHAR(100),
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 指导文档
CREATE TABLE t_guidance_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_number VARCHAR(100) UNIQUE,
    title VARCHAR(1000),
    description TEXT,
    publish_date DATE,
    country_code VARCHAR(10),
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 海关案例
CREATE TABLE t_customs_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_number VARCHAR(100) UNIQUE,
    hs_code VARCHAR(20),
    product_description TEXT,
    ruling_date DATE,
    country_code VARCHAR(10),
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**2. 新闻数据表**

```sql
CREATE TABLE t_cert_news_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(1000),
    content TEXT,
    summary TEXT,
    source VARCHAR(100),
    country VARCHAR(50),
    publish_date DATE,
    url VARCHAR(1000) UNIQUE,
    risk_level VARCHAR(20),
    ai_judgment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**3. 任务管理表**

```sql
-- 任务配置
CREATE TABLE t_unified_task_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) UNIQUE,
    crawler_name VARCHAR(100),
    cron_expression VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    preset_params JSON,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 任务执行日志
CREATE TABLE t_unified_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    crawler_name VARCHAR(100),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20),
    success_count INT,
    error_count INT,
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES t_unified_task_config(id)
);
```

**4. 关键词管理表**

```sql
CREATE TABLE t_device_match_keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(200),
    keyword_type VARCHAR(50),
    is_blacklist BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 数据库优化

#### 索引优化

**已添加的性能索引**（参考 `V1_0_1__add_performance_indexes.sql`）：

```sql
-- 设备 510K 表索引
CREATE INDEX idx_510k_device_name ON t_device_510k(device_name);
CREATE INDEX idx_510k_manufacturer ON t_device_510k(manufacturer);
CREATE INDEX idx_510k_decision_date ON t_device_510k(decision_date);
CREATE INDEX idx_510k_risk_level ON t_device_510k(risk_level);

-- 注册记录表索引
CREATE INDEX idx_reg_device_name ON t_device_registration_record(device_name);
CREATE INDEX idx_reg_manufacturer ON t_device_registration_record(manufacturer);
CREATE INDEX idx_reg_country ON t_device_registration_record(country_code);
CREATE INDEX idx_reg_date ON t_device_registration_record(registration_date);

-- 召回记录表索引
CREATE INDEX idx_recall_device_name ON t_device_recall_record(device_name);
CREATE INDEX idx_recall_date ON t_device_recall_record(recall_date);

-- 任务日志表索引
CREATE INDEX idx_log_task_id ON t_unified_task_log(task_id);
CREATE INDEX idx_log_start_time ON t_unified_task_log(start_time);
CREATE INDEX idx_log_status ON t_unified_task_log(status);
```

#### 查询优化建议

**1. 使用索引字段查询**

```sql
-- 好的查询（使用索引）
SELECT * FROM t_device_510k
WHERE decision_date >= '2024-01-01'
AND risk_level = 'HIGH';

-- 不好的查询（全表扫描）
SELECT * FROM t_device_510k
WHERE YEAR(decision_date) = 2024;
```

**2. 分页查询**

```sql
-- 使用 LIMIT 和 OFFSET
SELECT * FROM t_device_registration_record
WHERE country_code = 'US'
ORDER BY registration_date DESC
LIMIT 100 OFFSET 0;
```

**3. 统计查询**

```sql
-- 按国家统计设备数量
SELECT country_code, COUNT(*) as count
FROM t_device_registration_record
GROUP BY country_code;

-- 按风险等级统计
SELECT risk_level, COUNT(*) as count
FROM t_device_510k
WHERE decision_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY risk_level;
```

### 数据备份

#### 自动备份脚本

**备份命令**：

```bash
#!/bin/bash
# 数据库备份脚本

BACKUP_DIR="/path/to/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="common_db"
DB_USER="cert_user"
DB_PASS="your_password"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# 删除 30 天前的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "数据库备份完成: backup_$DATE.sql.gz"
```

**设置定时任务**：

```bash
# 编辑 crontab
crontab -e

# 每天凌晨 3 点执行备份
0 3 * * * /path/to/backup_script.sh
```

#### 恢复数据

```bash
# 解压备份文件
gunzip backup_20241220_030000.sql.gz

# 恢复数据库
mysql -u cert_user -p common_db < backup_20241220_030000.sql
```

### 数据清理

#### 清理过期数据

```sql
-- 删除 1 年前的任务日志
DELETE FROM t_unified_task_log
WHERE created_at < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

-- 删除已标记为低风险且超过 6 个月的数据
DELETE FROM t_cert_news_data
WHERE risk_level = 'LOW'
AND created_at < DATE_SUB(CURDATE(), INTERVAL 6 MONTH);
```

#### 清理重复数据

```sql
-- 查找重复的 510K 记录
SELECT k_number, COUNT(*) as count
FROM t_device_510k
GROUP BY k_number
HAVING count > 1;

-- 删除重复记录（保留最新的）
DELETE t1 FROM t_device_510k t1
INNER JOIN t_device_510k t2
WHERE t1.k_number = t2.k_number
AND t1.id < t2.id;
```

---

## 部署指南

### 容器化部署（推荐）

系统支持 Docker 和 Podman 两种容器化方案：

#### Docker 部署

**1. 准备工作**

```bash
# 克隆项目
git clone https://github.com/steteve6-tech/EveIndex.git
cd EveIndex

# 创建环境配置文件
cp .env.prod.example .env.prod
vim .env.prod  # 编辑配置
```

**2. 构建镜像**

```bash
# 使用构建脚本
chmod +x docker-build.sh
./docker-build.sh

# 或手动构建
docker-compose -f docker-compose.prod.yml build
```

**3. 启动服务**

```bash
# 使用部署脚本
chmod +x deploy.sh
./deploy.sh

# 或手动启动
docker-compose -f docker-compose.prod.yml up -d
```

**4. 查看日志**

```bash
# 查看所有服务日志
docker-compose -f docker-compose.prod.yml logs -f

# 查看后端日志
docker-compose -f docker-compose.prod.yml logs -f backend

# 查看前端日志
docker-compose -f docker-compose.prod.yml logs -f frontend
```

**5. 停止服务**

```bash
docker-compose -f docker-compose.prod.yml down

# 停止并删除数据卷（慎用）
docker-compose -f docker-compose.prod.yml down -v
```

#### Podman Desktop 部署

详细部署指南请参考：[PODMAN_DEPLOYMENT_GUIDE.md](./PODMAN_DEPLOYMENT_GUIDE.md)

**快速开始**：

```bash
# 1. 配置环境变量
cp .env.prod.example .env.prod
# 编辑 .env.prod 填入真实配置

# 2. 构建并启动
podman-compose -f docker-compose.prod.yml up -d --build

# 3. 查看状态
podman ps

# 4. 查看日志
podman-compose -f docker-compose.prod.yml logs -f
```

**Podman 优势**：
- 无需守护进程，更安全
- 兼容 Docker 命令
- 适合企业环境

### 手动部署

#### 后端部署

**1. 环境要求**

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

**2. 构建项目**

```bash
cd spring-boot-backend
mvn clean package -DskipTests
```

**3. 配置数据库**

```sql
CREATE DATABASE common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cert_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON common_db.* TO 'cert_user'@'%';
FLUSH PRIVILEGES;
```

**4. 启动应用**

```bash
java -jar target/certification-monitor-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/common_db \
  --spring.datasource.username=cert_user \
  --spring.datasource.password=your_password
```

#### 前端部署

**1. 环境要求**

- Node.js 16+
- npm 或 yarn

**2. 构建项目**

```bash
cd vue-frontend
npm install
npm run build
```

**3. 部署静态文件**

```bash
# 使用 Nginx
cp -r dist/* /var/www/html/

# 或使用其他 Web 服务器
```

**4. Nginx 配置**

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 健康检查

```bash
# 检查后端服务
curl http://localhost:8080/actuator/health

# 检查数据库连接
curl http://localhost:8080/actuator/health/db

# 检查 Redis 连接
curl http://localhost:8080/actuator/health/redis
```

---

## 性能优化

### JVM 优化

**推荐 JVM 参数**：

```bash
JAVA_OPTS="-Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heapdump.hprof"
```

### 数据库连接池优化

**Druid 配置**：

```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
```

### 缓存优化

**Redis 缓存策略**：

```java
@Cacheable(value = "deviceData", key = "#id")
public Device getDeviceById(Long id) {
    return deviceRepository.findById(id).orElse(null);
}

@CacheEvict(value = "deviceData", key = "#device.id")
public Device updateDevice(Device device) {
    return deviceRepository.save(device);
}
```

### 爬虫性能优化

**1. 并发控制**

```java
// 使用线程池控制并发
ExecutorService executor = Executors.newFixedThreadPool(5);
for (String keyword : keywords) {
    executor.submit(() -> crawl(keyword));
}
```

**2. 限流**

```java
// 使用 Google Guava RateLimiter
RateLimiter rateLimiter = RateLimiter.create(2.0); // 每秒 2 个请求
rateLimiter.acquire();
makeRequest();
```

---

## 安全建议

### 1. 密码安全

- 使用强密码（至少 16 位，包含大小写字母、数字、特殊字符）
- 定期更换密钥和密码
- 不要将 `.env.prod` 文件提交到版本控制系统

### 2. API 密钥管理

- 使用环境变量存储 API 密钥
- 定期轮换 API 密钥
- 监控 API 使用量，防止滥用

### 3. 生产环境配置

- 关闭 Swagger API 文档（`SWAGGER_ENABLED=false`）
- 配置 Druid 监控白名单（`DRUID_ALLOW_IP`）
- 启用 HTTPS
- 配置防火墙规则

### 4. 数据库安全

- 不使用 root 账户连接数据库
- 限制数据库用户权限
- 定期备份数据库
- 启用慢查询日志

### 5. 应用安全

- 定期更新依赖包
- 使用安全的 HTTP 头（CSP、HSTS 等）
- 实施速率限制
- 记录审计日志

---

## 故障排查

### 常见问题

#### 1. 数据库连接失败

**现象**：

```
java.sql.SQLException: Access denied for user 'cert_user'@'localhost'
```

**解决方案**：

1. 检查数据库配置
2. 验证用户名和密码
3. 确认数据库权限

```bash
mysql -u cert_user -p
SHOW GRANTS FOR 'cert_user'@'%';
```

#### 2. 爬虫无法访问目标网站

**现象**：

```
java.net.SocketTimeoutException: connect timed out
```

**解决方案**：

1. 检查网络连接
2. 验证目标网站是否正常
3. 增加超时时间
4. 配置代理（如需要）

#### 3. AI 服务调用失败

**现象**：

```
OpenAI API error: Unauthorized
```

**解决方案**：

1. 检查 API 密钥是否正确
2. 验证 API 配额是否充足
3. 检查 API 基础 URL 是否正确

#### 4. 翻译服务签名错误

**现象**：

```
ERROR: 火山引擎翻译服务签名验证失败！
```

**解决方案**：

1. 检查 `access-key` 和 `secret-key` 是否正确
2. 验证 `region` 配置
3. 参考文档：`火山引擎翻译配置说明.md`

#### 5. 内存溢出

**现象**：

```
java.lang.OutOfMemoryError: Java heap space
```

**解决方案**：

1. 增加 JVM 堆内存：`-Xmx2048m`
2. 优化爬虫批量处理逻辑
3. 检查内存泄漏

### 日志分析

#### 查看错误日志

```bash
# 查看最近的错误
grep "ERROR" logs/certification-monitor.log | tail -50

# 统计错误类型
grep "ERROR" logs/certification-monitor.log | awk '{print $6}' | sort | uniq -c
```

#### 查看爬虫执行日志

```bash
# 查看特定爬虫的执行记录
grep "US_510K" logs/certification-monitor.log | tail -100

# 查看翻译服务日志
grep "翻译" logs/certification-monitor.log
```

---

## 联系与支持

### 项目信息

- **项目名称**：医疗器械认证监控系统
- **版本**：v2.0.0
- **技术支持**：开发团队
- **问题反馈**：通过项目管理平台提交

### 相关文档

**部署与运维**：
- `PODMAN_DEPLOYMENT_GUIDE.md` - Podman Desktop 部署指南（新增）
- `SYSTEM_MAINTENANCE_GUIDE.md` - 系统维护文档（本文档）

**开发文档**：
- `爬虫维护与拓展指南.md` - 爬虫开发指南
- `AI模块维护文档.md` - AI 功能详细说明
- `火山引擎翻译配置说明.md` - 翻译服务配置
- `日志系统介绍文档.md` - 日志系统说明

**用户手册**：
- `README.md` - 项目总览和快速开始
- `认证预警系统使用与维护文档.docx` - 完整使用手册
- `爬虫调度管理平台-README.md` - 爬虫管理平台说明

---

## 附录

### 数据流程图

```
┌─────────────┐
│  定时任务   │
│  触发器     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  爬虫引擎   │
│  - HTTP请求 │
│  - 数据解析 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  翻译服务   │◄─────── 火山引擎 API
│  (可选)     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  数据存储   │
│  MySQL      │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  AI 审计    │◄─────── OpenAI GPT-4o
│  服务       │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  风险标记   │
│  更新       │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  前端展示   │
└─────────────┘
```

### 技术栈版本清单

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS 版本 |
| Spring Boot | 3.2.0 | 最新稳定版 |
| Vue | 3.4.0 | Composition API |
| TypeScript | 5.3.0 | 类型安全 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Ant Design Vue | 4.2.6 | UI 框架 |
| ECharts | 5.4.3 | 图表库 |
| Vite | 6.0.1 | 构建工具 |

---

**文档版本**：v2.0.0
**最后更新**：2025-01-20
**维护者**：开发团队

---

## 版本更新记录

### v2.0.0 (2025-01-20)

**新增功能**：
- ✨ 新增 Podman Desktop 部署支持
- ✨ 新增台湾 TFDA 数据源（4 个爬虫）
- ✨ 新增日本 PMDA 完整数据支持（3 个爬虫）
- ✨ 新增中国 NMPA 数据源（2 个爬虫）
- ✨ 集成 Tesseract OCR 验证码识别
- ✨ 新增 2Captcha 验证码识别服务

**优化改进**：
- 🔧 优化爬虫调度平台性能
- 🔧 完善 AI 智能判断准确度
- 🔧 优化数据库连接池配置
- 🔧 改进火山引擎翻译错误处理
- 🔧 优化前端界面响应速度
- 📚 更新所有部署和维护文档

**问题修复**：
- 🐛 修复韩国爬虫翻译签名错误
- 🐛 修复部分爬虫数据重复问题
- 🐛 修复定时任务执行异常
- 🐛 修复数据库连接池泄漏

### v1.0.0 (2024-09-30)

**初始发布**：
- 🎉 项目初始化
- ✨ 美国 FDA 数据源（7 个爬虫）
- ✨ 韩国 MFDS 数据源（5 个爬虫）
- ✨ 欧盟 EUDAMED 数据源（5 个爬虫）
- ✨ 认证新闻爬虫（3 个来源）
- ✨ AI 智能审核功能
- ✨ 火山引擎翻译集成
- ✨ 统一爬虫调度平台
