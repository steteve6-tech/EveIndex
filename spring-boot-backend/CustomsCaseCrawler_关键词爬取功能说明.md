# CustomsCaseCrawler 关键词爬取功能实现说明

## 功能概述

已成功为 `CustomsCaseCrawler.java` 添加了基于关键词列表的爬取功能，类似于 `US_registration.java` 的实现方式。

## 实现的功能

### 1. 新增方法

#### `crawlWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize)`
- **功能**: 基于关键词列表爬取海关案例数据（简化版本）
- **参数**: 
  - `inputKeywords`: 关键词列表
  - `maxRecords`: 最大记录数
  - `batchSize`: 批次大小

#### `crawlWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo)`
- **功能**: 基于关键词列表爬取海关案例数据（支持时间范围）
- **参数**: 
  - `inputKeywords`: 关键词列表
  - `maxRecords`: 最大记录数
  - `batchSize`: 批次大小
  - `dateFrom`: 开始日期
  - `dateTo`: 结束日期

### 2. 搜索策略

每个关键词将依次进行以下两种搜索：
1. **HS编码搜索**: 使用关键词作为HS编码进行搜索
2. **普通关键词搜索**: 使用关键词作为普通搜索词进行搜索

### 3. 错误处理

- 自动跳过空关键词
- 对每个关键词的搜索进行异常处理
- 添加请求间隔避免过于频繁的API调用

## 服务层更新

### USCrawlerService.java

在 `testCustomsCase` 方法中添加了关键词支持：

```java
// 检查是否有关键词参数
Object inputKeywordsObj = params.get("inputKeywords");
List<String> inputKeywords = null;

if (inputKeywordsObj instanceof List) {
    @SuppressWarnings("unchecked")
    List<String> tempList = (List<String>) inputKeywordsObj;
    inputKeywords = tempList;
} else if (inputKeywordsObj instanceof String) {
    String keywordsStr = (String) inputKeywordsObj;
    if (!keywordsStr.trim().isEmpty()) {
        inputKeywords = parseKeywordsFromString(keywordsStr);
    }
}

// 如果有关键词列表，使用关键词爬取方法
if (inputKeywords != null && !inputKeywords.isEmpty()) {
    String crawlResult = customsCaseCrawler.crawlWithKeywords(inputKeywords, maxRecords, batchSize);
    // 返回关键词爬取结果
} else {
    // 使用原有的HS编码爬取方法
}
```

## 控制器层更新

### USCrawlerController.java

在 `searchCustomsCase` 方法中添加了关键词参数：

```java
@PostMapping("/search/customs-case")
public ResponseEntity<Map<String, Object>> searchCustomsCase(
        @Parameter(description = "HS编码") @RequestParam(required = false) String hsCode,
        @Parameter(description = "最大记录数") @RequestParam(required = false, defaultValue = "10") Integer maxRecords,
        @Parameter(description = "批次大小") @RequestParam(required = false, defaultValue = "10") Integer batchSize,
        @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String startDate,
        @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
    // 实现逻辑
}
```

## API接口

### 测试接口
- **URL**: `POST /api/us-crawler/test/customs-case`
- **功能**: 测试CustomsCaseCrawler爬虫

### 搜索接口
- **URL**: `POST /api/us-crawler/search/customs-case`
- **参数**:
  - `hsCode`: HS编码（可选）
  - `maxRecords`: 最大记录数（默认10）
  - `batchSize`: 批次大小（默认10）
  - `startDate`: 开始日期（可选，格式：MM/DD/YYYY）
  - `inputKeywords`: 输入关键词列表（可选，逗号分隔）

### 配置模板接口
- **URL**: `GET /api/us-crawler/config/customs-case`
- **功能**: 获取CustomsCaseCrawler的参数配置模板

## 使用示例

### 1. 使用关键词列表爬取

```bash
curl -X POST "http://localhost:8080/api/us-crawler/search/customs-case" \
  -d "inputKeywords=9018,9021,9022&maxRecords=20&batchSize=10"
```

### 2. 使用HS编码爬取（原有功能）

```bash
curl -X POST "http://localhost:8080/api/us-crawler/search/customs-case" \
  -d "hsCode=9018&maxRecords=10&batchSize=10"
```

### 3. 测试关键词功能

```bash
# 使用测试脚本
./test_customs_case_keywords.bat

# 或直接使用Maven
mvn exec:java -Dexec.mainClass="com.certification.crawler.generalArchitecture.us.CustomsCaseCrawler" -Dexec.args="keywords 9018,9021,9022"
```

## 返回结果格式

### 关键词爬取模式
```json
{
  "success": true,
  "message": "CustomsCaseCrawler关键词爬取成功",
  "crawlResult": "关键词列表爬取完成，处理关键词数: 3, 总获取记录数: 15",
  "keywordsProcessed": 3,
  "keywords": ["9018", "9021", "9022"]
}
```

### HS编码爬取模式
```json
{
  "success": true,
  "message": "CustomsCaseCrawler爬虫测试成功",
  "totalSaved": 5,
  "totalSkipped": 0,
  "totalPages": 1,
  "data": [...]
}
```

## 注意事项

1. **请求频率**: 每个关键词搜索之间会添加1秒延迟，避免API请求过于频繁
2. **错误处理**: 单个关键词搜索失败不会影响其他关键词的搜索
3. **兼容性**: 新功能完全兼容原有的HS编码搜索功能
4. **数据库**: 爬取的数据会自动保存到 `t_customs_case` 表中
5. **去重**: 系统会自动检查并跳过重复记录

## 测试文件

- `test_customs_case_keywords.bat`: 关键词爬取功能测试脚本
- `CustomsCaseCrawler.java`: 主爬虫类，包含关键词爬取方法
- `USCrawlerService.java`: 服务层，处理关键词参数解析
- `USCrawlerController.java`: 控制器层，提供REST API接口

