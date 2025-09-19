# Docker部署说明

## 概述

使用Docker Compose可以快速部署认证监控系统所需的MySQL数据库和Redis缓存服务。

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+

## 快速部署

### 1. 启动服务

```bash
# 在项目根目录执行
docker-compose up -d
```

这将启动以下服务：
- MySQL 8.0 数据库 (端口: 3306)
- Redis 7 缓存 (端口: 6379)
- phpMyAdmin 管理界面 (端口: 8081)

### 2. 验证服务状态

```bash
# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs mysql
docker-compose logs redis
```

### 3. 访问管理界面

- phpMyAdmin: http://localhost:8081
  - 用户名: root
  - 密码: password

## 数据库连接配置

使用Docker部署后，应用的数据库配置应设置为：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/certification_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
```

## 数据持久化

Docker Compose配置中已设置数据持久化：

- MySQL数据: `mysql_data` 卷
- Redis数据: `redis_data` 卷

数据存储在Docker卷中，容器重启后数据不会丢失。

## 常用操作

### 启动服务
```bash
docker-compose up -d
```

### 停止服务
```bash
docker-compose down
```

### 重启服务
```bash
docker-compose restart
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs mysql
docker-compose logs redis
```

### 进入容器
```bash
# 进入MySQL容器
docker-compose exec mysql mysql -u root -p

# 进入Redis容器
docker-compose exec redis redis-cli
```

### 备份数据
```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -u root -ppassword certification_db > backup.sql

# 备份Redis数据
docker-compose exec redis redis-cli BGSAVE
```

### 恢复数据
```bash
# 恢复MySQL数据
docker-compose exec -T mysql mysql -u root -ppassword certification_db < backup.sql
```

## 环境变量配置

可以通过环境变量自定义配置：

```bash
# 设置环境变量
export MYSQL_ROOT_PASSWORD=your_password
export MYSQL_DATABASE=your_database
export MYSQL_USER=your_user
export MYSQL_PASSWORD=your_password

# 启动服务
docker-compose up -d
```

## 生产环境配置

### 1. 修改默认密码

```bash
# 修改MySQL root密码
docker-compose exec mysql mysql -u root -ppassword
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### 2. 创建专用用户

```sql
-- 在MySQL中创建专用用户
CREATE USER 'certification_user'@'%' IDENTIFIED BY 'certification_password';
GRANT ALL PRIVILEGES ON certification_db.* TO 'certification_user'@'%';
FLUSH PRIVILEGES;
```

### 3. 配置Redis密码

```bash
# 修改docker-compose.yml中的Redis配置
redis:
  command: redis-server --requirepass your_redis_password --appendonly yes
```

### 4. 网络安全

```yaml
# 修改docker-compose.yml，移除不必要的端口映射
services:
  mysql:
    ports:
      - "127.0.0.1:3306:3306"  # 只允许本地访问
  
  redis:
    ports:
      - "127.0.0.1:6379:6379"  # 只允许本地访问
```

## 监控和维护

### 1. 查看资源使用情况

```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
docker system df
```

### 2. 清理资源

```bash
# 清理未使用的镜像和容器
docker system prune

# 清理所有数据（谨慎使用）
docker-compose down -v
```

### 3. 更新镜像

```bash
# 拉取最新镜像
docker-compose pull

# 重新启动服务
docker-compose up -d
```

## 故障排除

### 1. 端口冲突

如果端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # 使用3307端口
  
  redis:
    ports:
      - "6380:6379"  # 使用6380端口
```

### 2. 权限问题

```bash
# 修复数据目录权限
sudo chown -R 999:999 mysql_data/
sudo chown -R 999:999 redis_data/
```

### 3. 内存不足

```bash
# 增加Docker内存限制
# 在Docker Desktop设置中增加内存分配
```

### 4. 数据损坏

```bash
# 重新初始化数据库
docker-compose down -v
docker-compose up -d
```

## 性能优化

### 1. MySQL优化

```yaml
services:
  mysql:
    command: >
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --innodb-buffer-pool-size=1G
      --innodb-log-file-size=256M
      --max-connections=200
```

### 2. Redis优化

```yaml
services:
  redis:
    command: >
      redis-server
      --maxmemory 512mb
      --maxmemory-policy allkeys-lru
      --appendonly yes
```

## 扩展部署

### 1. 多实例部署

```yaml
services:
  mysql-master:
    image: mysql:8.0
    # 主库配置
  
  mysql-slave:
    image: mysql:8.0
    # 从库配置
  
  redis-master:
    image: redis:7-alpine
    # Redis主节点
  
  redis-slave:
    image: redis:7-alpine
    # Redis从节点
```

### 2. 负载均衡

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - mysql
      - redis
```






















