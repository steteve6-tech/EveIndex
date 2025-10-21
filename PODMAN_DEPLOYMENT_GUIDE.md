# Podman Desktop 部署指南

## 目录
- [前置准备](#前置准备)
- [环境配置](#环境配置)
- [使用 Podman Desktop 构建镜像](#使用-podman-desktop-构建镜像)
- [部署到服务器](#部署到服务器)
- [验证部署](#验证部署)
- [常见问题](#常见问题)

---

## 前置准备

### 1. 安装 Podman Desktop

1. 访问 [Podman Desktop 官网](https://podman-desktop.io/) 下载并安装
2. 启动 Podman Desktop 并确保 Podman 引擎正常运行
3. 打开 Podman Desktop，检查状态指示器显示为绿色

### 2. 验证 Podman 安装

在 Windows PowerShell 或 CMD 中运行：

```bash
podman --version
podman-compose --version
```

如果 `podman-compose` 未安装，请运行：

```bash
pip install podman-compose
```

---

## 环境配置

### 1. 创建生产环境配置文件

在项目根目录下，复制环境配置模板并填写真实配置：

```bash
# 复制模板
cp .env.prod.example .env.prod

# 编辑配置文件
notepad .env.prod
```

**关键配置项**（必须修改）：

```bash
# 数据库密码
MYSQL_ROOT_PASSWORD=your_strong_root_password_here
MYSQL_PASSWORD=your_strong_db_password_here

# Redis 密码
REDIS_PASSWORD=your_strong_redis_password_here

# AI 服务密钥（如需使用 AI 功能）
OPENAI_API_KEY=your_openai_api_key_here
VOLCENGINE_ACCESS_KEY=your_volcengine_access_key_here
VOLCENGINE_SECRET_KEY=your_volcengine_secret_key_here

# CORS 配置（改为实际域名）
CORS_ALLOWED_ORIGINS=https://yourdomain.com,http://your-server-ip

# Druid 监控密码
DRUID_PASSWORD=your_strong_druid_password_here
```

### 2. 准备 Tessdata 数据文件

确保 OCR 识别数据文件存在：

```bash
# 检查文件是否存在
dir spring-boot-backend\src\main\resources\tessdata\eng.traineddata
```

如果不存在，需要下载：
- 下载地址：https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
- 放置路径：`spring-boot-backend\src\main\resources\tessdata\eng.traineddata`

---

## 使用 Podman Desktop 构建镜像

### 方式一：使用 Podman Desktop 图形界面

#### 1. 构建后端镜像

1. 打开 Podman Desktop
2. 点击左侧 **Images**（镜像）
3. 点击 **Build** 按钮
4. 选择 Dockerfile：`spring-boot-backend/Dockerfile`
5. 设置镜像名称：`cert-backend:latest`
6. 设置构建上下文：`spring-boot-backend/`
7. 点击 **Build** 开始构建

#### 2. 构建前端镜像

1. 重复上述步骤
2. 选择 Dockerfile：`vue-frontend/Dockerfile`
3. 设置镜像名称：`cert-frontend:latest`
4. 设置构建上下文：`vue-frontend/`
5. 点击 **Build** 开始构建

#### 3. 使用 Compose 启动所有服务

1. 点击左侧 **Compose**
2. 点击 **Create** 按钮
3. 选择 compose 文件：`docker-compose.prod.yml`
4. 点击 **Start** 启动所有服务

### 方式二：使用命令行（推荐）

在项目根目录打开 PowerShell 或 CMD：

```bash
# 1. 加载环境变量
# PowerShell:
Get-Content .env.prod | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}

# CMD (需要手动设置):
# set MYSQL_ROOT_PASSWORD=your_password
# set MYSQL_PASSWORD=your_password
# ... (设置其他变量)

# 2. 使用 podman-compose 构建并启动
podman-compose -f docker-compose.prod.yml up -d --build

# 查看日志
podman-compose -f docker-compose.prod.yml logs -f

# 查看运行状态
podman-compose -f docker-compose.prod.yml ps
```

---

## 部署到服务器

### 方法一：导出镜像到服务器

#### 1. 在本地导出镜像

```bash
# 导出后端镜像
podman save -o cert-backend.tar localhost/cert-backend:latest

# 导出前端镜像
podman save -o cert-frontend.tar localhost/cert-frontend:latest
```

#### 2. 传输到服务器

使用 SCP 或其他工具传输：

```bash
scp cert-backend.tar user@your-server:/path/to/upload/
scp cert-frontend.tar user@your-server:/path/to/upload/
scp docker-compose.prod.yml user@your-server:/path/to/deploy/
scp .env.prod user@your-server:/path/to/deploy/
```

#### 3. 在服务器上导入并启动

SSH 登录服务器后：

```bash
# 导入镜像
podman load -i cert-backend.tar
podman load -i cert-frontend.tar

# 启动服务
cd /path/to/deploy/
podman-compose -f docker-compose.prod.yml up -d
```

### 方法二：使用镜像仓库（推荐）

#### 1. 推送到 Docker Hub 或私有仓库

```bash
# 登录 Docker Hub
podman login docker.io

# 标记镜像
podman tag localhost/cert-backend:latest yourusername/cert-backend:latest
podman tag localhost/cert-frontend:latest yourusername/cert-frontend:latest

# 推送镜像
podman push yourusername/cert-backend:latest
podman push yourusername/cert-frontend:latest
```

#### 2. 修改 docker-compose.prod.yml

将 `build` 配置改为 `image`：

```yaml
services:
  backend:
    # build:
    #   context: ./spring-boot-backend
    #   dockerfile: Dockerfile
    image: yourusername/cert-backend:latest
    # ... 其他配置不变

  frontend:
    # build:
    #   context: ./vue-frontend
    #   dockerfile: Dockerfile
    image: yourusername/cert-frontend:latest
    # ... 其他配置不变
```

#### 3. 在服务器上部署

```bash
# 拉取镜像并启动
podman-compose -f docker-compose.prod.yml pull
podman-compose -f docker-compose.prod.yml up -d
```

---

## 验证部署

### 1. 检查容器状态

```bash
# 查看运行中的容器
podman ps

# 查看所有容器（包括停止的）
podman ps -a

# 使用 compose 查看
podman-compose -f docker-compose.prod.yml ps
```

### 2. 检查服务健康状态

```bash
# 查看容器健康检查
podman inspect cert_backend_prod | findstr Health
podman inspect cert_mysql_prod | findstr Health
```

### 3. 测试服务访问

在浏览器或使用 curl：

```bash
# 前端
curl http://your-server-ip/

# 后端健康检查
curl http://your-server-ip:8080/api/health

# 后端 API 文档（如果启用）
curl http://your-server-ip:8080/api/doc.html

# phpMyAdmin 数据库管理
# 浏览器访问：http://your-server-ip:8081
```

### 4. 查看日志

```bash
# 查看所有服务日志
podman-compose -f docker-compose.prod.yml logs -f

# 查看特定服务日志
podman logs -f cert_backend_prod
podman logs -f cert_frontend_prod
podman logs -f cert_mysql_prod
```

---

## 常见问题

### 1. Podman 与 Docker 的区别

- Podman 无需守护进程，更安全
- 命令几乎完全兼容：`docker` 命令改为 `podman`
- `docker-compose` 改为 `podman-compose`

### 2. 构建失败

**问题**：Maven 依赖下载超时

**解决**：
```bash
# 在 spring-boot-backend/Dockerfile 中添加国内镜像源
# 修改 RUN mvn dependency:go-offline -B 为：
RUN mvn dependency:go-offline -B -DmirrorId=aliyun -Dmaven.repo.mirror=http://maven.aliyun.com/nexus/content/groups/public/
```

**问题**：npm install 失败

**解决**：
```bash
# 在 vue-frontend/Dockerfile 中添加淘宝镜像
RUN npm config set registry https://registry.npmmirror.com
RUN npm ci
```

### 3. 容器启动失败

```bash
# 查看详细错误信息
podman logs cert_backend_prod --tail 100

# 检查端口占用
netstat -ano | findstr 8080

# 重启容器
podman restart cert_backend_prod
```

### 4. 数据库连接失败

**检查**：
1. MySQL 容器是否启动：`podman ps | findstr mysql`
2. 网络是否正常：`podman network inspect cert_network`
3. 环境变量是否正确：检查 `.env.prod` 文件

**解决**：
```bash
# 进入后端容器检查
podman exec -it cert_backend_prod sh
# 测试数据库连接
ping mysql
```

### 5. 前端无法访问后端 API

**检查 nginx 配置**：
```bash
# 查看前端容器日志
podman logs cert_frontend_prod

# 进入容器检查 nginx 配置
podman exec -it cert_frontend_prod sh
cat /etc/nginx/conf.d/default.conf
```

### 6. 修改配置后重新部署

```bash
# 停止并删除容器
podman-compose -f docker-compose.prod.yml down

# 重新构建并启动（如果修改了代码）
podman-compose -f docker-compose.prod.yml up -d --build

# 仅重启（如果只修改了环境变量）
podman-compose -f docker-compose.prod.yml up -d
```

### 7. 数据备份

```bash
# 备份 MySQL 数据
podman exec cert_mysql_prod mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} common_db > backup.sql

# 备份数据卷
podman volume export docker-compose_mysql_data > mysql_data_backup.tar
```

### 8. 清理资源

```bash
# 停止并删除所有容器
podman-compose -f docker-compose.prod.yml down

# 删除未使用的镜像
podman image prune -a

# 删除未使用的数据卷（谨慎使用！）
podman volume prune
```

---

## 生产环境优化建议

### 1. 资源限制

在 `docker-compose.prod.yml` 中添加资源限制：

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### 2. 日志轮转

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 3. 安全配置

- 修改所有默认密码
- 关闭 Swagger API 文档（`SWAGGER_ENABLED=false`）
- 配置防火墙规则
- 使用 HTTPS（配置 SSL 证书）
- 限制数据库和 phpMyAdmin 的访问 IP

### 4. 监控和告警

- 配置邮件通知
- 配置 Slack 告警（可选）
- 使用 Prometheus + Grafana 监控（可选）

---

## 快速命令参考

```bash
# 构建镜像
podman-compose -f docker-compose.prod.yml build

# 启动服务
podman-compose -f docker-compose.prod.yml up -d

# 停止服务
podman-compose -f docker-compose.prod.yml stop

# 停止并删除容器
podman-compose -f docker-compose.prod.yml down

# 查看日志
podman-compose -f docker-compose.prod.yml logs -f [service_name]

# 重启服务
podman-compose -f docker-compose.prod.yml restart [service_name]

# 进入容器
podman exec -it cert_backend_prod sh

# 查看容器资源使用
podman stats

# 查看网络
podman network ls
podman network inspect cert_network
```

---

## 支持

如有问题，请检查：
1. 日志文件：`podman logs [container_name]`
2. 服务状态：`podman ps -a`
3. 网络连接：`podman network inspect cert_network`

更多信息请参考：
- [Podman 官方文档](https://docs.podman.io/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
