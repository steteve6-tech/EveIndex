# 爬虫管理API文档

## 概述
重写后的 `NewsUnicrawlController` 提供了统一的REST API接口来管理三个爬虫：
- **北测爬虫 (BEICE)** - 北测认证新闻爬虫
- **SGS爬虫 (SGS)** - SGS认证新闻爬虫  
- **UL爬虫 (UL)** - UL认证新闻爬虫

## 基础信息
- **基础路径**: `/api/crawlers`
- **支持跨域**: 是
- **API版本**: 2.0.0

## API接口列表

### 1. 获取爬虫列表
```http
GET /api/crawlers/list
```

**响应示例**:
```json
{
  "success": true,
  "crawlers": [
    {
      "name": "BEICE",
      "displayName": "北测爬虫",
      "description": "北测认证新闻爬虫",
      "status": "可用"
    },
    {
      "name": "SGS", 
      "displayName": "SGS爬虫",
      "description": "SGS认证新闻爬虫",
      "status": "可用"
    },
    {
      "name": "UL",
      "displayName": "UL爬虫", 
      "description": "UL认证新闻爬虫",
      "status": "可用"
    }
  ],
  "count": 3,
  "message": "获取爬虫列表成功"
}
```

### 2. 执行北测爬虫
```http
POST /api/crawlers/beice/execute
```

**参数**:
- `keyword` (可选): 搜索关键词
- `count` (默认50): 爬取数量

**示例**:
```http
POST /api/crawlers/beice/execute?keyword=医疗器械&count=100
```

### 3. 执行SGS爬虫
```http
POST /api/crawlers/sgs/execute
```

**参数**:
- `keyword` (可选): 搜索关键词
- `count` (默认50): 爬取数量

**示例**:
```http
POST /api/crawlers/sgs/execute?keyword=质量管理&count=100
```

### 4. 执行SGS爬虫（带过滤条件）
```http
POST /api/crawlers/sgs/execute-with-filters
```

**参数**:
- `keyword` (可选): 搜索关键词
- `count` (默认50): 爬取数量
- `newsType` (可选): 新闻类型值
- `dateRange` (可选): 日期范围值
- `topics` (可选): 主题值列表（逗号分隔）

**示例**:
```http
POST /api/crawlers/sgs/execute-with-filters?keyword=认证&count=100&newsType=新闻&dateRange=最近一月&topics=医疗器械,质量管理
```

### 5. 执行UL爬虫
```http
POST /api/crawlers/ul/execute
```

**参数**:
- `keyword` (可选): 搜索关键词
- `count` (默认50): 爬取数量

**示例**:
```http
POST /api/crawlers/ul/execute?keyword=标准&count=100
```

### 6. 并发执行所有爬虫
```http
POST /api/crawlers/execute-all
```

**参数**:
- `keyword` (可选): 搜索关键词
- `countPerCrawler` (默认50): 每个爬虫爬取的数量

**示例**:
```http
POST /api/crawlers/execute-all?keyword=认证&countPerCrawler=50
```

**响应示例**:
```json
{
  "success": true,
  "message": "所有爬虫执行完成",
  "keyword": "认证",
  "countPerCrawler": 50,
  "results": [
    {
      "success": true,
      "crawler": "BEICE",
      "crawlerDisplayName": "北测爬虫",
      "savedCount": 45,
      "message": "北测爬虫执行成功"
    },
    {
      "success": true,
      "crawler": "SGS",
      "crawlerDisplayName": "SGS爬虫", 
      "savedCount": 52,
      "message": "SGS爬虫执行成功"
    },
    {
      "success": true,
      "crawler": "UL",
      "crawlerDisplayName": "UL爬虫",
      "savedCount": 38,
      "message": "UL爬虫执行成功"
    }
  ],
  "timestamp": 1698123456789
}
```

### 7. 获取爬虫状态
```http
GET /api/crawlers/status
```

