# 🚀 快速部署指南

## 📋 系统概述

本项目已配置完整的 Docker Compose 部署方案，包含：

- ✅ **MySQL 8.0** - 数据库（自动初始化19张表）
- ✅ **Redis 7** - 缓存服务
- ✅ **Spring Boot** - 后端服务
- ✅ **Vue 3** - 前端应用
- ✅ **phpMyAdmin** - 数据库管理工具
- ✅ **Nginx** - 反向代理（可选）

**所有服务都运行在 Docker 容器中，无需手动安装配置！**

---

## 🎯 一键部署（推荐）

### Windows 系统

```cmd
# 双击运行或命令行执行
deploy-quick-start.bat
```

### Linux/Mac 系统

```bash
# 赋予执行权限
chmod +x deploy-quick-start.sh

# 运行部署脚本
bash deploy-quick-start.sh
```

**脚本会自动完成**：
1. ✅ 检查系统依赖
2. ✅ 创建环境配置文件
3. ✅ 检查数据库初始化脚本
4. ✅ 停止旧容器
5. ✅ 构建 Docker 镜像
6. ✅ 启动所有服务
7. ✅ 验证部署状态

---

## 📝 手动部署步骤

### 前置要求

- ✅ **Podman Desktop** 或 **Docker Desktop** 已安装
- ✅ 至少 4GB 可用内存
- ✅ 至少 10GB 可用磁盘空间

### 步骤1: 配置环境变量

```bash
# 复制最小化配置模板
cp .env.prod.minimal .env.prod

# 编辑配置文件
vim .env.prod  # Linux/Mac
notepad .env.prod  # Windows
```

**必须修改的配置**：
```bash
# 修改所有密码为强密码
MYSQL_ROOT_PASSWORD=你的强密码
MYSQL_PASSWORD=你的强密码
REDIS_PASSWORD=你的强密码
DRUID_PASSWORD=你的强密码

# 修改为实际服务器IP或域名
CORS_ALLOWED_ORIGINS=http://your-server-ip,http://your-domain.com
```

### 步骤2: 构建并启动

```bash
# 使用 Podman
podman-compose -f docker-compose.prod.yml up -d --build

# 或使用 Docker
docker-compose -f docker-compose.prod.yml up -d --build
```

### 步骤3: 验证部署

```bash
# 查看容器状态
podman-compose -f docker-compose.prod.yml ps

# 查看日志
podman-compose -f docker-compose.prod.yml logs -f
```

---

## 🌐 访问系统

部署成功后，可以访问以下服务：

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端应用** | http://localhost | Vue 3 应用 |
| **后端API** | http://localhost:8080/api | Spring Boot API |
| **API文档** | http://localhost:8080/api/doc.html | Knife4j 文档 |
| **数据库管理** | http://localhost:8081 | phpMyAdmin |
| **Druid监控** | http://localhost:8080/druid | 数据库连接池监控 |

**生产环境请替换 `localhost` 为实际服务器IP或域名**

---

## 🗄️ 数据库说明

### 自动初始化

MySQL 容器启动时会自动执行 `database/init_database_full.sql`，创建完整的19张表：

```
✅ 认证新闻数据表（1张）
✅ 医疗设备数据表（6张）
✅ 关键词管理表（2张）
✅ 任务配置和日志表（5张）
✅ AI判断任务表（1张）
✅ 统计分析表（1张）
✅ 基础数据表（1张）
✅ 爬虫状态管理表（3张）
```

### 数据库连接信息

**从应用内部访问**（容器间通信）：
```
主机: mysql
端口: 3306
数据库: common_db
用户名: cert_user
密码: 查看 .env.prod 中的 MYSQL_PASSWORD
```

**从外部访问**（宿主机）：
```
主机: localhost 或 服务器IP
端口: 3306
数据库: common_db
用户名: root 或 cert_user
密码: 查看 .env.prod 中的密码
```

### 数据持久化

数据库数据存储在 Docker Volume 中，即使删除容器数据也不会丢失：

```bash
# 查看数据卷
podman volume ls | grep mysql_data

# 备份数据卷
podman run --rm -v mysql_data:/source -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz -C /source .

# 恢复数据卷
podman run --rm -v mysql_data:/target -v $(pwd):/backup alpine tar xzf /backup/mysql_backup.tar.gz -C /target
```

---

## 🔧 常用命令

### 查看服务状态

```bash
# 查看所有容器
podman-compose -f docker-compose.prod.yml ps

# 查看详细状态
podman ps -a
```

### 查看日志

```bash
# 所有服务日志
podman-compose -f docker-compose.prod.yml logs -f

# 单个服务日志
podman-compose -f docker-compose.prod.yml logs -f backend
podman-compose -f docker-compose.prod.yml logs -f mysql
podman-compose -f docker-compose.prod.yml logs -f frontend
```

### 重启服务

```bash
# 重启所有服务
podman-compose -f docker-compose.prod.yml restart

# 重启单个服务
podman-compose -f docker-compose.prod.yml restart backend
```

### 停止服务

```bash
# 停止所有服务（保留数据）
podman-compose -f docker-compose.prod.yml stop

# 停止并删除容器（保留数据卷）
podman-compose -f docker-compose.prod.yml down

# 停止并删除所有（包括数据，慎用！）
podman-compose -f docker-compose.prod.yml down -v
```

### 进入容器

