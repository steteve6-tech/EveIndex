# Zeabur前后端连接配置指南

## 🏗️ 架构概述

```
Zeabur项目
├── 前端服务 (Vue.js + Nginx)
│   ├── 端口: 80
│   ├── 域名: https://your-frontend.zeabur.app
│   └── 代理API请求到后端
├── 后端服务 (Spring Boot)
│   ├── 端口: 8080
│   ├── 域名: https://your-backend.zeabur.app
│   └── 连接数据库和缓存
├── MySQL数据库
└── Redis缓存
```

## 📋 部署步骤

### 第一步：创建服务

在Zeabur Dashboard中创建以下服务：

#### 1. 后端服务 (Spring Boot)
- **服务名称**: `certification-backend`
- **构建路径**: `./spring-boot-backend`
- **Dockerfile**: `Dockerfile.zeabur`
- **端口**: `8080`

#### 2. 前端服务 (Vue.js)
- **服务名称**: `certification-frontend`
- **构建路径**: `./vue-frontend`
- **Dockerfile**: `Dockerfile.zeabur`
- **端口**: `80`

#### 3. 数据库服务
- **MySQL**: 用于数据存储
- **Redis**: 用于缓存

### 第二步：配置环境变量

#### 后端服务环境变量：
```bash
SPRING_PROFILES_ACTIVE=zeabur
SPRING_DATASOURCE_URL=${MYSQL_URL}
SPRING_DATASOURCE_USERNAME=${MYSQL_USERNAME}
SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
SPRING_DATA_REDIS_HOST=${REDIS_HOST}
SPRING_DATA_REDIS_PORT=${REDIS_PORT}
VOLCENGINE_ACCESS_KEY=${VOLCENGINE_ACCESS_KEY}
VOLCENGINE_SECRET_KEY=${VOLCENGINE_SECRET_KEY}
ARK_API_KEY=${ARK_API_KEY}
```

#### 前端服务环境变量：
```bash
VITE_API_BASE_URL=https://your-backend-domain.zeabur.app/api
BACKEND_URL=https://your-backend-domain.zeabur.app
```

### 第三步：设置服务依赖

在Zeabur Dashboard中设置服务依赖关系：
- 前端服务依赖后端服务
- 后端服务依赖MySQL和Redis

### 第四步：部署顺序

1. **先部署数据库服务** (MySQL, Redis)
2. **再部署后端服务** (Spring Boot)
3. **最后部署前端服务** (Vue.js)

## 🔧 连接配置详解

### 1. 前端配置

#### Vite配置 (`vite.config.ts`)
```typescript
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true,
        secure: true
      }
    }
  }
})
```

#### 请求配置 (`src/request.ts`)
```typescript
const request = axios.create({
  baseURL: '', // 使用相对路径，让Nginx代理处理
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})
```

### 2. Nginx配置

#### 代理配置 (`nginx.zeabur.conf`)
```nginx
# API代理到后端服务
location /api/ {
    # 设置代理到后端服务
    proxy_pass ${BACKEND_URL}/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # 超时设置
    proxy_connect_timeout 30s;
    proxy_send_timeout 30s;
    proxy_read_timeout 30s;
    
    # 错误处理：如果后端不可用，返回临时响应
    error_page 502 503 504 = @fallback_api;
}

# 后端不可用时的备用响应
location @fallback_api {
    add_header Content-Type application/json always;
    add_header Access-Control-Allow-Origin * always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Content-Type, Authorization" always;
    
    return 200 '{"message": "Backend service is temporarily unavailable", "status": "maintenance", "timestamp": "$time_iso8601", "data": [], "total": 0, "page": 1, "size": 10}';
}
```

### 3. 后端配置

#### Spring Boot配置 (`application-zeabur.yml`)
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

server:
  port: 8080
  servlet:
    context-path: /api
```

## 🔍 连接测试

### 1. 健康检查

#### 后端健康检查
```bash
curl https://your-backend-domain.zeabur.app/api/health
```

#### 前端健康检查
```bash
curl https://your-frontend-domain.zeabur.app/health
```

### 2. API测试

#### 测试API连接
```bash
curl https://your-frontend-domain.zeabur.app/api/device-data/overview-statistics
```

### 3. 浏览器测试

访问测试页面：
```
https://your-frontend-domain.zeabur.app/test-api.html
```

## 🚨 故障排除

### 常见问题

#### 1. 502 Bad Gateway
- **原因**: 后端服务未启动或不可访问
- **解决**: 检查后端服务状态，确保健康检查通过

#### 2. CORS错误
- **原因**: 跨域请求被阻止
- **解决**: 检查Nginx CORS配置，确保包含正确的头信息

#### 3. 连接超时
- **原因**: 网络延迟或服务响应慢
- **解决**: 增加Nginx代理超时时间

#### 4. 环境变量未生效
- **原因**: 环境变量配置错误
- **解决**: 检查Zeabur Dashboard中的环境变量设置

### 调试步骤

1. **检查服务状态**
   ```bash
   # 在Zeabur Dashboard中查看服务日志
   ```

2. **测试网络连接**
   ```bash
   curl -I https://your-backend-domain.zeabur.app/api/health
   ```

3. **检查Nginx配置**
   ```bash
   # 查看Nginx错误日志
   docker logs your-frontend-container
   ```

4. **验证环境变量**
   ```bash
   # 在服务中检查环境变量
   echo $BACKEND_URL
   ```

## 📊 监控和维护

### 1. 服务监控
- 使用Zeabur Dashboard监控服务状态
- 设置健康检查告警
- 监控资源使用情况

### 2. 日志管理
- 定期查看服务日志
- 设置日志轮转
- 监控错误日志

### 3. 性能优化
- 调整Nginx缓存设置
- 优化数据库连接池
- 监控API响应时间

## 🔄 更新部署

### 1. 代码更新
1. 推送代码到GitHub
2. 在Zeabur Dashboard中触发重新部署
3. 等待部署完成

### 2. 配置更新
1. 修改环境变量
2. 更新Dockerfile
3. 重新部署服务

### 3. 数据库迁移
1. 备份现有数据
2. 执行数据库迁移脚本
3. 验证数据完整性

## 📝 最佳实践

1. **服务分离**: 前后端独立部署，便于扩展和维护
2. **环境隔离**: 使用不同的环境变量配置
3. **健康检查**: 设置完整的健康检查机制
4. **错误处理**: 实现优雅的错误处理和降级策略
5. **监控告警**: 设置完整的监控和告警系统
6. **安全配置**: 使用HTTPS，配置安全头信息
7. **缓存策略**: 合理使用缓存提高性能
8. **日志记录**: 完整的日志记录和审计

## 🎯 总结

通过以上配置，您可以在Zeabur上成功部署前后端分离的应用，实现：

- ✅ 前后端独立部署和扩展
- ✅ 自动服务发现和负载均衡
- ✅ 完整的错误处理和降级策略
- ✅ 健康检查和监控
- ✅ 安全的服务间通信
- ✅ 灵活的配置管理

这种架构具有高可用性、可扩展性和可维护性，适合生产环境使用。
