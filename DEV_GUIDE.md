# 开发环境使用指南

##  快速开始

### 1. 启动开发环境
`ash
# Windows PowerShell
docker-compose -f docker-compose.dev.yml up -d

# Linux/Mac
./dev.sh start
`

### 2. 访问地址
- **前端开发服务器**: http://localhost:3100 (支持热重载)
- **后端API**: http://localhost:8080/api
- **数据库管理**: http://localhost:8081 (phpMyAdmin)
  - 用户名: root
  - 密码: dev123

##  开发环境文件说明

### Docker配置文件
- .dockerignore - Docker构建忽略文件
- spring-boot-backend/Dockerfile.dev - 后端开发镜像
- ue-frontend/Dockerfile.dev - 前端开发镜像
- docker-compose.dev.yml - 开发环境编排文件

### 环境配置
- .env.dev - 开发环境变量
- dev.sh - 开发环境管理脚本

##  热重载功能

### 后端热重载
- **Spring DevTools**: 自动检测Java代码变化并重启
- **LiveReload**: 浏览器自动刷新（端口35729）
- **支持的变更**: Java类、配置文件、静态资源

### 前端热重载
- **Vite HMR**: 模块热替换，无需刷新页面
- **支持的变更**: Vue组件、TypeScript、CSS、配置文件
- **即时更新**: 修改代码后立即在浏览器中看到效果

##  开发工作流

### 1. 启动开发环境
`ash
docker-compose -f docker-compose.dev.yml up -d
`

### 2. 查看服务状态
`ash
docker-compose -f docker-compose.dev.yml ps
`

### 3. 查看日志
`ash
# 查看所有服务日志
docker-compose -f docker-compose.dev.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose.dev.yml logs -f backend-dev
docker-compose -f docker-compose.dev.yml logs -f frontend-dev
`

### 4. 重启服务
`ash
# 重启特定服务
docker-compose -f docker-compose.dev.yml restart backend-dev
docker-compose -f docker-compose.dev.yml restart frontend-dev
`

### 5. 停止开发环境
`ash
docker-compose -f docker-compose.dev.yml down
`

##  常见开发任务

### 添加新的依赖

#### 后端添加Maven依赖
1. 修改 spring-boot-backend/pom.xml
2. 重启后端容器：
   `ash
   docker-compose -f docker-compose.dev.yml restart backend-dev
   `

#### 前端添加NPM依赖
1. 进入前端容器：
   `ash
   docker exec -it cert_frontend_dev sh
   npm install package-name
   exit
   `
2. 或者重新构建前端镜像

### 数据库操作

#### 连接数据库
- **phpMyAdmin**: http://localhost:8081
- **直接连接**:
  `ash
  docker exec -it cert_mysql_dev mysql -u root -pdev123
  `

#### 初始化数据
将SQL文件放入 database/ 目录，重启MySQL容器会自动执行。

### 调试应用

#### 后端调试
1. 在IDE中配置远程调试
2. 连接到 localhost:5005 (如果开启了调试端口)

#### 前端调试
1. 浏览器开发者工具
2. Vue DevTools扩展
3. 源码映射支持

##  性能监控

### 查看资源使用
`ash
docker stats
`

### 查看容器日志大小
`ash
docker system df
`

##  故障排除

### 常见问题

#### 1. 端口被占用
`ash
# 检查端口占用
netstat -an | findstr :3100
netstat -an | findstr :8080

# 修改docker-compose.dev.yml中的端口映射
`

#### 2. 容器启动失败
`ash
# 查看详细日志
docker-compose -f docker-compose.dev.yml logs backend-dev
docker-compose -f docker-compose.dev.yml logs frontend-dev
`

#### 3. 热重载不工作
- 检查文件挂载是否正确
- 确保 CHOKIDAR_USEPOLLING=true 环境变量设置
- 重启相关容器

#### 4. 数据库连接失败
`ash
# 检查MySQL容器状态
docker-compose -f docker-compose.dev.yml ps mysql-dev

# 查看MySQL日志
docker-compose -f docker-compose.dev.yml logs mysql-dev
`

### 清理和重置

#### 清理开发环境
`ash
# 停止并删除容器、网络
docker-compose -f docker-compose.dev.yml down

# 删除数据卷（会丢失数据）
docker-compose -f docker-compose.dev.yml down -v

# 清理未使用的镜像
docker system prune -f
`

##  开发技巧

### 1. 使用别名简化命令
`ash
# 在 ~/.bashrc 或 ~/.zshrc 中添加
alias dev-start='docker-compose -f docker-compose.dev.yml up -d'
alias dev-stop='docker-compose -f docker-compose.dev.yml down'
alias dev-logs='docker-compose -f docker-compose.dev.yml logs -f'
`

### 2. 配置IDE
- **IntelliJ IDEA**: 配置Docker插件，直接在IDE中管理容器
- **VS Code**: 使用Docker扩展，方便查看容器状态

### 3. 数据持久化
开发环境使用Docker卷保存数据，容器重启不会丢失：
- MySQL数据: mysql_dev_data
- Redis数据: edis_dev_data
- Maven缓存: maven_cache
- Node模块: 
ode_modules_cache

##  从开发到生产

当开发完成后，可以使用生产环境配置部署：

`ash
# 停止开发环境
docker-compose -f docker-compose.dev.yml down

# 启动生产环境
docker-compose -f docker-compose.prod.yml up -d
`

注意：生产环境需要配置 .env.prod 文件中的真实密码和API密钥。
