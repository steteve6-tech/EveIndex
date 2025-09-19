# 高风险数据管理后端实现

## 概述

这个后端实现为高风险数据管理提供了完整的关键词筛选和国家筛选功能。

## 主要功能

### 1. 关键词筛选
- 支持根据关键词筛选高风险数据
- 提供关键词统计功能
- 支持关键词编辑功能

### 2. 国家筛选
- 支持根据国家筛选高风险数据
- 提供按国家分类的统计数据

### 3. 组合筛选
- 支持关键词和国家组合筛选
- 实时更新统计数据

## API接口

### 1. 获取高风险数据统计
```
GET /api/high-risk-data/statistics
```

### 2. 按国家获取高风险数据统计
```
GET /api/high-risk-data/statistics/by-country
```

### 3. 获取关键词统计
```
GET /api/high-risk-data/keywords/statistics
```

### 4. 按类型获取高风险数据（支持关键词和国家筛选）
```
GET /api/high-risk-data/{dataType}?keyword={keyword}&country={country}&page={page}&size={size}
```

参数说明：
- `dataType`: 数据类型（device510k, recall, event, registration, guidance, customs）
- `keyword`: 关键词（可选）
- `country`: 国家代码（可选）
- `page`: 页码（默认0）
- `size`: 每页大小（默认20）

### 5. 更新风险等级
```
PUT /api/high-risk-data/{dataType}/{id}/risk-level
```

### 6. 批量更新风险等级
```
PUT /api/high-risk-data/batch/risk-level
```

### 7. 更新关键词
```
PUT /api/high-risk-data/{dataType}/{id}/keywords
```

## 数据库设计

### 主要字段
- `id`: 主键
- `device_name`: 设备名称
- `applicant`: 申请人
- `date_received`: 接收日期
- `country`: 国家
- `matched_keywords`: 匹配的关键词（逗号分隔）
- `matched_fields`: 匹配的字段（逗号分隔）
- `risk_level`: 风险等级
- `created_at`: 创建时间
- `updated_at`: 更新时间

## 实现要点

### 1. 关键词筛选实现
使用 `LIKE` 查询和 `CONTAINING` 方法实现关键词的模糊匹配：

```java
Page<Device510K> findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
    String keyword, String riskLevel, Pageable pageable);
```

### 2. 国家筛选实现
根据国家字段进行精确匹配：

```java
Page<Device510K> findByCountryAndRiskLevel(String country, String riskLevel, Pageable pageable);
```

### 3. 组合筛选实现
同时支持关键词和国家筛选：

```java
Page<Device510K> findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
    String country, String keyword, String riskLevel, Pageable pageable);
```

### 4. 关键词统计实现
使用原生SQL查询统计每个关键词的出现次数：

```sql
SELECT DISTINCT keyword, COUNT(*) as count 
FROM (
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(matched_keywords, ',', numbers.n), ',', -1)) as keyword 
    FROM device510k 
    CROSS JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) numbers 
    WHERE CHAR_LENGTH(matched_keywords) - CHAR_LENGTH(REPLACE(matched_keywords, ',', '')) >= numbers.n - 1 
    AND risk_level = 'HIGH'
) keywords 
WHERE keyword != '' 
GROUP BY keyword 
ORDER BY count DESC
```

## 部署说明

### 1. 环境要求
- Java 8+
- Spring Boot 2.x
- MySQL 5.7+
- Maven 3.x

### 2. 配置数据库
在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/high_risk_data
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

## 测试

### 1. 使用Postman测试
导入API接口进行测试，验证关键词筛选和国家筛选功能。

### 2. 前端集成测试
启动前端应用，测试关键词筛选功能是否正常工作。

## 注意事项

1. **性能优化**: 对于大量数据，建议在 `matched_keywords` 字段上创建索引
2. **关键词分隔**: 使用逗号分隔多个关键词，确保查询逻辑正确
3. **错误处理**: 添加适当的异常处理和日志记录
4. **数据验证**: 在更新操作前验证数据的有效性

## 扩展功能

1. **高级搜索**: 支持正则表达式搜索
2. **搜索历史**: 记录用户的搜索历史
3. **搜索建议**: 提供关键词自动补全功能
4. **数据导出**: 支持筛选结果的导出功能
