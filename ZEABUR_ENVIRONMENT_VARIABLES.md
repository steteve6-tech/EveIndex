# Zeabur环境变量配置指南

## 🚨 当前问题

后端服务仍然尝试连接到 `zeabur` 数据库，而不是 `common_db`。这是因为环境变量没有正确设置。

## 🔧 解决方案

### 1. 在Zeabur Dashboard中正确设置环境变量

#### MySQL服务环境变量：
```bash
MYSQL_ROOT_PASSWORD=your-secure-root-password
MYSQL_DATABASE=common_db
MYSQL_USER=app_user
MYSQL_PASSWORD=your-secure-app-password
```

#### 后端服务环境变量：
```bash
SPRING_PROFILES_ACTIVE=zeabur
SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
```

### 2. 重要说明

**⚠️ 关键点：**
- 不要手动设置 `SPRING_DATASOURCE_URL` 的值
- 必须使用 `${{mysql.DATABASE_URL}}` 格式
- 这会自动指向 `common_db` 数据库

### 3. 部署步骤

#### 步骤1：删除旧的环境变量
如果之前设置了错误的 `SPRING_DATASOURCE_URL`，请删除它。

#### 步骤2：重新配置环境变量
在后端服务中设置：
```bash
SPRING_PROFILES_ACTIVE=zeabur
SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
```

#### 步骤3：重新部署
1. 保存环境变量配置
2. 重新部署后端服务
3. 等待部署完成

### 4. 验证配置

部署完成后，检查后端服务日志，应该看到：
```
Connected to database: common_db
```

而不是：
```
Connected to database: zeabur
```

### 5. 常见错误

#### 错误1：手动设置SPRING_DATASOURCE_URL
```bash
# ❌ 错误 - 不要这样做
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/common_db

# ✅ 正确 - 使用服务引用
SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
```

#### 错误2：数据库名称不匹配
```bash
# ❌ 错误 - MySQL服务中设置错误的数据库名
MYSQL_DATABASE=zeabur

# ✅ 正确 - 使用common_db
MYSQL_DATABASE=common_db
```

### 6. 调试步骤

如果问题仍然存在：

1. **检查MySQL服务状态**
   - 确认MySQL服务已启动
   - 查看MySQL服务日志

2. **检查环境变量**
   - 在后端服务中查看环境变量
   - 确认 `SPRING_DATASOURCE_URL` 指向正确的数据库

3. **查看后端日志**
   - 检查连接URL是否正确
   - 查看数据库连接错误信息

### 7. 完整的服务配置

#### MySQL服务：
```yaml
name: mysql
type: mysql
version: "8.0"
env:
  - MYSQL_ROOT_PASSWORD=your-secure-root-password
  - MYSQL_DATABASE=common_db
  - MYSQL_USER=app_user
  - MYSQL_PASSWORD=your-secure-app-password
```

#### 后端服务：
```yaml
name: backend
source:
  type: git
  repo: your-github-repo
  branch: main
  buildPath: ./spring-boot-backend
dockerfile: Dockerfile.zeabur
env:
  - SPRING_PROFILES_ACTIVE=zeabur
  - SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
  - SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
  - SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
```

## 📝 总结

问题的根本原因是环境变量配置不正确。通过正确设置 `${{mysql.DATABASE_URL}}` 引用，后端服务将自动连接到 `common_db` 数据库，而不是 `zeabur`。

关键步骤：
1. ✅ 确保MySQL服务配置了 `MYSQL_DATABASE=common_db`
2. ✅ 后端服务使用 `SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}`
3. ✅ 重新部署服务
4. ✅ 验证连接成功
