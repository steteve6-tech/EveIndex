# AI判断延迟确认系统 + 关键词统计优化 - 实施进度

## ✅ 已完成部分 (2025-10-21)

### 1. 数据库层
- ✅ 创建SQL迁移脚本 `database/migration_ai_judgment_and_keyword_stats.sql`
  - 创建 `t_ai_judgment_pending` 表（AI判断待审核）
  - 为所有数据实体表添加 `is_new` 和 `new_data_viewed` 字段
  - 扩展 `t_unified_task_log` 表添加 `keyword_statistics` 字段
  - 创建必要的索引优化查询性能

### 2. 实体层
- ✅ 创建 `AIPendingJudgment.java` 实体
  - 支持两种模块类型（DEVICE_DATA, CERT_NEWS）
  - 支持四种状态（PENDING, CONFIRMED, REJECTED, EXPIRED）
  - 自动设置30天过期时间
  - 包含黑名单关键词字段（JSON格式）

### 3. Repository层
- ✅ 创建 `AIPendingJudgmentRepository.java`
  - 按模块类型和状态查询
  - 统计待审核数量（总数、按实体类型）
  - 查找过期记录
  - 批量更新状态
  - 查询黑名单过滤和高风险记录

### 4. Service层
- ✅ 创建 `PendingAIJudgmentService.java`
  - 获取待审核列表和数量统计
  - 确认/拒绝单个判断
  - 批量确认操作
  - 执行AI判断结果（调用Strategy）
  - 清理过期记录
  - 设备数据统计（黑名单过滤、高风险、新增关键词）

---

## 📋 待实施部分

### Phase 1: 后端完善（预计1-2天）

#### 1.1 修改AI判断服务
- [ ] 修改 `AutoAIJudgeService.java`
  - 将 `judgeNewData()` 改为保存到 `t_ai_judgment_pending`
  - 不再立即调用 `updateEntityWithJudgeResult()`
  - 记录黑名单关键词和过滤信息

- [ ] 修改 `CertNewsAIJudgeService.java`
  - 保存判断结果到待审核表
  - 无需记录黑名单相关信息

#### 1.2 创建新增数据统计服务
- [ ] `NewDataStatisticsService.java`
  - `getNewDataCount(entityType)` - 获取新增数据数量
  - `markDataAsViewed(entityType, ids)` - 标记为已查看
  - `getNewDataList(entityType)` - 获取新增数据列表

#### 1.3 创建Controller APIs
- [ ] `PendingAIJudgmentController.java`
  ```java
  GET  /api/ai-judgment/pending?moduleType=DEVICE_DATA
  GET  /api/ai-judgment/pending/count?moduleType=DEVICE_DATA
  GET  /api/ai-judgment/pending/statistics/device-data
  GET  /api/ai-judgment/pending/{id}
  POST /api/ai-judgment/confirm/{id}
  POST /api/ai-judgment/batch-confirm
  POST /api/ai-judgment/reject/{id}
  ```

- [ ] `NewDataController.java`
  ```java
  GET  /api/new-data/count?entityType=Recall
  GET  /api/new-data/list?entityType=Recall
  POST /api/new-data/mark-viewed
  ```

#### 1.4 创建定时任务
- [ ] `ScheduledCleanupJob.java`
  - 每天凌晨清理30天前未确认的待审核记录
  - 清理已查看超过7天的新增数据标记

---

### Phase 2: 关键词统计（预计2-3天）

#### 2.1 扩展CrawlerResult
- [ ] 添加字段 `Map<String, Map<String, Integer>> keywordStatistics`
- [ ] 添加相关getter/setter

#### 2.2 修改爬虫底层逻辑
需要修改以下爬虫文件，在爬取过程中收集关键词统计：
- [ ] `US_recall_api.java` - 修改 `crawlAndSaveWithMultipleFields()`
- [ ] `US_510K.java`
- [ ] `US_registration.java`
- [ ] `US_event_api.java`
- [ ] `US_Guidance.java`
- [ ] 其他国家爬虫（KR, JP, TW等）

返回结构示例：
```java
Map<String, Map<String, Integer>> stats = new HashMap<>();
stats.put("brandNames", Map.of("Medtronic", 45, "Boston Scientific", 23));
stats.put("recallingFirms", Map.of("Johnson & Johnson", 18));
```

#### 2.3 修改Adapter层
- [ ] `USRecallAdapter.java` - 接收并传递关键词统计
- [ ] 其他所有Adapter类