**响应示例**:
```json
{
  "success": true,
  "timestamp": 1698123456789,
  "crawlers": {
    "BEICE": {
      "name": "BEICE",
      "displayName": "北测爬虫",
      "status": {
        "status": "可用",
        "message": "北测爬虫运行正常"
      }
    },
    "SGS": {
      "name": "SGS",
      "displayName": "SGS爬虫", 
      "status": {
        "lastCrawlTime": "2025-09-24T10:30:00",
        "totalCrawled": 1250,
        "status": "可用"
      }
    },
    "UL": {
      "name": "UL",
      "displayName": "UL爬虫",
      "status": {
        "status": "可用",
        "message": "UL爬虫运行正常"
      }
    }
  }
}
```

### 8. 获取SGS过滤选项
```http
GET /api/crawlers/sgs/filter-options
```

**响应示例**:
```json
{
  "success": true,
  "crawlerName": "SGS",
  "filterOptions": {
    "newsTypes": ["所有", "新闻", "公告", "通知"],
    "dateRanges": ["所有", "最近一周", "最近一月", "最近三月", "最近一年"],
    "topics": ["医疗器械", "质量管理", "认证标准", "法规更新", "市场准入"]
  }
}
```

### 9. 获取API配置
```http
GET /api/crawlers/config
```

**响应示例**:
```json
{
  "apiName": "爬虫管理API",
  "version": "2.0.0",
  "description": "统一管理北测、SGS、UL三个爬虫的REST API",
  "supportedCrawlers": ["BEICE", "SGS", "UL"],
  "endpoints": {
    "getCrawlers": "GET /api/crawlers/list - 获取爬虫列表",
    "executeBeice": "POST /api/crawlers/beice/execute - 执行北测爬虫",
    "executeSgs": "POST /api/crawlers/sgs/execute - 执行SGS爬虫",
    "executeSgsWithFilters": "POST /api/crawlers/sgs/execute-with-filters - 执行SGS爬虫（过滤条件）",
    "executeUL": "POST /api/crawlers/ul/execute - 执行UL爬虫",
    "executeAll": "POST /api/crawlers/execute-all - 执行所有爬虫",
    "getStatus": "GET /api/crawlers/status - 获取爬虫状态",
    "getSgsFilterOptions": "GET /api/crawlers/sgs/filter-options - 获取SGS过滤选项",
    "getConfig": "GET /api/crawlers/config - 获取API配置"
  },
  "defaultCount": 50,
  "maxCount": 1000,
  "features": [
    "支持关键词搜索",
    "支持过滤条件（SGS爬虫）",
    "支持并发执行",
    "支持状态查询"
  ]
}
```

## 主要改进

### 1. 去除了测试接口
- 移除了原来的测试方法
- 所有接口都是生产就绪的

### 2. 直接调用爬虫方法
- 直接注入三个爬虫类：`BeiceCrawler`、`SgsCrawler`、`ULCrawler`
- 调用各自的执行方法，而不是通过中间服务

### 3. 统一的API设计
- 所有爬虫都有统一的执行接口
- 支持关键词搜索
- 支持并发执行

### 4. 更好的错误处理
- 每个接口都有完整的异常处理
- 返回详细的错误信息

### 5. 完整的Swagger文档
- 所有接口都有详细的API文档
- 包含参数说明和示例

## 使用建议

1. **单个爬虫执行**: 使用具体的爬虫接口，如 `/api/crawlers/beice/execute`
2. **批量执行**: 使用 `/api/crawlers/execute-all` 接口并发执行所有爬虫
3. **状态监控**: 定期调用 `/api/crawlers/status` 检查爬虫状态
4. **SGS高级功能**: 使用 `/api/crawlers/sgs/execute-with-filters` 进行精确过滤

## 注意事项

1. 所有爬虫执行都是异步的，可能需要一些时间完成
2. 建议设置合理的 `count` 参数，避免一次爬取过多数据
3. 并发执行所有爬虫时，请注意服务器资源使用情况
4. 关键词搜索支持中文和英文


