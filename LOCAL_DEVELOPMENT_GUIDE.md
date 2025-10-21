# 🛠️ 本地开发环境配置指南

## 📋 快速开始

本指南将帮助你在本地运行完整的医疗器械认证监控系统，包括：
- ✅ 数据库服务（MySQL + Redis）
- ✅ 后端服务（Spring Boot）
- ✅ 前端服务（Vue 3）
- ✅ AI 功能（OpenAI + 火山引擎）
- ✅ 翻译功能（火山引擎翻译）

---

## 🎯 方式一：使用 Docker Compose（推荐）

### 优点
- ✅ 一键启动所有服务
- ✅ 数据库自动初始化
- ✅ 环境隔离，不影响本地环境
- ✅ 配置简单，适合快速开发

### 步骤1: 配置环境变量

```bash
# 1. 复制开发环境配置模板
cp .env.dev.example .env.dev

# 如果没有 .env.dev.example，创建新文件
notepad .env.dev  # Windows
# 或
vim .env.dev      # Linux/Mac
```

在 `.env.dev` 中填入以下内容：

```bash
# ================================
# 开发环境配置文件
# ================================

# ================================
# 数据库配置（必须）
# ================================
MYSQL_ROOT_PASSWORD=dev123
MYSQL_DATABASE=common_db
MYSQL_USER=dev_user
MYSQL_PASSWORD=dev123

# ================================
# Redis 配置（可选，不设密码）
# ================================
REDIS_PASSWORD=

# ================================
# Druid 监控配置
# ================================
DRUID_USERNAME=admin
DRUID_PASSWORD=admin123

# ================================
# CORS 跨域配置
# ================================
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3100,http://127.0.0.1:3000

# ================================
# AI 功能配置（可选）
# ================================
# OpenAI API 配置
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_MODEL=gpt-4o
OPENAI_BASE_URL=https://api.openai.com/v1
OPENAI_TIMEOUT=60

# 火山引擎翻译服务配置
VOLCENGINE_ACCESS_KEY=your_volcengine_access_key
VOLCENGINE_SECRET_KEY=your_volcengine_secret_key
VOLCENGINE_REGION=cn-beijing

# 火山引擎 AI 配置
ARK_API_KEY=your_ark_api_key
ARK_MODEL_ID=bot-20250915145921-rspmk

# ================================
# 应用配置
# ================================
APP_ENV=development
SWAGGER_ENABLED=true
AI_CLASSIFICATION_ENABLED=true

# ================================
# 日志配置
# ================================
LOG_LEVEL=DEBUG
```

### 步骤2: 启动所有服务

```bash
# 使用 Podman（推荐）
podman-compose -f docker-compose.dev.yml up -d

# 或使用 Docker
docker-compose -f docker-compose.dev.yml up -d
```

### 步骤3: 等待服务启动

```bash
# 查看容器状态
podman-compose -f docker-compose.dev.yml ps

# 查看启动日志
podman-compose -f docker-compose.dev.yml logs -f

# 等待数据库初始化完成（约30秒-1分钟）
podman-compose -f docker-compose.dev.yml logs mysql | grep "ready for connections"
```

### 步骤4: 访问系统

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端应用** | http://localhost:3000 | Vue 3 开发服务器 |
| **后端API** | http://localhost:8080/api | Spring Boot API |
| **API文档** | http://localhost:8080/api/doc.html | Knife4j 文档 |
| **数据库管理** | http://localhost:8081 | phpMyAdmin（root/dev123）|
| **Druid监控** | http://localhost:8080/druid | 数据库监控（admin/admin123）|

---

## 🎯 方式二：本地手动运行（适合开发调试）

### 优点
- ✅ 代码修改实时生效
- ✅ 便于调试和断点
- ✅ IDE 集成更好
- ✅ 资源占用更少

### 前置要求

确保安装以下软件：
- ✅ **JDK 17+**（推荐 JDK 21）
- ✅ **Maven 3.8+**
- ✅ **Node.js 18+**（推荐 Node 20）
- ✅ **MySQL 8.0**（本地安装或 Docker）
- ✅ **Redis 7**（可选，推荐安装）

