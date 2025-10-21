# 医疗器械认证监控系统

<div align="center">

**Medical Device Certification Monitoring System**

一个全球医疗器械认证数据的自动化监控、分析和风险预警平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.0-4FC08D.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

[功能特性](#-功能特性) •
[快速开始](#-快速开始) •
[技术架构](#-技术架构) •
[部署指南](#-部署指南) •
[文档](#-详细文档)

</div>

---

## 📋 项目简介

医疗器械认证监控系统是一个智能化的数据监控平台，专注于全球医疗器械认证标准、产品注册动态和风险信息的自动化采集与分析。系统集成了多国家/地区的数据源，通过 AI 技术实现智能判断和风险评估，帮助企业及时把握认证法规变化。

### 核心价值

- 🌍 **全球覆盖**：支持美国、欧盟、日本、韩国、台湾、中国等多个国家和地区
- 🤖 **AI 驱动**：集成 GPT-4o 实现智能数据分析和风险评估
- 📊 **实时监控**：定时任务自动化采集，实时更新认证动态
- 🔍 **精准筛选**：关键词匹配和黑名单机制，过滤无关信息
- 📈 **可视化分析**：数据统计图表，风险趋势一目了然

---

## ✨ 功能特性

### 1. 统一爬虫调度管理平台

- **多国家数据源**：支持 6 个国家/地区，30+ 爬虫
- **灵活调度**：Cron 表达式定时执行，支持手动触发
- **参数预设**：保存常用爬取配置，一键复用
- **实时监控**：任务执行状态、进度、结果实时可见
- **日志追踪**：完整的执行历史和错误日志

### 2. 医疗设备数据管理

**数据类型**：
- 📄 **510K 申请**：FDA 上市前通知数据
- 📝 **设备注册**：医疗器械注册信息
- ⚠️ **召回记录**：产品召回公告和原因
- 📊 **事件报告**：不良事件和故障报告
- 📚 **指导文档**：监管指南和技术标准
- 🚢 **海关案例**：进出口归类判例

**功能亮点**：
- 多维度筛选（国家、风险等级、日期范围）
- 批量数据导出（Excel、CSV）
- AI 智能判断相关性
- 数据详情查看和编辑

### 3. AI 智能审核

**认证新闻审核**：
- 判断新闻是否与无线电子设备认证相关
- 自动提取关键认证标准（FCC、RoHS、SRRC、CE/RED 等）
- 置信度评分和理由说明

**医疗设备审核**：
- 识别皮肤检测/分析类医疗器械
- 判断设备用途和相关性
- 支持多语言数据（自动翻译）

**智能翻译**：
- 集成火山引擎翻译 API
- 自动翻译韩文、日文等非英文数据
- 翻译失败自动降级，不影响主流程

### 4. 认证新闻监控

**支持的认证机构**：
- 🏢 **SGS**：瑞士通用公证行
- 🔬 **UL Solutions**：安全科学公司
- 🇨🇳 **北测检测**：国内权威检测机构

**监控内容**：
- 认证标准更新公告
- 产品认证动态
- 法规变更通知
- 技术要求调整

### 5. 高风险数据管理

- 自动标记高风险数据
- 关键词黑名单管理
- 批量风险等级调整
- 邮件/Slack 告警（可选）

---

## 🚀 快速开始

### 前置要求

- **JDK 17+**
- **Node.js 16+**
- **MySQL 8.0+**
- **Redis 6.0+**（可选）
- **Maven 3.6+**

### 本地开发

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/certification-monitor.git
cd certification-monitor
```

#### 2. 配置数据库

```sql
CREATE DATABASE common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cert_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON common_db.* TO 'cert_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. 配置后端

编辑 `spring-boot-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/common_db
    username: cert_user
    password: your_password

# OpenAI API 配置（用于 AI 判断）
openai:
  api:
    key: your_openai_api_key
    model: gpt-4o
```

#### 4. 启动后端

```bash
cd spring-boot-backend
mvn clean install
mvn spring-boot:run
```

#### 5. 启动前端

```bash
cd vue-frontend
npm install
npm run dev
```

#### 6. 访问应用

- **前端应用**：http://localhost:3100
- **后端 API**：http://localhost:8080/api
- **API 文档**：http://localhost:8080/api/doc.html
- **Druid 监控**：http://localhost:8080/druid

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Vue 3 + TypeScript)              │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐│
│  │ 数据展示  │  │ AI 审核   │  │ 爬虫管理  │  │ 任务调度  ││
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘│
└────────────────────────┬────────────────────────────────────┘
                         │ REST API (Axios)
┌────────────────────────┴────────────────────────────────────┐
│                  后端层 (Spring Boot 3.2)                    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Controller → Service → Repository            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  爬虫引擎    │  │  AI 服务     │  │  定时任务    │     │
│  │  - Jsoup     │  │  - GPT-4o    │  │  - Quartz    │     │
│  │  - Selenium  │  │  - 火山引擎  │  │  - Cron      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────┬────────────────┬────────────────┬────────────┘
              │                │                │
    ┌─────────▼──────┐  ┌─────▼──────┐  ┌─────▼──────────┐
    │  MySQL 8.0     │  │  Redis     │  │  外部 API      │
    │  - 设备数据    │  │  - 缓存    │  │  - OpenAI      │
    │  - 新闻数据    │  │  - 队列    │  │  - 火山引擎    │
    │  - 任务配置    │  └────────────┘  │  - FDA/PMDA    │
    └────────────────┘                  └────────────────┘
```

### 技术栈

**后端技术栈**：
- **框架**：Spring Boot 3.2.0
- **语言**：Java 17 (LTS)
- **ORM**：Spring Data JPA + Hibernate
- **数据库**：MySQL 8.0+
- **缓存**：Redis 6.0+
- **连接池**：Druid 1.2.20
- **调度**：Spring Quartz
- **HTTP 客户端**：Jsoup 1.17.2、Apache HttpClient 5
- **爬虫框架**：Selenium 4.15.0
- **AI 服务**：
  - OpenAI GPT-4o (数据分析判断)
  - 火山引擎翻译 API (多语言翻译)
- **OCR**：Tesseract 5.7.0 (验证码识别)
- **工具库**：Hutool 5.8.23、Lombok 1.18.30
- **日志**：Logback
- **API 文档**：Knife4j 4.3.0

**前端技术栈**：
- **框架**：Vue 3.4.0 (Composition API)
- **语言**：TypeScript 5.3.0
- **构建工具**：Vite 6.0.1
- **UI 框架**：Ant Design Vue 4.2.6
- **状态管理**：Pinia 2.1.7
- **路由**：Vue Router 4.2.5
- **HTTP 客户端**：Axios 1.11.0
- **图表库**：ECharts 5.4.3
- **日期处理**：Day.js 1.11.10

---

## 🌍 支持的国家和地区

### 美国 (US) 🇺🇸
- **数据源**：FDA (Food and Drug Administration)
- **爬虫数量**：7 个
- **数据类型**：510K 申请、注册记录、召回、事件报告、指导文档、海关案例

### 欧盟 (EU) 🇪🇺
- **数据源**：EUDAMED、EMA
- **爬虫数量**：5 个
- **数据类型**：注册记录、召回、指导文档、海关案例、制造商信息

### 日本 (JP) 🇯🇵
- **数据源**：PMDA (医药品医疗器械综合机构)
- **爬虫数量**：3 个
- **数据类型**：召回记录、注册信息、指导文档

### 韩国 (KR) 🇰🇷
- **数据源**：MFDS (食品药品安全处)
- **爬虫数量**：5 个
- **数据类型**：注册记录、召回、海关案例、事件报告、指导文档
- **特色**：集成火山引擎翻译，自动韩文翻译

### 台湾 (TW) 🇹🇼
- **数据源**：TFDA (卫生福利部食品药物管理署)
- **爬虫数量**：4 个
- **数据类型**：注册记录、海关案例、事件报告、指导文档
- **特色**：集成 2Captcha 验证码识别

### 中国 (CN) 🇨🇳
- **数据源**：NMPA (国家药品监督管理局)
- **爬虫数量**：2 个
- **数据类型**：注册记录、海关案例

---

## 📦 部署指南

### Docker 部署（推荐）

#### 使用 Docker Compose

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/certification-monitor.git
cd certification-monitor

# 2. 配置环境变量
cp .env.prod.example .env.prod
vim .env.prod  # 编辑配置

# 3. 启动所有服务
docker-compose -f docker-compose.prod.yml up -d

# 4. 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

#### 使用 Podman Desktop

详细步骤请参考：[Podman Desktop 部署指南](./PODMAN_DEPLOYMENT_GUIDE.md)

```bash
# 构建并启动
podman-compose -f docker-compose.prod.yml up -d --build

# 查看状态
podman ps

# 查看日志
podman-compose -f docker-compose.prod.yml logs -f
```

### 访问服务

部署成功后，访问以下地址：

- **前端应用**：http://your-server-ip
- **后端 API**：http://your-server-ip:8080/api
- **API 文档**：http://your-server-ip:8080/api/doc.html
- **数据库管理 (phpMyAdmin)**：http://your-server-ip:8081
- **Druid 监控**：http://your-server-ip:8080/druid

---

## 📚 详细文档

### 部署与运维
- 📘 [Podman Desktop 部署指南](./PODMAN_DEPLOYMENT_GUIDE.md) - 使用 Podman 进行容器化部署
- 📙 [系统维护文档](./SYSTEM_MAINTENANCE_GUIDE.md) - 日常维护、故障排查、性能优化

### 开发文档
- 📗 [爬虫开发指南](./爬虫维护与拓展指南.md) - 新增爬虫、维护现有爬虫
- 📕 [AI 模块文档](./AI模块维护文档.md) - AI 服务配置和使用
- 📓 [火山引擎翻译配置](./火山引擎翻译配置说明.md) - 翻译服务配置

### 用户手册
- 📖 [用户使用指南](./认证预警系统使用与维护文档.docx) - 完整操作手册
- 📊 [爬虫调度平台](./爬虫调度管理平台-README.md) - 爬虫管理和调度

---

## 🎯 核心模块说明

### 1. 爬虫调度管理平台
- **访问路径**：`/unified-crawler-management`
- **功能**：
  - 查看所有已注册爬虫
  - 配置爬取参数（关键词、日期范围、数量限制）
  - 手动执行爬虫任务
  - 保存参数预设
  - 创建定时任务（Cron 表达式）
  - 查看任务执行历史和日志

### 2. 设备数据管理
- **访问路径**：`/device-data`
- **功能**：
  - 多维度数据筛选
  - 批量 AI 智能判断
  - 风险等级标记
  - 数据导出（Excel、CSV）
  - 详情查看和编辑

### 3. 高风险数据管理
- **访问路径**：`/high-risk-data-management`
- **功能**：
  - 高风险数据筛选
  - 关键词黑名单管理
  - 批量操作（标记、删除）
  - 数据审核

### 4. 认证新闻数据
- **访问路径**：`/crawler-data-management`
- **功能**：
  - 认证新闻查看
  - AI 相关性判断
  - 风险等级标记
  - 黑名单管理

---

## 🔧 配置说明

### 环境变量配置

创建 `.env.prod` 文件并配置以下关键参数：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_DATABASE=common_db
MYSQL_USER=cert_user
MYSQL_PASSWORD=your_password

# Redis 配置
REDIS_PASSWORD=your_redis_password

# OpenAI API 配置
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o
OPENAI_BASE_URL=https://api.openai.com/v1

# 火山引擎翻译配置
VOLCENGINE_ACCESS_KEY=your_access_key
VOLCENGINE_SECRET_KEY=your_secret_key

# CORS 配置
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# Druid 监控配置
DRUID_USERNAME=admin
DRUID_PASSWORD=your_druid_password
```

详细配置说明请参考 `.env.prod.example` 文件。

---

## 📊 数据统计

截至目前，系统已集成：

- **30+** 个数据爬虫
- **6** 个国家/地区数据源
- **6** 种数据类型
- **3** 个认证新闻源
- **支持多语言翻译**（英语、韩语、日语、中文）

---

## 🛡️ 安全建议

1. **修改所有默认密码**
2. **配置强密码**（至少 16 位，包含大小写字母、数字、特殊字符）
3. **启用 HTTPS**（生产环境）
4. **配置防火墙规则**
5. **定期更新依赖包**
6. **生产环境关闭 Swagger API 文档**
7. **配置 Druid 监控白名单**
8. **不要将 `.env.prod` 提交到版本控制**

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📝 更新日志

### v2.0.0 (2025-01-20)
- ✨ 新增台湾 TFDA 数据源支持
- ✨ 集成 2Captcha 验证码识别服务
- ✨ 新增日本 PMDA 数据源
- 🔧 优化爬虫调度平台
- 🔧 完善 AI 智能判断功能
- 📚 更新文档和部署指南
- 🐛 修复已知问题

### v1.0.0 (2024-09-30)
- 🎉 项目初始发布
- ✨ 美国 FDA 数据源
- ✨ 韩国 MFDS 数据源
- ✨ 欧盟 EUDAMED 数据源
- ✨ 认证新闻爬虫
- ✨ AI 智能审核功能

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 👥 联系方式

- **GitHub**：[项目地址](https://github.com/yourusername/certification-monitor)
- **Issues**：[问题反馈](https://github.com/yourusername/certification-monitor/issues)
- **Email**：your-email@example.com

---

<div align="center">

**Made with ❤️ by Development Team**

**版本**: v2.0.0
**最后更新**: 2025-01-20

</div>
