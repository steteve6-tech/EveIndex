# 医疗认证监控系统

## 📋 项目简介

医疗设备认证数据的自动化爬取、监控和风险分析系统。

## 🚀 快速启动

### 后端
```bash
cd spring-boot-backend
mvn spring-boot:run
```

### 前端
```bash
cd vue-frontend
npm install
npm run dev
```

### 访问
- 前端地址: http://localhost:3100
- API文档: http://localhost:8080/swagger-ui/index.html

## 🎯 核心模块

### 1. 爬虫调度管理平台
- **页面**: `/unified-crawler-management`
- **功能**: 11个爬虫的统一调度管理
- **特点**: Schema驱动、动态Cron、完整监控

### 2. 设备数据管理
- **页面**: `/device-data`
- **功能**: 设备数据查询和AI智能判断

### 3. 高风险数据管理
- **页面**: `/high-risk-data-management`
- **功能**: 高风险数据筛选和管理

### 4. 认证新闻数据
- **页面**: `/crawler-data-management`
- **功能**: 认证新闻爬取和管理

## 📚 详细文档

- 📘 [爬虫调度平台](./爬虫调度管理平台-README.md)
- 📗 [医疗设备爬虫](./医疗设备模块爬虫详细文档.md)
- 📙 [爬虫API文档](./CRAWLER_API_DOCUMENTATION.md)
- 📕 [AI智能判断](./README-AI智能审核.md)
- 📓 [开发指南](./DEV_GUIDE.md)

## 🛠️ 技术栈

- **后端**: Spring Boot 3.2.0, JPA/Hibernate, MySQL
- **前端**: Vue 3, TypeScript, Ant Design Vue
- **爬虫**: Selenium, HttpClient5
- **AI**: OpenAI GPT

## 📊 支持的数据类型

- 510K设备审批
- 设备召回记录
- 不良事件报告
- 设备注册信息
- 监管指导文档
- 海关判例数据

## 🌍 支持的国家/地区

- 🇺🇸 美国 (FDA)
- 🇪🇺 欧盟 (EUDAMED)
- 🇰🇷 韩国 (MFDS)
- 🇨🇳 中国 (NMPA)

---

**版本**: V2.0  
**更新**: 2025-10-13

