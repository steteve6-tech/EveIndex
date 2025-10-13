# 🕐 爬虫调度管理平台

## 📋 功能概述

统一的爬虫注册、参数预设、定时调度、执行监控管理系统。

## 🚀 快速访问

**页面地址**: http://localhost:3100/#/unified-crawler-management  
**菜单位置**: 医疗认证风险 → 爬虫调度平台

## ✨ 核心功能

- ✅ 11个爬虫自动注册（美国6个、欧盟4个、韩国1个）
- ✅ 参数预设管理（创建/编辑/复制/删除）
- ✅ 动态Cron调度（运行时管理定时任务）
- ✅ Schema驱动动态表单（前端自动生成）
- ✅ 完整执行监控（运行中任务、执行历史、统计分析）

## 🎯 使用流程

1. **创建预设** - 选择爬虫，配置参数，设置Cron表达式
2. **启用任务** - 系统自动创建定时任务
3. **自动执行** - 到达Cron时间自动触发
4. **监控查看** - 查看执行历史和统计

## 🔑 技术亮点

- **扩展性**: 新增爬虫只需3个文件（爬虫类、适配器、Schema）
- **动态性**: 参数完全动态，支持任意字段配置
- **智能化**: Schema驱动，前端表单自动生成

## 📊 系统架构

```
Controller → ParamPresetService → DynamicTaskSchedulerService
                ↓                         ↓
         TaskExecutionService → CrawlerAdapter → Crawler
                ↓
         UnifiedTaskLog
```

## 📚 API文档

访问: http://localhost:8080/swagger-ui/index.html  
搜索: UnifiedCrawlerController

**版本**: V2.0  
**更新**: 2025-10-13

