# TARIC 海关判例爬虫 API 文档

## 概述

TARIC（Tariff Integrated of the European Communities）爬虫用于爬取欧盟海关商品编码信息，并将其保存到CustomsCase实体中。

## 主要功能

### 1. 数据爬取
- 爬取指定TARIC编码下的所有商品编码信息
- 支持批次保存（每批20条记录）
- 自动重复检测，连续3个批次完全重复则停止爬取
- 默认keywords字段为空

### 2. 基于关键词列表的爬取（参考美国海关爬虫）
- **单一参数搜索**: 每个关键词作为TARIC编码进行搜索
- **关键词批次处理**: 每20个关键词为一批进行处理
- **重复检测**: 连续3个关键词批次完全重复则停止爬取
- **参数化控制**: 支持最大记录数、批次大小等参数控制
- **智能延迟控制**: 基础延迟2秒+随机延迟，避免429错误
- **重试机制**: 最多3次重试，指数退避延迟（2秒→4秒→8秒）

### 3. 关键词提取
- 从商品描述中自动提取关键词
- 支持单个记录和批量更新
- 使用停用词过滤，提取有意义的关键词

## API 接口

### 1. 爬取TARIC数据
```
POST /api/eu-crawler/taric/crawl
```

**参数：**
- `taricCode` (可选): TARIC编码，默认为"9018"（医疗设备）

**响应示例：**
```json
{
  "success": true,
  "message": "TARIC商品编码数据爬取完成",
  "taricCode": "9018",
  "savedCount": 25,
  "timestamp": 1698766879127
}
```

### 2. 提取单个记录关键词
```
POST /api/eu-crawler/taric/extract-keywords
```

**参数：**
- `customsCaseId`: 海关判例ID

**响应示例：**
```json
{
  "success": true,
  "message": "关键词提取完成",
  "customsCaseId": 123,
  "keywordCount": 5,
  "timestamp": 1698766879127
}
```

### 3. 批量提取所有关键词
```
POST /api/eu-crawler/taric/extract-all-keywords
```

**响应示例：**
```json
{
  "success": true,
  "message": "批量关键词提取完成",
  "updatedCount": 150,
  "timestamp": 1698766879127
}
```

### 4. 获取爬虫状态
```
GET /api/eu-crawler/taric/status
```

**响应示例：**
```json
{
  "success": true,
  "message": "TARIC爬虫状态正常",
  "status": {
    "available": true,
    "batchSize": 20,
    "maxConsecutiveDuplicateBatches": 3,
    "baseUrl": "https://ec.europa.eu/taxation_customs/dds2/taric/measures.jsp",
    "lastTest": 1698766879127
  },
  "timestamp": 1698766879127
}
```

### 5. 获取爬虫配置
```
GET /api/eu-crawler/taric/config
```

**响应示例：**
```json
{
  "success": true,
  "message": "TARIC爬虫配置信息",
  "config": {
    "batchSize": "20",
    "maxConsecutiveDuplicateBatches": "3",
    "maxRetryAttempts": "3",
    "baseDelayMs": "2000",
    "maxDelayMs": "10000",
    "defaultRiskLevel": "MEDIUM",
    "defaultKeywords": "",
    "dataSource": "EU TARIC Database",
    "jdCountry": "EU",
    "violationType": "TARIC措施",
    "baseUrl": "https://ec.europa.eu/taxation_customs/dds2/taric/measures.jsp"
  },
  "timestamp": 1698766879127
}
```

### 6. 测试TARIC连接
```
GET /api/eu-crawler/taric/test-connection
```

**响应示例：**
```json
{
  "success": true,
  "message": "TARIC连接测试完成",
  "connectionInfo": {
    "statusCode": 200,
    "responseTime": "1250ms",
    "accessible": true,
    "testUrl": "https://ec.europa.eu/taxation_customs/dds2/taric/measures.jsp?..."
  },
  "timestamp": 1698766879127
}
```