### 步骤1: 启动数据库

#### 选项A: 使用 Docker 启动数据库（推荐）

```bash
# 只启动 MySQL 和 Redis
podman-compose -f docker-compose.dev.yml up -d mysql redis phpmyadmin

# 查看数据库日志
podman-compose -f docker-compose.dev.yml logs -f mysql
```

#### 选项B: 使用本地 MySQL

```bash
# 1. 启动 MySQL 服务
# Windows: 在服务管理中启动 MySQL
# Linux: sudo systemctl start mysql
# Mac: brew services start mysql

# 2. 创建数据库
mysql -uroot -p
```

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS common_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS 'dev_user'@'localhost' IDENTIFIED BY 'dev123';
GRANT ALL PRIVILEGES ON common_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

```bash
# 3. 初始化数据库表
mysql -uroot -p common_db < database/init_database_full.sql
```

### 步骤2: 配置后端环境变量

#### 方法A: 使用环境变量（推荐）

**Windows PowerShell:**
```powershell
# 设置环境变量
$env:DATABASE_URL="jdbc:mysql://localhost:3306/common_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8"
$env:DATABASE_USERNAME="root"
$env:DATABASE_PASSWORD="dev123"

# AI 功能（可选）
$env:OPENAI_API_KEY="your_openai_api_key"
$env:VOLCENGINE_ACCESS_KEY="your_volcengine_access_key"
$env:VOLCENGINE_SECRET_KEY="your_volcengine_secret_key"
```

**Linux/Mac Bash:**
```bash
# 设置环境变量
export DATABASE_URL="jdbc:mysql://localhost:3306/common_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="dev123"

# AI 功能（可选）
export OPENAI_API_KEY="your_openai_api_key"
export VOLCENGINE_ACCESS_KEY="your_volcengine_access_key"
export VOLCENGINE_SECRET_KEY="your_volcengine_secret_key"
```

#### 方法B: 创建 application-local.yml（更简单）

在 `spring-boot-backend/src/main/resources/` 创建 `application-local.yml`：

```yaml
spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/common_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: dev123

  # Redis 配置（如果本地没有 Redis，可以注释掉）
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

# OpenAI API 配置（填入你的真实 API Key）
openai:
  api:
    key: your_openai_api_key_here
    model: gpt-4o
    base-url: https://api.openai.com/v1

# 火山引擎翻译配置（填入你的真实密钥）
volcengine:
  translate:
    access-key: your_volcengine_access_key
    secret-key: your_volcengine_secret_key
    region: cn-beijing

# 火山引擎 AI 配置（填入你的真实密钥）
volc:
  ai:
    api-key: your_ark_api_key

# 开发环境日志
logging:
  level:
    com.certification: DEBUG
    root: INFO
```

**注意**：`application-local.yml` 已在 `.gitignore` 中，不会被提交到 Git。

### 步骤3: 启动后端服务

```bash
# 进入后端目录
cd spring-boot-backend

# 方法1: 使用 Maven 启动（开发模式，支持热重载）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 方法2: 使用 IDE 启动
# 在 IDEA 或 Eclipse 中：
# 1. 打开 CertificationApplication.java
# 2. 右键 -> Run 'CertificationApplication'
# 3. 在 Run Configuration 中添加 VM options: -Dspring.profiles.active=local

# 方法3: 打包后运行
mvn clean package -DskipTests
java -jar target/certification-monitor-*.jar --spring.profiles.active=local
```

启动成功后，访问：
- API: http://localhost:8080/api
- Swagger 文档: http://localhost:8080/api/doc.html
- Druid 监控: http://localhost:8080/druid

### 步骤4: 启动前端服务

```bash
# 进入前端目录
cd vue-frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 或指定端口
npm run dev -- --port 3100
```

启动成功后，访问：
- 前端应用: http://localhost:3000

---

## 🔧 AI 功能配置详解

### 1. OpenAI API 配置

**获取 API Key：**
1. 访问 https://platform.openai.com/api-keys
2. 登录账号
3. 点击 "Create new secret key"
4. 复制生成的 API Key（格式：`sk-...`）

