# 🚀 爬虫任务管理系统

统一的多国家爬虫调度和管理平台，支持关键词批量爬取、动态定时任务、可视化监控。

## ✨ 核心特性

- 🌍 **多国家支持** - 美国、欧盟、韩国等11个爬虫统一管理
- 🔑 **关键词驱动** - 支持批量关键词列表爬取
- ⏰ **动态调度** - 可视化配置定时任务，运行时修改
- 📊 **实时监控** - 执行状态、日志、统计一目了然
- 🔌 **易于扩展** - 标准化流程，快速接入新国家数据

## 🎯 快速开始

### 1. 启动服务

```bash
# 后端
cd spring-boot-backend
mvn clean package
java -jar target/certification-monitor-0.0.1-SNAPSHOT.jar

# 前端
cd vue-frontend
npm install
npm run dev
```

### 2. 访问系统

打开浏览器访问：`http://localhost:3000/#/crawler-task-management`

### 3. 创建第一个任务

1. 点击 **"使用模板"**
2. 选择 **"美国510K每日爬取"**
3. 点击 **"保存"**
4. 完成！系统每天凌晨2点自动爬取

### 4. 立即执行测试

点击任务行的 **"▶️"** 按钮，立即执行任务并查看结果。

## 📖 文档

- 📘 [用户操作手册](./爬虫任务管理系统-用户操作手册.md) - 详细的功能说明
- 📗 [快速部署指南](./爬虫任务管理系统-快速部署指南.md) - 部署步骤和配置
- 📙 [新国家接入指南](./docs/CRAWLER_INTEGRATION_GUIDE.md) - 标准化接入流程
- 📕 [实施总结](./爬虫任务管理系统-实施总结.md) - 项目成果和技术亮点

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────┐
│           前端界面 (Vue 3)                   │
│  - 任务管理页面                              │
│  - 监控仪表盘                                │
│  - 可复用组件                                │
└────────────────┬────────────────────────────┘
                 │ REST API
┌────────────────▼────────────────────────────┐
│       Controller层 (Spring Boot)            │
│  - CrawlerTaskController (15个接口)         │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│              Service层                       │
│  ├─ CrawlerRegistryService (爬虫注册)        │
│  ├─ CrawlerTaskConfigService (配置管理)      │
│  ├─ CrawlerTaskExecutionService (任务执行)   │
│  └─ DynamicSchedulerService (动态调度)       │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│           Adapter层 (11个适配器)             │
│  US: 510K, Recall, Registration, Event,     │
│      Guidance, CustomsCase                  │
│  EU: Recall, Registration, Guidance,        │
│      CustomsCase                            │
│  KR: Recall                                 │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│           Crawler层 (11个爬虫)               │
│  - 实际的数据爬取逻辑                        │
│  - 数据转换和保存                            │
└─────────────────────────────────────────────┘
```

## 📊 支持的爬虫

| 国家 | 数据类型 | 爬虫数量 | 关键词支持 |
|------|---------|---------|----------|
| 美国 | 510K, Recall, Event, Registration, Guidance, Customs | 6 | 5个支持 |
| 欧盟 | Recall, Registration, Guidance, Customs | 4 | 3个支持 |
| 韩国 | Recall | 1 | ✅ |
| **总计** | - | **11** | **9/11** |

## 🔑 核心功能

### 任务管理
- ✅ 创建/编辑/删除任务
- ✅ 启用/禁用任务
- ✅ 批量创建
- ✅ 预设模板

### 任务执行
- ✅ 立即执行
- ✅ 定时执行
- ✅ 批量执行
- ✅ 停止运行中的任务
- ✅ 异步执行

### 监控统计
- ✅ 实时任务状态
- ✅ 执行历史日志
- ✅ 成功率统计
- ✅ 数据量统计
- ✅ 耗时分析

### 高级特性
- ✅ 关键词批量爬取
- ✅ 动态Cron调度
- ✅ 错误自动重试
- ✅ 执行优先级
- ✅ 超时控制

## 🛠️ API接口

```
# 任务管理
POST   /api/crawler-tasks                    # 创建任务
PUT    /api/crawler-tasks/{id}               # 更新任务
DELETE /api/crawler-tasks/{id}               # 删除任务
GET    /api/crawler-tasks                    # 查询列表

# 任务控制
POST   /api/crawler-tasks/{id}/execute       # 立即执行
POST   /api/crawler-tasks/{id}/stop          # 停止任务
POST   /api/crawler-tasks/{id}/enable        # 启用
POST   /api/crawler-tasks/{id}/disable       # 禁用

# 监控查询
GET    /api/crawler-tasks/running            # 运行中任务
GET    /api/crawler-tasks/next-executions    # 下次执行
GET    /api/crawler-tasks/statistics         # 统计信息
GET    /api/crawler-tasks/available-crawlers # 可用爬虫
```

完整API文档：`http://localhost:8080/doc.html`

## 🌟 使用示例

### 创建任务

```bash
curl -X POST http://localhost:8080/api/crawler-tasks \
  -H "Content-Type: application/json" \
  -d '{
    "crawlerName": "US_510K",
    "countryCode": "US",
    "keywords": "[\"Skin Analyzer\", \"3D Scanner\"]",
    "cronExpression": "0 0 2 * * ?",
    "enabled": true,
    "crawlParams": "{\"maxRecords\": -1, \"batchSize\": 100}"
  }'
```

### 立即执行

```bash
curl -X POST http://localhost:8080/api/crawler-tasks/1/execute?triggeredBy=admin
```

### 批量执行

```bash
curl -X POST http://localhost:8080/api/crawler-tasks/batch-execute \
  -H "Content-Type: application/json" \
  -d '{"taskIds": [1, 2, 3]}'
```

## 🔄 新增国家（5步）

1. **开发爬虫**：创建 `{国家}_{类型}.java`
2. **创建适配器**：创建 `{国家}{类型}Adapter.java`
3. **自动注册**：重启服务（自动扫描注册）
4. **配置任务**：通过界面或API创建任务
5. **测试验证**：立即执行验证

详细步骤：[新国家爬虫接入指南](./docs/CRAWLER_INTEGRATION_GUIDE.md)

## 📋 待办事项

- [ ] 单元测试覆盖
- [ ] 集成测试
- [ ] 性能压测
- [ ] 分布式锁（Redis）
- [ ] 告警通知
- [ ] 数据可视化图表

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

---

**当前版本**：v1.0.0  
**最后更新**：2025-10-11  
**维护团队**：开发组

