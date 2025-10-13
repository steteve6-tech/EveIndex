# 新国家爬虫接入指南

本指南提供了标准化的流程，用于快速接入新国家的爬虫到现有的爬虫任务管理系统。

## 📋 概述

爬虫任务管理系统支持统一管理多个国家、多种数据类型的爬虫。当需要接入新国家的数据时，只需遵循本指南的5个标准步骤即可快速集成。

## 🎯 接入流程（5步法）

### 第1步：开发爬虫类 📝

#### 1.1 创建爬虫文件

**位置**：`spring-boot-backend/src/main/java/com/certification/crawler/countrydata/{国家代码}/`

**命名规范**：`{国家代码}_{数据类型}.java`

**示例**：
```
日本510K爬虫：JP_510K.java
日本召回爬虫：JP_Recall.java
```

#### 1.2 必须实现的方法

```java
package com.certification.crawler.countrydata.jp;

@Slf4j
@Component
public class JP_Recall {
    
    @Autowired
    private DeviceRecallRecordRepository recallRepository;
    
    @Autowired
    private TranslateAI translateAI;  // 如需翻译
    
    /**
     * ✅ 必须实现：基于关键词列表的批量爬取
     * 
     * @param keywords 关键词列表
     * @param maxRecords 最大记录数（-1表示全部）
     * @param batchSize 批次大小
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 爬取结果描述
     */
    @Transactional
    public String crawlAndSaveWithKeywords(
        List<String> keywords,
        int maxRecords,
        int batchSize,
        String dateFrom,
        String dateTo
    ) {
        log.info("开始爬取日本召回数据，关键词数量: {}", keywords.size());
        
        int totalSaved = 0;
        int totalSkipped = 0;
        
        for (String keyword : keywords) {
            try {
                // 1. 爬取数据
                List<JapanRecallData> dataList = fetchDataByKeyword(keyword, dateFrom, dateTo);
                
                // 2. 转换为实体
                List<DeviceRecallRecord> entities = convertToEntities(dataList);
                
                // 3. 去重并保存
                for (DeviceRecallRecord entity : entities) {
                    if (!recallRepository.existsByCfresId(entity.getCfresId())) {
                        recallRepository.save(entity);
                        totalSaved++;
                    } else {
                        totalSkipped++;
                    }
                }
                
                // 添加延迟避免被封
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("关键词 {} 爬取失败", keyword, e);
            }
        }
        
        return String.format("爬取完成：保存 %d 条，跳过 %d 条", totalSaved, totalSkipped);
    }
    
    /**
     * ✅ 简化版本（无时间范围）
     */
    public String crawlAndSaveWithKeywords(List<String> keywords, int maxRecords, int batchSize) {
        return crawlAndSaveWithKeywords(keywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * ✅ 关键：转换为标准实体
     */
    private DeviceRecallRecord convertToEntity(JapanRecallData src) {
        DeviceRecallRecord entity = new DeviceRecallRecord();
        
        // ⭐ 必须设置：国家标识
        entity.setJdCountry("JP");
        entity.setCountryCode("JP");
        entity.setDataSource("日本数据源名称");
        
        // ⭐ 必须设置：唯一标识
        entity.setCfresId("JP_" + src.getRecallId());
        
        // ⭐ 必须设置：爬取时间
        entity.setCrawlTime(LocalDateTime.now());
        
        // 业务字段
        entity.setProductDescription(src.getProductDescription());
        entity.setRecallingFirm(src.getCompany());
        entity.setDeviceName(src.getDeviceName());
        
        // ⭐ 翻译非英文字段（如果需要）
        if (translateAI != null && !isEnglish(src.getDeviceName())) {
            String translated = translateAI.translateSingleTextAuto(src.getDeviceName());
            entity.setDeviceName(translated);
            
            // 保存原文到remark
            entity.setRemark("原文: " + src.getDeviceName());
        }
        
        // 风险等级和关键词
        entity.setRiskLevel(calculateRiskLevel(src));
        entity.setKeywords(extractKeywords(src));
        
        return entity;
    }
}
```

#### 1.3 必须遵循的规范

| 规范项 | 要求 | 说明 |
|--------|------|------|
| **国家标识** | 必须设置 `jdCountry` 和 `countryCode` | 用于区分数据来源 |
| **数据源** | 必须设置 `dataSource` | 标识具体的数据来源网站 |
| **爬取时间** | 必须设置 `crawlTime` | 记录数据获取时间 |
| **唯一标识** | 必须设置唯一键（如cfresId、kNumber等） | 用于去重 |
| **去重机制** | 必须检查重复 | 避免重复保存 |
| **批量方法** | 必须实现 `crawlAndSaveWithKeywords` | 支持关键词列表 |
| **异常处理** | 必须捕获并记录 | 单个关键词失败不影响其他 |
| **翻译支持** | 非英文国家必须翻译 | 统一为英文或中文 |

### 第2步：创建爬虫适配器 🔌

#### 2.1 创建适配器文件