**配置到环境变量：**
```bash
# Windows
$env:OPENAI_API_KEY="sk-your-actual-key-here"

# Linux/Mac
export OPENAI_API_KEY="sk-your-actual-key-here"
```

**用途：**
- ✅ AI 智能审核功能
- ✅ 自动判断设备是否相关
- ✅ 数据分类和打标签

**测试 OpenAI 功能：**
```bash
# 访问 API 文档
http://localhost:8080/api/doc.html

# 找到 "AI智能审核" 模块
# 测试接口：POST /api/ai/smart-audit
```

### 2. 火山引擎翻译配置

**获取密钥：**
1. 访问火山引擎控制台：https://console.volcengine.com/
2. 进入"访问控制" → "访问密钥"
3. 创建新密钥，获取 Access Key 和 Secret Key

**配置到环境变量：**
```bash
# Windows
$env:VOLCENGINE_ACCESS_KEY="AKLT..."
$env:VOLCENGINE_SECRET_KEY="your-secret-key"

# Linux/Mac
export VOLCENGINE_ACCESS_KEY="AKLT..."
export VOLCENGINE_SECRET_KEY="your-secret-key"
```

**用途：**
- ✅ 自动翻译英文/韩文/日文数据
- ✅ 多语言数据处理

**测试翻译功能：**
```bash
# 运行测试类
cd spring-boot-backend
mvn test -Dtest=TestTranslateText

# 或直接运行
java -cp target/classes com.certification.analysis.analysisByai.TestTranslateText
```

### 3. 火山引擎 AI 配置

**获取 API Key：**
1. 访问火山引擎 AI 平台
2. 创建 Bot 应用
3. 获取 API Key

**配置到环境变量：**
```bash
# Windows
$env:ARK_API_KEY="your-ark-api-key"

# Linux/Mac
export ARK_API_KEY="your-ark-api-key"
```

---

## 📊 功能测试清单

### 1. 数据库功能测试

```bash
# 访问 phpMyAdmin
http://localhost:8081

# 登录信息
用户名: root
密码: dev123（或你配置的密码）

# 检查表是否创建成功
# 应该看到 19 张表：
# - t_crawler_data
# - t_device_510k
# - t_device_registration
# - ... 等共 19 张表
```

### 2. 后端 API 测试

```bash
# 1. 健康检查
curl http://localhost:8080/api/health

# 2. 查看 Swagger 文档
http://localhost:8080/api/doc.html

# 3. 测试数据查询
curl http://localhost:8080/api/device/510k?page=0&size=10
```

### 3. 前端功能测试

访问 http://localhost:3000，测试以下功能：
- [ ] 页面正常加载
- [ ] 可以看到侧边栏菜单
- [ ] 数据列表可以正常显示
- [ ] 可以进行数据筛选
- [ ] 可以查看数据详情

### 4. AI 功能测试

#### OpenAI 智能审核测试

1. 访问 Swagger 文档：http://localhost:8080/api/doc.html
2. 找到 "AI智能审核" 模块
3. 测试 `POST /api/ai/smart-audit` 接口
4. 输入测试数据：
   ```json
   {
     "productName": "Surgical Mask",
     "manufacturer": "3M",
     "productCode": "FXX"
   }
   ```
5. 查看返回结果是否包含相关性判断

#### 翻译功能测试

```bash
# 设置环境变量后运行测试
cd spring-boot-backend
export VOLCENGINE_ACCESS_KEY="your_key"
export VOLCENGINE_SECRET_KEY="your_secret"

# 运行翻译测试
mvn exec:java -Dexec.mainClass="com.certification.analysis.analysisByai.TestTranslateText"
```

### 5. 爬虫功能测试

1. 访问前端：http://localhost:3000
2. 进入"爬虫管理"模块
3. 选择一个爬虫任务（如 US_510K）
4. 点击"立即执行"
5. 查看任务日志

---

## 🐛 常见问题解决

### Q1: 后端启动失败 - 数据库连接失败

**错误信息：**
```
Cannot connect to MySQL server on 'localhost'
```

