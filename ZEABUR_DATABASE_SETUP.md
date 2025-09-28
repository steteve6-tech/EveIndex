# Zeabur数据库连接问题解决方案

## 🚨 问题描述

错误信息：`Unknown database 'zeabur'`

这个错误表明后端尝试连接到名为 `zeabur` 的数据库，但该数据库不存在。

## 🔧 解决方案

### 1. 更新Zeabur MySQL服务配置

在Zeabur Dashboard中配置MySQL服务时，确保设置以下环境变量：

```bash
MYSQL_ROOT_PASSWORD=your-secure-root-password
MYSQL_DATABASE=common_db
MYSQL_USER=app_user
MYSQL_PASSWORD=your-secure-app-password
```

### 2. 更新后端服务环境变量

在后端服务的环境变量中设置：

```bash
SPRING_PROFILES_ACTIVE=zeabur
SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
```

### 3. 部署步骤

#### 步骤1：创建MySQL服务
1. 在Zeabur Dashboard中点击 "Add Service"
2. 选择 "Database" → "MySQL"
3. 版本选择 "8.0"
4. 服务名称：`mysql`
5. 设置环境变量：
   ```bash
   MYSQL_ROOT_PASSWORD=your-secure-root-password
   MYSQL_DATABASE=common_db
   MYSQL_USER=app_user
   MYSQL_PASSWORD=your-secure-app-password
   ```

#### 步骤2：创建后端服务
1. 点击 "Add Service"
2. 选择 "Git"
3. 连接你的GitHub仓库
4. 选择分支：`main`
5. 构建路径：`./spring-boot-backend`
6. 服务名称：`backend`
7. 使用Dockerfile：`Dockerfile.zeabur`
8. 设置环境变量：
   ```bash
   SPRING_PROFILES_ACTIVE=zeabur
   SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
   SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
   SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
   ```

#### 步骤3：初始化数据库
1. 等待MySQL服务启动完成
2. 在MySQL服务中执行 `init-zeabur-database.sql` 脚本
3. 然后执行 `create_database_schema.sql` 脚本创建表结构

### 4. 验证连接

#### 检查数据库连接
```bash
# 在Zeabur Dashboard中查看后端服务日志
# 应该看到类似以下信息：
# "Started CertificationMonitorApplication in X.XXX seconds"
```

#### 健康检查
```bash
curl https://your-backend-domain.zeabur.app/api/health
```

### 5. 常见问题解决

#### 问题1：数据库连接超时
**解决方案：**
- 检查MySQL服务是否已完全启动
- 确认网络连接正常
- 增加连接超时时间

#### 问题2：权限不足
**解决方案：**
- 确认用户 `app_user` 有足够权限
- 检查GRANT语句是否正确执行

#### 问题3：字符集问题
**解决方案：**
- 确保数据库使用 `utf8mb4` 字符集
- 检查连接URL中的字符编码参数

### 6. 手动数据库初始化

如果自动初始化失败，可以手动执行：

1. **连接到MySQL服务**
   ```bash
   # 在Zeabur Dashboard中进入MySQL服务
   # 使用Web终端或连接工具
   ```

2. **执行初始化脚本**
   ```sql
   -- 创建数据库
   CREATE DATABASE IF NOT EXISTS common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- 创建用户
   CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'your-secure-password';
   GRANT ALL PRIVILEGES ON common_db.* TO 'app_user'@'%';
   FLUSH PRIVILEGES;
   
   -- 使用数据库
   USE common_db;
   ```

3. **创建表结构**
   ```sql
   -- 执行 create_database_schema.sql 中的表创建语句
   ```

### 7. 监控和调试

#### 查看服务日志
1. 在Zeabur Dashboard中点击后端服务
2. 选择 "Logs" 标签页
3. 查看启动日志和错误信息

#### 数据库连接测试
```bash
# 在后端服务中测试数据库连接
curl -X GET "https://your-backend-domain.zeabur.app/api/health" \
  -H "Content-Type: application/json"
```

### 8. 最佳实践

1. **使用环境变量管理敏感信息**
2. **设置强密码**
3. **定期备份数据库**
4. **监控数据库性能**
5. **使用连接池优化连接**

## 📝 总结

通过以上步骤，你应该能够成功解决 "Unknown database 'zeabur'" 错误，并让后端正确连接到Zeabur上的MySQL数据库。

关键点：
- ✅ 确保MySQL服务配置了正确的数据库名称
- ✅ 使用Zeabur的环境变量引用语法
- ✅ 正确初始化数据库和表结构
- ✅ 验证连接和权限设置