**连接失败响应示例：**
```json
{
  "success": false,
  "message": "TARIC连接测试失败: HTTP error fetching URL. Status=429",
  "connectionInfo": {
    "accessible": false,
    "error": "HTTP error fetching URL. Status=429",
    "errorType": "HttpStatusException"
  },
  "timestamp": 1698766879127
}
```

### 7. 基于关键词列表爬取TARIC数据
```
POST /api/eu-crawler/taric/crawl-with-keywords
```

**请求体：**
```json
["medical device", "9018", "surgical instrument", "diagnostic equipment"]
```

**参数：**
- `maxRecords` (可选): 最大记录数，-1表示所有数据，默认-1
- `batchSize` (可选): 批次大小，默认20

**响应示例：**
```json
{
  "success": true,
  "message": "基于关键词列表的TARIC数据爬取完成",
  "keywords": ["medical device", "9018", "surgical instrument", "diagnostic equipment"],
  "maxRecords": -1,
  "batchSize": 20,
  "result": "基于关键词列表的TARIC数据爬取完成，处理了 4 个关键词，总共保存: 85 条记录",
  "timestamp": 1698766879127
}
```

### 8. 基于智能关键词策略爬取TARIC数据
```
POST /api/eu-crawler/taric/crawl-with-smart-keywords
```

**请求体：**
```json
["9018", "medical", "surgical instrument", "diagnostic"]
```

**参数：**
- `maxRecords` (可选): 最大记录数，-1表示所有数据，默认-1
- `batchSize` (可选): 批次大小，默认20

**响应示例：**
```json
{
  "success": true,
  "message": "基于智能关键词策略的TARIC数据爬取完成",
  "keywords": ["9018", "medical", "surgical instrument", "diagnostic"],
  "maxRecords": -1,
  "batchSize": 20,
  "result": "基于智能关键词策略的TARIC数据爬取完成，处理了 4 个关键词，总共保存: 45 条记录",
  "timestamp": 1698766879127
}
```

## 数据字段映射

| TARIC网站数据 | CustomsCase字段 | 说明 |
|---------------|-----------------|------|
| 商品编码 | `hsCodeUsed` | 如9018.11.00 |
| 商品描述 | `rulingResult` | 商品详细描述 |
| 措施信息 | `violationType` | 固定为"TARIC措施" |
| 当前日期 | `caseDate` | 爬取日期 |
| 构建URL | `caseNumber` | TARIC-{编码}-{时间戳} |
| 固定值 | `dataSource` | "EU TARIC Database" |
| 固定值 | `jdCountry` | "EU" |
| 当前时间 | `crawlTime` | 爬取时间 |
| 固定值 | `dataStatus` | "ACTIVE" |
| 固定值 | `riskLevel` | RiskLevel.MEDIUM |
| 空值 | `keywords` | 默认为空字符串 |

## 使用示例

### 1. 爬取医疗设备相关数据
```bash
curl -X POST "http://localhost:8080/api/eu-crawler/taric/crawl?taricCode=9018"
```

### 2. 爬取其他商品类别
```bash
# 爬取电子设备
curl -X POST "http://localhost:8080/api/eu-crawler/taric/crawl?taricCode=8543"

# 爬取化学产品
curl -X POST "http://localhost:8080/api/eu-crawler/taric/crawl?taricCode=3004"
```

### 3. 提取关键词
```bash
# 为特定记录提取关键词
curl -X POST "http://localhost:8080/api/eu-crawler/taric/extract-keywords?customsCaseId=123"

# 批量提取所有记录的关键词
curl -X POST "http://localhost:8080/api/eu-crawler/taric/extract-all-keywords"
```