```bash
# 进入后端容器
podman exec -it cert_backend_prod sh

# 进入MySQL容器
podman exec -it cert_mysql_prod bash

# 进入前端容器
podman exec -it cert_frontend_prod sh
```

### 数据库操作

```bash
# 连接MySQL
podman exec -it cert_mysql_prod mysql -uroot -p

# 导出数据库
podman exec cert_mysql_prod mysqldump -uroot -p common_db > backup.sql

# 导入数据库
podman exec -i cert_mysql_prod mysql -uroot -p common_db < backup.sql
```

---

## 📊 系统监控

### 健康检查

```bash
# 检查容器健康状态
podman inspect cert_mysql_prod | grep -A 10 Health
podman inspect cert_backend_prod | grep -A 10 Health
```

### 资源使用

```bash
# 查看资源使用情况
podman stats

# 查看特定容器资源
podman stats cert_backend_prod cert_mysql_prod
```

### 磁盘空间

```bash
# 查看数据卷大小
podman system df -v

# 清理未使用的资源
podman system prune
```

---

## 🛡️ 安全建议

### 1. 修改默认密码

编辑 `.env.prod`，设置强密码：
```bash
# 使用强密码（至少16位，包含大小写字母、数字、特殊字符）
MYSQL_ROOT_PASSWORD=MyStr0ng!P@ssw0rd#2024$ROOT
MYSQL_PASSWORD=MyStr0ng!P@ssw0rd#2024$USER
REDIS_PASSWORD=MyStr0ng!P@ssw0rd#2024$REDIS
DRUID_PASSWORD=MyStr0ng!P@ssw0rd#2024$DRUID
```

### 2. 配置防火墙

```bash
# Linux 使用 firewalld
firewall-cmd --permanent --add-port=80/tcp    # 前端
firewall-cmd --permanent --add-port=8080/tcp  # 后端
firewall-cmd --reload

# 或使用 ufw
ufw allow 80/tcp
ufw allow 8080/tcp
ufw enable
```

### 3. 限制数据库访问

仅允许容器内部访问数据库，修改 `docker-compose.prod.yml`：

```yaml
mysql:
  ports:
    - "127.0.0.1:3306:3306"  # 仅本地访问
```

### 4. 关闭 Swagger

生产环境关闭 API 文档：
```bash
SWAGGER_ENABLED=false
```

### 5. 配置 SSL（可选）

使用 Nginx 配置 HTTPS：
```bash
# 启用 nginx profile
podman-compose --profile ssl -f docker-compose.prod.yml up -d
```

---

## 🔄 更新部署

### 更新代码后重新部署

```bash
# 1. 停止服务
podman-compose -f docker-compose.prod.yml stop

# 2. 重新构建
podman-compose -f docker-compose.prod.yml build --no-cache

# 3. 启动服务
podman-compose -f docker-compose.prod.yml up -d

# 4. 查看日志验证
podman-compose -f docker-compose.prod.yml logs -f backend
```

### 仅更新环境配置

```bash
# 1. 编辑 .env.prod
vim .env.prod

# 2. 重启相关服务
podman-compose -f docker-compose.prod.yml restart backend
```

---

## ❓ 常见问题

### Q1: 端口冲突

**问题**：端口 3306、8080、80 已被占用

**解决**：修改 `docker-compose.prod.yml` 中的端口映射
```yaml
ports:
  - "13306:3306"  # MySQL
  - "18080:8080"  # 后端
  - "8000:80"     # 前端
```

### Q2: 数据库初始化失败

**问题**：容器启动但表未创建

**解决**：
```bash
# 1. 检查初始化脚本
ls -la database/init_database_full.sql

# 2. 手动初始化
podman exec -i cert_mysql_prod mysql -uroot -p common_db < database/init_database_full.sql

# 3. 验证表是否创建
podman exec cert_mysql_prod mysql -uroot -p -e "USE common_db; SHOW TABLES;"
```

### Q3: 容器启动失败

**问题**：容器无法启动

**解决**：
```bash
# 查看详细日志
podman-compose -f docker-compose.prod.yml logs backend

# 检查配置文件
cat .env.prod

# 检查端口占用
netstat -tuln | grep -E '80|8080|3306'
```

### Q4: 前端无法连接后端

**问题**：API 请求失败

**解决**：
```bash
# 检查 CORS 配置
grep CORS_ALLOWED_ORIGINS .env.prod

# 应该包含前端访问地址
CORS_ALLOWED_ORIGINS=http://localhost,http://your-server-ip
```

### Q5: 内存不足

**问题**：系统响应慢或容器重启

**解决**：调整 JVM 内存，编辑 `.env.prod`
```bash
# 减少内存使用
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
```

---

## 📞 技术支持

遇到问题请：

1. 查看日志：`podman-compose logs -f`
2. 检查容器状态：`podman-compose ps`
3. 验证配置文件：`cat .env.prod`
4. 查阅相关文档：
   - [README.md](./README.md)
   - [SYSTEM_MAINTENANCE_GUIDE.md](./SYSTEM_MAINTENANCE_GUIDE.md)
   - [PODMAN_DEPLOYMENT_GUIDE.md](./PODMAN_DEPLOYMENT_GUIDE.md)
   - [database/README.md](./database/README.md)

---

**版本**: v2.0.0
**更新日期**: 2025-01-20
**维护者**: 开发团队