**位置**：`spring-boot-backend/src/main/java/com/certification/service/crawler/adapter/`

**命名规范**：`{国家代码}{类型}Adapter.java`

**示例**：
```java
package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.jp.JP_Recall;
import com.certification.entity.ScheduledCrawlerConfig;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日本召回数据爬虫适配器
 */
@Slf4j
@Component("JP_Recall_Adapter")  // ✅ Bean名称要唯一
public class JPRecallAdapter implements ICrawlerExecutor {
    
    @Autowired
    private JP_Recall crawler;
    
    @Override
    public String getCrawlerName() {
        return "JP_Recall";
    }
    
    @Override
    public String getCountryCode() {
        return "JP";
    }
    
    @Override
    public String getCrawlerType() {
        return ScheduledCrawlerConfig.CrawlerType.DEVICE_RECALL;
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行日本召回爬虫，关键词数量: {}", params.getKeywordCount());
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            String resultMessage = crawler.crawlAndSaveWithKeywords(
                params.getKeywords(),
                params.getMaxRecords(),
                params.getBatchSize(),
                params.getDateFrom(),
                params.getDateTo()
            );
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMessage);
            
            return CrawlerResult.fromString(resultMessage)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("日本召回爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null || params.getKeywords() == null || params.getKeywords().isEmpty()) {
            log.warn("参数验证失败：关键词列表为空");
            return false;
        }
        return true;
    }
}
```

#### 2.2 适配器模板（快速复制）

适配器代码模式固定，只需修改几处即可：
1. 包名和类名
2. @Component 名称
3. 注入的爬虫类
4. getCrawlerName()、getCountryCode()、getCrawlerType() 返回值

### 第3步：注册爬虫（自动） ✅

由于使用了 `@Component` 注解，适配器会**自动注册**到Spring容器。

`CrawlerRegistryService` 会在启动时自动扫描并注册所有 `ICrawlerExecutor` 的实现类。

**验证注册成功**：

1. 查看启动日志：
```
注册爬虫: JP_Recall (JP_RECALL) - 日本国家的RECALL类型数据爬虫
爬虫注册完成，共 12 个爬虫
```

2. 调用查询接口：
```bash
GET http://localhost:8080/api/crawler-tasks/available-crawlers
```

### 第4步：配置任务 ⚙️

#### 4.1 方式1：通过前端界面创建

1. 访问：`http://localhost:3000/#/crawler-task-management`
2. 点击"创建任务"
3. 填写表单：
   - 爬虫选择：JP_Recall
   - 国家代码：JP
   - 关键词：["医療機器", "リコール"]
   - Cron表达式：0 0 3 * * ?
   - 最大记录数：-1（全部）
   - 批次大小：50
4. 保存 → 自动开始调度

#### 4.2 方式2：通过API创建

```bash
POST http://localhost:8080/api/crawler-tasks
Content-Type: application/json

{
  "crawlerName": "JP_Recall",
  "countryCode": "JP",
  "taskType": "KEYWORD_BATCH",
  "keywords": "[\"医療機器\", \"リコール\", \"安全性\"]",
  "cronExpression": "0 0 3 * * ?",
  "enabled": true,
  "crawlParams": "{\"maxRecords\": -1, \"batchSize\": 50, \"recentDays\": 30}",
  "priority": 6,
  "timeoutMinutes": 60,
  "retryCount": 3,
  "description": "每天凌晨3点爬取日本召回数据"
}
```

#### 4.3 方式3：批量创建

```json
POST /api/crawler-tasks/batch-create

[
  {
    "crawlerName": "JP_Recall",
    "countryCode": "JP",
    "taskType": "KEYWORD_BATCH",
    "keywords": "[\"医療機器\"]",
    "cronExpression": "0 0 3 * * ?"
  },
  {
    "crawlerName": "JP_510K",
    "countryCode": "JP",
    "taskType": "KEYWORD_BATCH",
    "keywords": "[\"承認\"]",
    "cronExpression": "0 0 4 * * ?"
  }
]
```

### 第5步：测试验证 ✅

#### 5.1 测试检查清单

| 测试项 | 检查方法 | 预期结果 |
|--------|---------|---------|
| **爬虫可用性** | 调用爬虫的单个方法测试 | 能获取数据 |
| **适配器注册** | 查看启动日志或调用API | 出现在注册列表中 |
| **手动执行** | 点击"立即执行"按钮 | 任务成功执行 |
| **数据保存** | 查询数据库 | 有新记录 |
| **国家标识** | 检查 `jd_country` 字段 | 显示正确的国家代码 |
| **去重机制** | 重复执行任务 | 不重复保存 |
| **翻译功能** | 查看保存的数据 | 非英文已翻译 |
| **定时任务** | 等待Cron触发 | 按时自动执行 |
| **执行日志** | 查看任务执行历史 | 日志完整 |
| **错误处理** | 故意制造错误 | 正确捕获和记录 |

#### 5.2 测试SQL