#### 2.4 修改UnifiedCrawlerServiceImpl
- [ ] `executeTask()` 方法
  - 从 `CrawlerResult` 获取关键词统计
  - 序列化为JSON
  - 保存到 `UnifiedTaskLog.keywordStatistics`

#### 2.5 创建统计API
- [ ] `KeywordStatisticsController.java`
  ```java
  GET /api/keyword-stats/{taskLogId}
  GET /api/keyword-stats/trends?crawlerName=US_Recall&keyword=Medtronic
  ```

---

### Phase 3: 前端实现（预计3-4天）

#### 3.1 AI判断确认弹窗
- [ ] 创建 `AIJudgmentReviewPopup.vue`
  - 设备数据版本（显示黑名单过滤、高风险、新增关键词）
  - 医疗认证版本（仅显示AI判断结果）
  - 支持批量确认/拒绝

#### 3.2 修改现有页面
- [ ] `DeviceData.vue`
  - 添加顶部通知栏（待审核数量、新增数据数量）
  - 添加"待审核AI判断"按钮（徽章）
  - 添加"新增数据"筛选器
  - 集成弹窗组件

- [ ] `CertNewsTaskManagement.vue`
  - 同上（无黑名单相关）

#### 3.3 关键词统计展示
- [ ] 创建 `KeywordStatisticsChart.vue`
  - 柱状图/饼图展示关键词数量
  - 支持导出数据

- [ ] 修改 `UnifiedCrawlerManagement.vue`
  - 任务执行历史中添加"关键词统计"列
  - 点击展开查看详细统计

- [ ] 优化 `CrawlerPresetEditor.vue`
  - 编辑关键词时显示历史统计
  - 高亮高效关键词、标记低效关键词

#### 3.4 全局通知
- [ ] 修改 `Layout.vue`
  - 用户登录后自动检查待审核数量和新增数据
  - 显示系统通知或弹窗提醒

---

## 🔧 下一步操作

### 立即执行：
1. **运行数据库迁移脚本**
   ```bash
   mysql -u root -p common_db < database/migration_ai_judgment_and_keyword_stats.sql
   ```

2. **编译测试后端**
   ```bash
   cd spring-boot-backend
   mvn clean compile
   ```

3. **继续实施Phase 1**
   - 修改 `AutoAIJudgeService.java`
   - 创建 Controller APIs

---

## 📊 整体进度

| 阶段 | 任务 | 状态 | 完成度 |
|------|------|------|--------|
| 数据库设计 | 表结构和字段 | ✅ 完成 | 100% |
| 实体和Repository | AIPendingJudgment | ✅ 完成 | 100% |
| Service层 | PendingAIJudgmentService | ✅ 完成 | 100% |
| AI判断服务修改 | Auto/CertNews AI判断 | ⏳ 待实施 | 0% |
| Controller APIs | 后端API接口 | ⏳ 待实施 | 0% |
| 定时任务 | 清理过期记录 | ⏳ 待实施 | 0% |
| 关键词统计后端 | Crawler修改 | ⏳ 待实施 | 0% |
| 前端组件 | 弹窗和统计图表 | ⏳ 待实施 | 0% |
| 前端页面集成 | DeviceData等页面 | ⏳ 待实施 | 0% |

**总体进度: 约 25%**

---

## 💡 技术要点提醒

### AI判断服务修改注意事项
```java
// 原逻辑（立即执行）
strategy.updateEntityWithJudgeResult(data, judgeResult);
saveEntity(data, moduleName);

// 新逻辑（保存到待审核）
AIPendingJudgment pending = new AIPendingJudgment();
pending.setModuleType("DEVICE_DATA");
pending.setEntityType(moduleName);
pending.setEntityId(getEntityId(data));
pending.setJudgeResult(objectMapper.writeValueAsString(judgeResult));
pending.setSuggestedRiskLevel(judgeResult.isRelated() ? "HIGH" : "LOW");
// ... 设置其他字段
pendingJudgmentRepository.save(pending);
```

### 关键词统计收集示例
```java
// 在爬虫中收集统计
Map<String, Integer> brandStats = new HashMap<>();
for (String brandName : brandNames) {
    int count = crawlByBrandName(brandName);
    brandStats.put(brandName, count);
}
result.putKeywordStatistics("brandNames", brandStats);
```

---

## 📞 后续支持
如需继续实施，请告知优先级：
1. 优先完成AI判断确认功能
2. 优先完成关键词统计功能
3. 同时推进两个功能
