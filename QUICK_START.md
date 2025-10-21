# ⚡ 快速启动指南

## 🎯 选择你的启动方式

### 方式 1️⃣: 一键启动（最简单） ⭐ 推荐

**适合**: 快速体验、演示、测试

```bash
# Windows
start-local-dev.bat

# Linux/Mac
bash start-local-dev.sh
```

**包含**: MySQL + Redis + phpMyAdmin

**后续步骤**:
1. 等待数据库启动（约 30 秒）
2. 新窗口启动后端：`cd spring-boot-backend && mvn spring-boot:run`
3. 新窗口启动前端：`cd vue-frontend && npm run dev`
4. 访问：http://localhost:3000

---

### 方式 2️⃣: Docker Compose 完整部署

**适合**: 开发环境、团队协作

```bash
# 1. 配置环境变量
cp .env.dev.example .env.dev
notepad .env.dev  # 编辑配置

# 2. 启动所有服务
podman-compose -f docker-compose.dev.yml up -d

# 3. 查看日志
podman-compose -f docker-compose.dev.yml logs -f
```

**包含**: MySQL + Redis + 后端 + 前端 + phpMyAdmin

**访问**:
- 前端: http://localhost:3000
- 后端: http://localhost:8080/api
- 文档: http://localhost:8080/api/doc.html
- 数据库: http://localhost:8081

---

### 方式 3️⃣: 手动启动（完全控制）

**适合**: 开发调试、性能优化

#### 步骤1: 启动数据库
```bash
# Docker 方式（推荐）
podman-compose -f docker-compose.dev.yml up -d mysql redis

# 或本地 MySQL
mysql -uroot -p < database/init_database_full.sql
```

#### 步骤2: 配置环境变量
```bash
# Windows PowerShell
$env:DATABASE_URL="jdbc:mysql://localhost:3306/common_db"
$env:DATABASE_USERNAME="root"
$env:DATABASE_PASSWORD="dev123"

# Linux/Mac
export DATABASE_URL="jdbc:mysql://localhost:3306/common_db"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="dev123"
```

#### 步骤3: 启动后端
```bash
cd spring-boot-backend
mvn spring-boot:run
```

#### 步骤4: 启动前端
```bash
cd vue-frontend
npm install  # 首次运行
npm run dev
```

---

## 🤖 启用 AI 功能（可选）

### 快速配置（2 分钟）

1. **获取 OpenAI API Key**
   - 访问：https://platform.openai.com/api-keys
   - 创建新密钥

2. **配置到环境变量**
   ```bash
   # 编辑 .env.dev
   OPENAI_API_KEY=sk-proj-your-key-here

   # 或设置环境变量
   export OPENAI_API_KEY="sk-proj-your-key-here"
   ```

3. **重启后端服务**

📖 详细配置：[AI_SETUP_QUICK_START.md](./AI_SETUP_QUICK_START.md)

---

## 📊 验证启动成功

### 检查清单

- [ ] 数据库运行：`http://localhost:8081`（phpMyAdmin）
- [ ] 后端运行：`http://localhost:8080/api/health`
- [ ] API 文档：`http://localhost:8080/api/doc.html`
- [ ] 前端运行：`http://localhost:3000`

### 测试功能

```bash
# 1. 测试后端健康检查
curl http://localhost:8080/api/health

# 2. 查看数据库表
访问 http://localhost:8081
登录: root / dev123
查看 common_db 数据库（应有 19 张表）

# 3. 测试前端
访问 http://localhost:3000
应该看到系统主界面
```

---

## 🛑 停止服务

```bash
# 方式1: 停止 Docker 服务
podman-compose -f docker-compose.dev.yml stop

# 方式2: 停止并清理
podman-compose -f docker-compose.dev.yml down

# 方式3: 停止进程（手动启动时）
# Ctrl+C 停止前端和后端进程
```

---

## 🐛 遇到问题？

### 常见问题快速解决

| 问题 | 解决方案 |
|------|----------|
| 端口被占用 | 修改 docker-compose.dev.yml 中的端口映射 |
| 数据库连接失败 | 检查 MySQL 是否启动：`podman ps` |
| API 调用 CORS 错误 | 检查 CORS_ALLOWED_ORIGINS 配置 |
| 前端白屏 | 查看浏览器控制台错误 |
| AI 功能不可用 | 检查 OPENAI_API_KEY 是否配置 |

### 查看日志

```bash
# Docker 服务日志
podman-compose -f docker-compose.dev.yml logs -f backend
podman-compose -f docker-compose.dev.yml logs -f mysql

# 手动启动时查看控制台输出
```

### 完全重置

```bash
# 停止所有服务
podman-compose -f docker-compose.dev.yml down -v

# 重新启动
bash start-local-dev.sh  # 或 start-local-dev.bat
```

---

## 📚 详细文档

- 📖 [本地开发指南](./LOCAL_DEVELOPMENT_GUIDE.md) - 完整的开发环境配置
- 🤖 [AI 功能配置](./AI_SETUP_QUICK_START.md) - OpenAI 和火山引擎配置
- 🚀 [生产部署指南](./DEPLOYMENT_README.md) - 服务器部署
- 🔒 [安全检查清单](./SECURITY_CHECKLIST.md) - 安全配置
- 🛠️ [系统维护指南](./SYSTEM_MAINTENANCE_GUIDE.md) - 运维管理

---

## 🎓 新手推荐路径

### 第一次使用

1. **快速体验**（10 分钟）
   ```bash
   # 1. 一键启动数据库
   start-local-dev.bat

   # 2. 启动后端（新窗口）
   cd spring-boot-backend
   mvn spring-boot:run

   # 3. 启动前端（新窗口）
   cd vue-frontend
   npm run dev

   # 4. 访问系统
   浏览器打开 http://localhost:3000
   ```

2. **浏览功能**（20 分钟）
   - 查看 API 文档：http://localhost:8080/api/doc.html
   - 浏览数据库结构：http://localhost:8081
   - 测试爬虫功能：前端 → 爬虫管理
   - 查看数据展示：前端 → 设备数据

3. **配置 AI**（5 分钟）
   - 获取 OpenAI API Key
   - 配置到 `.env.dev`
   - 重启后端
   - 测试 AI 审核功能

### 深入开发

1. 阅读 [LOCAL_DEVELOPMENT_GUIDE.md](./LOCAL_DEVELOPMENT_GUIDE.md)
2. 了解数据库结构：[database/README.md](./database/README.md)
3. 学习系统架构：[README.md](./README.md)
4. 查看维护指南：[SYSTEM_MAINTENANCE_GUIDE.md](./SYSTEM_MAINTENANCE_GUIDE.md)

---

## 💡 提示

- ⚡ 使用 `start-local-dev.bat` 是最快的启动方式
- 🔧 开发时推荐使用 IDE 启动后端，便于调试
- 📝 修改代码后前端会自动热重载，后端需要重启
- 🚀 完整的生产部署使用 `docker-compose.prod.yml`
- 🤖 AI 功能是可选的，不配置也能运行基本功能

---

**文档版本**: v1.0.0
**最后更新**: 2025-10-21
**适用环境**: Windows/Linux/macOS