**验证数据保存**：
```sql
-- 查询日本召回数据
SELECT * FROM t_device_recall 
WHERE jd_country = 'JP' 
ORDER BY crawl_time DESC 
LIMIT 10;

-- 验证数据完整性
SELECT 
    COUNT(*) as total,
    COUNT(DISTINCT cfres_id) as unique_records,
    jd_country,
    data_source
FROM t_device_recall
WHERE jd_country = 'JP'
GROUP BY jd_country, data_source;
```

**验证任务执行**：
```sql
-- 查看任务配置
SELECT * FROM t_scheduled_crawler_config 
WHERE country_code = 'JP';

-- 查看执行日志
SELECT * FROM t_crawler_task_log 
WHERE country_code = 'JP'
ORDER BY start_time DESC;
```

## 📚 完整接入示例（日本召回）

### 文件清单

```
✅ 1. 爬虫类
   📁 crawler/countrydata/jp/JP_Recall.java (300-500行)
   
✅ 2. 适配器类
   📁 service/crawler/adapter/JPRecallAdapter.java (60-80行)
   
✅ 3. 任务配置
   📊 通过前端界面或API创建
   
✅ 4. 测试类（可选）
   📁 test/.../JPRecallTest.java
```

### 代码量估算

| 项目 | 代码量 | 时间 |
|------|--------|------|
| 爬虫类开发 | 300-500行 | 4-6小时 |
| 适配器类 | 60-80行 | 0.5小时 |
| 任务配置 | 配置数据 | 0.5小时 |
| 测试验证 | 测试用例 | 1-2小时 |
| **总计** | - | **6-9小时** |

## 🎨 爬虫开发规范

### 数据类型映射

| 数据类型 | 实体类 | 表名 | 唯一标识字段 |
|---------|--------|------|------------|
| 510K | Device510K | t_device_510k | k_number |
| Event | DeviceEventReport | t_device_event | report_number |
| Recall | DeviceRecallRecord | t_device_recall | cfres_id |
| Registration | DeviceRegistrationRecord | t_device_registration | registration_number |
| Guidance | GuidanceDocument | t_guidance_document | document_url |
| Customs | CustomsCase | t_customs_case | case_number |

### 通用代码模板

**爬虫类模板**：
```java
@Slf4j
@Component
public class {国家}_{类型} {
    
    @Autowired
    private {对应Repository} repository;
    
    @Autowired
    private TranslateAI translateAI;
    
    public String crawlAndSaveWithKeywords(
        List<String> keywords,
        int maxRecords,
        int batchSize,
        String dateFrom,
        String dateTo
    ) {
        // 实现逻辑...
    }
    
    private {实体类型} convertToEntity({原始数据类型} src) {
        {实体类型} entity = new {实体类型}();
        
        // 必须设置
        entity.setJdCountry("{国家代码}");
        entity.setCountryCode("{国家代码}");
        entity.setDataSource("{数据源名称}");
        entity.setCrawlTime(LocalDateTime.now());
        
        // 业务字段映射...
        
        return entity;
    }
}
```

## 🚀 快速接入检查清单

接入新国家前，请确认：

- [ ] 确定数据来源网站和API
- [ ] 了解数据结构和字段映射
- [ ] 确认是否需要翻译（非英文国家）
- [ ] 准备测试关键词列表
- [ ] 确定定时执行时间
- [ ] 了解去重逻辑（唯一标识字段）

接入完成后，请验证：

- [ ] 爬虫能正常访问数据源
- [ ] 数据能正确保存到数据库
- [ ] 国家标识字段正确
- [ ] 去重机制生效
- [ ] 翻译功能正常（如适用）
- [ ] 适配器已注册
- [ ] 任务能正常执行
- [ ] 定时任务按时触发
- [ ] 执行日志完整
- [ ] 错误处理正确

## 📞 技术支持

如遇问题，请检查：

1. **爬虫不在注册列表**
   - 检查 `@Component` 注解是否存在
   - 确认类实现了 `ICrawlerExecutor` 接口
   - 查看启动日志中的注册信息

2. **任务执行失败**
   - 查看执行日志的错误信息
   - 检查关键词格式是否正确
   - 验证数据源是否可访问
   - 确认数据库连接正常

3. **数据未保存**
   - 检查去重逻辑
   - 确认唯一标识字段设置
   - 查看数据库日志

4. **翻译失败**
   - 确认翻译服务API密钥已配置
   - 检查网络连接
   - 查看翻译服务日志

## 🎉 总结

通过遵循这个标准化流程，接入一个新国家的完整数据支持（6个数据类型）大约需要 **2-3天** 的开发时间。

接入后的优势：
- ✅ 统一管理：在同一个界面管理所有国家
- ✅ 灵活调度：可独立设置每个国家的执行时间
- ✅ 关键词驱动：支持批量关键词爬取
- ✅ 数据统一：保存到相同的实体表，便于查询
- ✅ 自动监控：执行日志、统计信息自动记录