**解决方案：**
```bash
# 1. 检查 MySQL 是否启动
# Windows: 打开服务管理器，检查 MySQL 服务
# Linux: sudo systemctl status mysql
# Mac: brew services list | grep mysql

# 2. 检查端口是否正确
netstat -ano | findstr 3306

# 3. 检查用户名密码
mysql -uroot -pdev123 -e "SELECT 1"

# 4. 检查数据库是否存在
mysql -uroot -pdev123 -e "SHOW DATABASES LIKE 'common_db'"
```

### Q2: OpenAI API 调用失败

**错误信息：**
```
OpenAI API key is not configured
```

**解决方案：**
```bash
# 1. 检查环境变量是否设置
# Windows
echo $env:OPENAI_API_KEY

# Linux/Mac
echo $OPENAI_API_KEY

# 2. 如果为空，重新设置
# Windows
$env:OPENAI_API_KEY="sk-your-key-here"

# Linux/Mac
export OPENAI_API_KEY="sk-your-key-here"

# 3. 重启后端服务
```

### Q3: 前端无法访问后端 API

**错误信息：**
```
CORS policy: No 'Access-Control-Allow-Origin' header
```

**解决方案：**

1. 检查 `application.yml` 中的 CORS 配置：
```yaml
app:
  cors:
    allowed-origins: "http://localhost:3000,http://localhost:3100"
```

2. 或设置环境变量：
```bash
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:3100"
```

3. 重启后端服务

### Q4: Redis 连接失败

**错误信息：**
```
Cannot connect to Redis server
```

**解决方案：**

如果不使用 Redis，可以临时禁用：

在 `application-local.yml` 中注释 Redis 配置：
```yaml
spring:
  # data:
  #   redis:
  #     host: localhost
  #     port: 6379
```

或启动 Redis：
```bash
# Docker 方式
podman run -d -p 6379:6379 --name redis redis:7

# 本地安装方式
# Windows: redis-server.exe
# Linux: sudo systemctl start redis
# Mac: brew services start redis
```

### Q5: 数据库表未创建

**解决方案：**
```bash
# 手动执行初始化脚本
mysql -uroot -pdev123 common_db < database/init_database_full.sql

# 检查表是否创建成功
mysql -uroot -pdev123 -e "USE common_db; SHOW TABLES;"

# 应该看到 19 张表
```

---

## 📝 开发工作流

### 日常开发流程

```bash
# 1. 启动数据库（如果使用 Docker）
podman-compose -f docker-compose.dev.yml up -d mysql redis

# 2. 启动后端（在 IDE 中运行或使用 Maven）
cd spring-boot-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. 启动前端
cd vue-frontend
npm run dev

# 4. 开始开发
# - 修改代码
# - 保存文件（前端会自动热重载）
# - 后端需要重启（或使用 Spring DevTools）

# 5. 停止服务
# Ctrl+C 停止前端和后端
# podman-compose -f docker-compose.dev.yml stop
```

### 代码提交前检查

```bash
# 1. 运行类型检查（前端）
cd vue-frontend
npm run type-check

# 2. 运行构建测试
npm run build

# 3. 后端编译检查
cd spring-boot-backend
mvn clean compile

# 4. 检查是否有敏感信息
git diff | grep -i "api.key\|password\|secret"

# 5. 提交代码
git add .
git commit -m "feat: 添加新功能"
git push
```

---

## 🔗 相关文档

- [部署指南](./DEPLOYMENT_README.md) - 生产环境部署
- [Podman 部署](./PODMAN_DEPLOYMENT_GUIDE.md) - 使用 Podman 部署
- [系统维护](./SYSTEM_MAINTENANCE_GUIDE.md) - 系统维护和运维
- [安全检查](./SECURITY_CHECKLIST.md) - 安全配置清单
- [数据库文档](./database/README.md) - 数据库表结构

---

## 📞 获取帮助

遇到问题时：
1. 查看本文档的"常见问题"章节
2. 检查日志输出
3. 查阅相关文档
4. 在 GitHub Issues 中提问

---

**文档版本**: v1.0.0
**最后更新**: 2025-10-21
**适用环境**: 开发环境（Windows/Linux/macOS）
