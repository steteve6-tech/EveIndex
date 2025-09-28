# EU新闻爬虫数据库集成说明

## 修改概述

已成功修改 `Eu_UpdataNews.java` 文件，添加了数据库保存功能，支持批次保存和重复检测。

## 主要功能

### 1. 批次保存
- **批次大小**: 每20个数据保存一次
- **配置**: `BATCH_SIZE = 20`
- **优势**: 提高性能，减少数据库连接次数

### 2. 重复检测
- **检测逻辑**: 基于标题和发布日期检查重复
- **停止条件**: 连续3个批次完全重复则停止爬取
- **配置**: `MAX_CONSECUTIVE_DUPLICATE_BATCHES = 3`

### 3. 字段映射
按照您的要求，`riskLevel` 默认为 `MEDIUM`，`keywords` 默认为空字符串。

## 新增方法

### `crawlAndSaveToDatabase(int maxPages)`
- **功能**: 爬取EU新闻并直接保存到数据库
- **参数**: `maxPages` - 最大爬取页数
- **返回**: 实际保存到数据库的记录数量
- **特性**: 
  - 支持批次保存（每20条）
  - 自动重复检测
  - 连续3个批次重复则停止

## 字段映射关系

| 爬取字段 | GuidanceDocument字段 | 说明 |
|---------|---------------------|------|
| `title` | `title` | 新闻标题 |
| `publish_date` | `publicationDate` | 发布日期 |
| `detail_url` | `documentUrl` | 详情链接 |
| `news_type` | `newsType` | 新闻类型 |
| `description` | `description` | 文章描述 |
| `read_time` | `readTime` | 阅读时间 |
| `image_url` | `imageUrl` | 图片URL |
| `image_alt` | `imageAlt` | 图片alt文本 |
| `article_index` | `articleIndex` | 文章序号 |
| `crawl_time` | `crawlTime` | 爬取时间 |

### 固定值设置
- `documentType`: "NEWS"
- `dataSource`: "EU"
- `jdCountry`: "EU"
- `riskLevel`: MEDIUM (默认)
- `keywords`: "" (默认空)

## 使用示例

### 1. 在Service中使用
```java
@Service
public class EuNewsService {
    
    @Autowired
    private Eu_UpdataNews euUpdataNews;
    
    public int crawlAndSave(int maxPages) {
        return euUpdataNews.crawlAndSaveToDatabase(maxPages);
    }
}
```

### 2. 在Controller中调用
```java
@RestController
public class EuNewsController {
    
    @Autowired
    private Eu_UpdataNews euUpdataNews;
    
    @PostMapping("/api/eu-news/crawl")
    public ResponseEntity<String> crawlEuNews(@RequestParam int maxPages) {
        int savedCount = euUpdataNews.crawlAndSaveToDatabase(maxPages);
        return ResponseEntity.ok("成功保存 " + savedCount + " 条记录");
    }
}
```

## 技术特性

### 1. 事务支持
- 使用 `@Transactional` 注解确保数据一致性
- 批次保存失败时自动回滚

### 2. 错误处理
- 完善的异常处理机制
- 详细的日志输出
- 优雅的错误恢复

### 3. 性能优化
- 批量保存减少数据库操作
- 智能重复检测避免无效操作
- 自动停止机制节省资源

## 配置参数

```java
// 批次大小
private static final int BATCH_SIZE = 20;

// 最大连续重复批次数
private static final int MAX_CONSECUTIVE_DUPLICATE_BATCHES = 3;
```

## 日志输出示例

```
🚀 开始爬取EU医疗设备新闻并保存到数据库...
📊 批次大小: 20，最大连续重复批次: 3
📄 正在爬取第1页: https://health.ec.europa.eu/...
⏱️ 第1页页面加载完成，耗时: 1234 毫秒
📝 第1页解析完成，获取到 15 条新闻
💾 批次保存完成: 15 条新记录
✅ 批次保存成功，保存了 15 条新记录
🔄 批次完全重复，连续重复批次数: 1
🛑 连续 3 个批次完全重复，停止爬取
🎉 爬取完成！总共保存了 35 条新记录到数据库
```

## 注意事项

1. **依赖注入**: 确保 `GuidanceDocumentRepository` 已正确配置
2. **数据库连接**: 确保数据库连接正常
3. **网络延迟**: 爬取过程中有1秒延迟，避免请求过快
4. **内存使用**: 批次处理有助于控制内存使用

## 兼容性

- 保留了原有的CSV保存功能
- 新增的数据库保存功能不影响现有代码
- 支持Spring Boot自动配置
