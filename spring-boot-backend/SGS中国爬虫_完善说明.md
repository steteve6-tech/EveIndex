# SGS中国爬虫完善说明

## 概述
已成功完善 `Sgs_cn.java` 文件，实现了SGS中国网站的新闻爬虫功能。

## 主要功能

### 1. 基础爬虫功能
- **目标网站**: https://www.sgsgroup.com.cn/zh-cn/news
- **爬虫类型**: HTML页面解析爬虫
- **数据源**: SGS中国新闻页面

### 2. 核心方法

#### `sendGetRequest(String url)`
- 发送GET请求获取页面内容
- 使用您提供的完整请求头信息
- 支持重定向和超时处理

#### `parseNewsPage(String html)`
- 解析HTML页面内容
- 智能识别新闻项容器
- 提取标题、URL、内容摘要、日期等信息
- 自动补全相对URL为绝对URL

#### `crawl(String keyword, int totalCount)`
- 主要爬取方法
- 支持关键词过滤
- 可控制爬取数量

#### `crawlLatest(int totalCount)`
- 爬取最新新闻
- 无关键词过滤

### 3. 请求头配置
使用了您提供的完整请求头信息：
```java
"Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
"Accept-Language": "zh-CN,zh;q=0.9"
"Cache-Control": "max-age=0"
"Priority": "u=0, i"
"Sec-Ch-Ua": "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\""
"Sec-Ch-Ua-Mobile": "?0"
"Sec-Ch-Ua-Platform": "\"Windows\""
"Sec-Fetch-Dest": "document"
"Sec-Fetch-Mode": "navigate"
"Sec-Fetch-Site": "same-origin"
"Sec-Fetch-User": "?1"
"Upgrade-Insecure-Requests": "1"
"Referer": "https://www.sgsgroup.com.cn/zh-cn"
"Referrer-Policy": "strict-origin-when-cross-origin"
```

### 4. 数据提取策略
- **标题提取**: 优先查找 h1-h4 标签，然后是 .title、.headline 等类名
- **URL提取**: 从链接元素中提取href属性
- **内容提取**: 查找 .content、.summary、.description 等类名
- **日期提取**: 查找 .date、.time、.publish-date 等类名
- **智能容器识别**: 支持多种新闻容器选择器

### 5. 错误处理
- 网络请求异常处理
- HTML解析异常处理
- 日期格式标准化
- 日志记录和警告

### 6. 测试功能
- `main` 方法提供独立测试
- 可用性检查
- 爬取功能测试
- 结果展示

## 技术特点

### 1. 参考SgsCrawler.java结构
- 实现了 `BaseCrawler` 接口
- 使用相同的依赖注入模式
- 保持一致的代码风格

### 2. 使用Jsoup进行HTML解析
- 灵活的CSS选择器
- 容错性强的解析逻辑
- 支持多种HTML结构

### 3. Java 11+ HttpClient
- 现代化的HTTP客户端
- 支持HTTP/2
- 异步处理能力

### 4. 智能数据提取
- 多种选择器策略
- 自动URL补全
- 日期格式标准化

## 使用方法

### 1. 直接测试
```bash
# 运行测试脚本
test_sgs_cn_crawler.bat
```

### 2. 在Spring Boot中使用
```java
@Autowired
private Sgs_cn sgsCnCrawler;

// 爬取最新5条新闻
List<CrawlerResult> results = sgsCnCrawler.crawlLatest(5);

// 带关键词搜索
List<CrawlerResult> results = sgsCnCrawler.crawl("认证", 10);
```

### 3. 可用性检查
```java
boolean available = sgsCnCrawler.isAvailable();
```

## 配置信息
- **基础URL**: https://www.sgsgroup.com.cn
- **新闻URL**: https://www.sgsgroup.com.cn/zh-cn/news
- **超时时间**: 30秒
- **重试次数**: 3次
- **用户代理**: Chrome 136.0.0.0

## 数据字段
爬取的数据包含以下字段：
- **标题** (title)
- **URL** (url)
- **内容摘要** (content)
- **发布日期** (date)
- **数据源** (source): "SGS中国"
- **分类** (category): "certification"
- **类型** (type): "news"
- **国家** (country): "CN"

## 注意事项
1. 需要网络连接访问SGS中国网站
2. 网站结构变化可能需要调整选择器
3. 建议控制爬取频率，避免对服务器造成压力
4. 日期格式解析依赖于 `DateFormatService`

## 后续扩展
1. 可以添加分页支持
2. 可以增加详情页面内容爬取
3. 可以添加数据保存到数据库的功能
4. 可以集成到现有的爬虫管理系统中

