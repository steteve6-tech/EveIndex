# Zeabur 部署指南

本指南将帮助您在 Zeabur 平台上部署认证监控系统。

## 📋 部署前准备

### 1. 环境要求
- GitHub 仓库（代码已推送到仓库）
- Zeabur 账户
- 火山引擎 API 密钥（可选）

### 2. 环境变量准备
在部署前，请准备以下环境变量：

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=your-secure-password
MYSQL_USERNAME=app_user
MYSQL_PASSWORD=your-app-password

# 火山引擎配置（可选）
VOLCENGINE_ACCESS_KEY=your-volcengine-access-key
VOLCENGINE_SECRET_KEY=your-volcengine-secret-key
ARK_API_KEY=your-ark-api-key

# 邮件配置（可选）
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-email-password
```

## 🚀 部署步骤

### 方法一：使用 Zeabur Dashboard（推荐）

1. **登录 Zeabur**
   - 访问 [Zeabur Dashboard](https://dash.zeabur.com)
   - 使用 GitHub 账户登录

2. **创建新项目**
   - 点击 "New Project"
   - 输入项目名称：`certification-monitor`

3. **添加服务**

   #### 3.1 添加 MySQL 数据库
   - 点击 "Add Service"
   - 选择 "Database" → "MySQL"
   - 版本选择 "8.0"
   - 服务名称：`mysql`

   #### 3.2 添加 Redis 缓存
   - 点击 "Add Service"
   - 选择 "Database" → "Redis"
   - 版本选择 "7.0"
   - 服务名称：`redis`

   #### 3.3 添加后端服务
   - 点击 "Add Service"
   - 选择 "Git"
   - 连接你的 GitHub 仓库
   - 选择分支：`main`
   - 构建路径：`./spring-boot-backend`
   - 服务名称：`backend`
   - 环境变量：
     ```bash
     SPRING_PROFILES_ACTIVE=zeabur
     SPRING_DATASOURCE_URL=${{mysql.DATABASE_URL}}
     SPRING_DATASOURCE_USERNAME=${{mysql.USERNAME}}
     SPRING_DATASOURCE_PASSWORD=${{mysql.PASSWORD}}
     SPRING_DATA_REDIS_HOST=${{redis.HOST}}
     SPRING_DATA_REDIS_PORT=${{redis.PORT}}
     VOLCENGINE_ACCESS_KEY=your-volcengine-access-key
     VOLCENGINE_SECRET_KEY=your-volcengine-secret-key
     ARK_API_KEY=your-ark-api-key
     ```

   #### 3.4 添加前端服务
   - 点击 "Add Service"
   - 选择 "Git"
   - 连接你的 GitHub 仓库
   - 选择分支：`main`
   - 构建路径：`./vue-frontend`
   - 服务名称：`frontend`
   - 环境变量：
     ```bash
     VITE_API_BASE_URL=${{backend.URL}}/api
     ```

### 方法二：使用 zeabur.yaml 配置文件

1. **推送配置文件**
   - 将 `zeabur.yaml` 文件推送到你的 GitHub 仓库根目录

2. **导入项目**
   - 在 Zeabur Dashboard 中点击 "Import from YAML"
   - 选择你的 GitHub 仓库
   - Zeabur 会自动读取 `zeabur.yaml` 配置

## 🔧 配置说明

### 后端配置
- **Dockerfile**: 使用 `Dockerfile.zeabur` 进行优化构建
- **配置文件**: 使用 `application-zeabur.yml`
- **端口**: 8080
- **健康检查**: `/api/health`

### 前端配置
- **Dockerfile**: 使用 `Dockerfile.zeabur` 进行优化构建
- **端口**: 80
- **健康检查**: `/`

### 数据库配置
- **MySQL**: 自动创建 `common_db` 数据库
- **Redis**: 用于缓存和会话存储

## 🌐 域名配置

### 1. 获取服务域名
部署完成后，每个服务都会获得一个 Zeabur 域名：
- 后端：`https://your-backend-service.zeabur.app`
- 前端：`https://your-frontend-service.zeabur.app`

### 2. 自定义域名（可选）
- 在服务设置中添加自定义域名
- 配置 DNS 记录指向 Zeabur

## 📊 监控和日志

### 1. 查看日志
- 在 Zeabur Dashboard 中点击服务
- 选择 "Logs" 标签页查看实时日志

### 2. 监控指标
- 在服务详情页面查看 CPU、内存使用情况
- 查看请求量和响应时间

### 3. 健康检查
- 后端健康检查：`https://your-backend-service.zeabur.app/api/health`
- 前端健康检查：`https://your-frontend-service.zeabur.app/`

## 🔄 更新部署

### 1. 自动部署
- 推送代码到 GitHub 主分支
- Zeabur 会自动检测更改并重新部署

### 2. 手动部署
- 在服务页面点击 "Redeploy"
- 选择要部署的提交

## 🛠️ 故障排除

### 常见问题

1. **构建失败**
   - 检查 Dockerfile 语法
   - 查看构建日志中的错误信息
   - 确认所有依赖文件都存在

2. **服务启动失败**
   - 检查环境变量配置
   - 查看服务日志
   - 确认数据库连接正常

3. **前端无法连接后端**
   - 检查 `VITE_API_BASE_URL` 环境变量
   - 确认后端服务正常运行
   - 检查 CORS 配置

### 调试命令

```bash
# 查看服务状态
zeabur status

# 查看服务日志
zeabur logs <service-name>

# 进入服务容器
zeabur exec <service-name> -- /bin/sh
```

## 📈 性能优化

### 1. 资源限制
- 在服务设置中配置 CPU 和内存限制
- 根据实际使用情况调整

### 2. 缓存配置
- 启用 Redis 缓存
- 配置适当的缓存过期时间

### 3. 数据库优化
- 定期清理日志表
- 优化数据库查询

## 🔒 安全配置

### 1. 环境变量
- 使用强密码
- 定期轮换 API 密钥
- 不要在代码中硬编码敏感信息

### 2. 网络安全
- 配置适当的 CORS 策略
- 使用 HTTPS
- 限制数据库访问

## 📞 支持

如果遇到问题，可以：
1. 查看 Zeabur 官方文档
2. 检查服务日志
3. 联系技术支持

---

**注意**: 请根据实际情况调整配置参数，特别是数据库密码和 API 密钥。