### 4. 基于关键词列表爬取
```bash
# 多策略关键词搜索
curl -X POST "http://localhost:8080/api/eu-crawler/taric/crawl-with-keywords" \
  -H "Content-Type: application/json" \
  -d '["medical device", "9018", "surgical instrument", "diagnostic equipment"]'

# 智能关键词策略搜索
curl -X POST "http://localhost:8080/api/eu-crawler/taric/crawl-with-smart-keywords" \
  -H "Content-Type: application/json" \
  -d '["9018", "medical", "surgical instrument", "diagnostic"]' \
  -G -d "maxRecords=100" -d "batchSize=10"
```

## 技术特性

### 1. 批次处理
- 每批处理20条记录
- 自动检测重复数据
- 连续3个批次完全重复则停止爬取

### 2. 重复检测
- 基于HS编码和商品描述进行重复检测
- 使用数据库查询避免重复保存
- 提取所有层级的商品编码（包括子分类）

### 3. 关键词列表爬取策略
- **单一参数搜索**: 每个关键词作为TARIC编码进行搜索
- **关键词批次处理**: 每20个关键词为一批进行处理
- **重复检测**: 连续3个关键词批次完全重复则停止爬取
- **智能延迟控制**: 基础延迟2秒+随机延迟（0-1秒），避免429错误
- **重试机制**: 最多3次重试，指数退避延迟（2秒→4秒→8秒）
- **参数化控制**: 支持最大记录数、批次大小等参数

### 4. 重试机制详解
- **HTTP 429 (Too Many Requests)**: 指数退避延迟，2秒→4秒→8秒
- **HTTP 5xx (服务器错误)**: 线性延迟，2秒→4秒→6秒
- **连接超时**: 线性延迟重试
- **连接异常**: 线性延迟重试
- **其他HTTP错误**: 不重试，直接抛出异常

### 5. 关键词提取
- 自动清理文本（去除标点符号）
- 过滤停用词
- 限制关键词数量（最多10个）
- 支持JSON格式存储

### 5. 错误处理
- 完整的异常处理机制
- 详细的日志记录
- 优雅的错误响应

## 注意事项

1. **网络超时**: 爬虫设置了15秒超时，确保网络稳定性
2. **数据完整性**: 只保存有效的商品编码和描述数据
3. **关键词提取**: 默认keywords为空，需要手动调用关键词提取API
4. **重复检测**: 基于HS编码和描述的前50个字符进行重复检测
5. **事务管理**: 使用Spring事务确保数据一致性

## 扩展功能

### 1. 支持更多TARIC编码
可以扩展支持其他商品类别的TARIC编码：
- 9018: 医疗设备
- 8543: 电子设备
- 3004: 化学产品
- 等等...

### 2. 关键词优化
- 可以添加领域特定的停用词
- 支持多语言关键词提取
- 可以集成更高级的NLP算法

### 3. 数据验证
- 可以添加数据质量检查
- 支持数据清洗和标准化
- 可以添加数据完整性验证

## 与美国海关爬虫的对比

| 特性 | 美国海关爬虫 | TARIC爬虫 |
|------|-------------|-----------|
| 搜索策略 | 多维度搜索（公司名、品牌名、产品描述） | 单一参数搜索（TARIC编码） |
| 关键词处理 | 每个关键词3种搜索方式 | 每个关键词1种搜索方式 |
| 批次处理 | 数据批次（20条记录） | 关键词批次（20个关键词） |
| 延迟控制 | 1秒延迟 | 1秒延迟 |
| 重复检测 | 支持 | 支持 |
| 参数控制 | 支持 | 支持 |

## 搜索策略详解

### 关键词列表搜索（crawlAndSaveWithKeywords）
- 每个关键词作为TARIC编码进行搜索
- 每20个关键词为一批进行处理
- 连续3个关键词批次完全重复则停止爬取

### 智能关键词搜索（crawlAndSaveWithSmartKeywords）
- 与关键词列表搜索相同的策略
- 每个关键词作为TARIC编码进行搜索
- 提供更详细的日志输出
