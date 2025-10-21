# ✅ 部署检查清单

## 📋 部署前检查

### 系统要求
- [ ] **操作系统**: Windows 10+ / Linux / macOS
- [ ] **内存**: 至少 4GB 可用内存
- [ ] **磁盘**: 至少 10GB 可用空间
- [ ] **网络**: 可以访问互联网（下载镜像）

### 软件安装
- [ ] **Podman Desktop** 或 **Docker Desktop** 已安装并运行
- [ ] **podman-compose** 或 **docker-compose** 命令可用
- [ ] Git 已安装（如需从仓库克隆）

### 端口检查
- [ ] 端口 **80** 未被占用（前端）
- [ ] 端口 **8080** 未被占用（后端）
- [ ] 端口 **3306** 未被占用（MySQL）
- [ ] 端口 **6379** 未被占用（Redis）
- [ ] 端口 **8081** 未被占用（phpMyAdmin）

**检查方法**：
```bash
# Windows
netstat -ano | findstr "80 8080 3306 6379 8081"

# Linux/Mac
netstat -tuln | grep -E '80|8080|3306|6379|8081'
```

---

## 🔧 配置检查

### 环境配置文件
- [ ] `.env.prod` 文件已创建（从 `.env.prod.minimal` 复制）
- [ ] `MYSQL_ROOT_PASSWORD` 已设置强密码
- [ ] `MYSQL_PASSWORD` 已设置强密码
- [ ] `REDIS_PASSWORD` 已设置强密码
- [ ] `DRUID_PASSWORD` 已设置强密码
- [ ] `CORS_ALLOWED_ORIGINS` 已修改为实际服务器IP/域名
- [ ] `SWAGGER_ENABLED` 生产环境设置为 `false`

### 数据库初始化
- [ ] `database/init_database_full.sql` 文件存在
- [ ] SQL 文件包含 19张表的创建语句
- [ ] SQL 文件包含初始化数据（关键词、国家等）

### Docker Compose 配置
- [ ] `docker-compose.prod.yml` 文件存在
- [ ] MySQL 服务配置正确
- [ ] 后端服务配置正确
- [ ] 前端服务配置正确
- [ ] 数据卷配置正确

---

## 🚀 部署执行

### 方式一：一键部署脚本
- [ ] 运行 `deploy-quick-start.bat`（Windows）
- [ ] 或运行 `deploy-quick-start.sh`（Linux/Mac）
- [ ] 脚本执行无错误
- [ ] 所有步骤显示 ✓

### 方式二：手动部署
- [ ] 执行 `cp .env.prod.minimal .env.prod`
- [ ] 编辑 `.env.prod` 文件
- [ ] 执行 `podman-compose -f docker-compose.prod.yml build`
- [ ] 执行 `podman-compose -f docker-compose.prod.yml up -d`
- [ ] 所有容器启动成功

---

## 🔍 部署验证

### 容器状态检查
- [ ] 执行 `podman-compose ps`，所有容器状态为 `Up`
- [ ] `cert_mysql_prod` 容器运行中
- [ ] `cert_redis_prod` 容器运行中
- [ ] `cert_backend_prod` 容器运行中
- [ ] `cert_frontend_prod` 容器运行中
- [ ] `cert_phpmyadmin_prod` 容器运行中

### 健康检查
```bash
# 检查容器健康状态
podman inspect cert_mysql_prod | grep -A 5 Health
podman inspect cert_backend_prod | grep -A 5 Health
```

- [ ] MySQL 健康检查通过
- [ ] 后端健康检查通过

### 日志检查
```bash
# 查看后端日志
podman logs cert_backend_prod --tail 50
```

- [ ] 后端启动无错误
- [ ] 数据库连接成功
- [ ] Redis 连接成功
- [ ] 无 Exception 或 ERROR

### 数据库验证
```bash
# 连接数据库
podman exec -it cert_mysql_prod mysql -uroot -p

# 执行 SQL
USE common_db;
SHOW TABLES;
```

- [ ] 数据库 `common_db` 存在
- [ ] 显示 19张表
- [ ] 表中有初始数据（查询 `t_keyword` 等）

---

## 🌐 访问测试

### 前端访问
- [ ] 浏览器打开 `http://localhost`
- [ ] 页面加载成功
- [ ] 界面显示正常
- [ ] 无 Console 错误

### 后端API访问
- [ ] 浏览器打开 `http://localhost:8080/api/health`
- [ ] 返回健康状态（JSON）
- [ ] 状态码 200

### API文档访问
- [ ] 浏览器打开 `http://localhost:8080/api/doc.html`
- [ ] Knife4j 文档加载成功
- [ ] 可以看到所有 API 接口

### 数据库管理访问
- [ ] 浏览器打开 `http://localhost:8081`
- [ ] phpMyAdmin 登录页面显示
- [ ] 使用 `root` 和密码登录成功
- [ ] 可以看到 `common_db` 数据库

### Druid监控访问
- [ ] 浏览器打开 `http://localhost:8080/druid`
- [ ] 登录页面显示
- [ ] 使用 `admin` 和密码登录成功
- [ ] 可以看到数据源监控信息

---

## 🧪 功能测试

### 基础功能
- [ ] 前端可以连接后端API
- [ ] 数据列表可以正常加载
- [ ] 可以进行数据查询
- [ ] 翻页功能正常

### 爬虫功能
- [ ] 可以查看爬虫列表
- [ ] 可以手动执行爬虫任务
- [ ] 任务日志正常记录

### 数据管理
- [ ] 可以查看设备数据
- [ ] 可以筛选数据
- [ ] 可以导出数据

### AI功能（如已配置）
- [ ] AI判断功能可用
- [ ] 可以查看判断结果

---

## 🔐 安全检查

### 密码安全
- [ ] 所有默认密码已修改
- [ ] 密码强度足够（至少16位）
- [ ] `.env.prod` 文件未提交到 Git

### 访问控制
- [ ] 生产环境 Swagger 已关闭
- [ ] Druid 监控配置了用户名密码
- [ ] phpMyAdmin 不对外网开放（或配置白名单）

### 网络安全
- [ ] 防火墙规则已配置
- [ ] 仅开放必要端口（80, 8080）
- [ ] 数据库端口不对外开放

---

## 📊 性能检查

### 资源使用
```bash
# 查看容器资源使用
podman stats
```

- [ ] 内存使用 < 80%
- [ ] CPU 使用正常
- [ ] 磁盘 I/O 正常

### 响应时间
- [ ] 前端首次加载 < 3秒
- [ ] API 响应时间 < 1秒
- [ ] 数据查询响应正常

---

## 📝 文档检查

### 部署文档
- [ ] `DEPLOYMENT_README.md` 已阅读
- [ ] `PODMAN_DEPLOYMENT_GUIDE.md` 已阅读
- [ ] `database/README.md` 已阅读

### 运维准备
- [ ] 已了解常用命令
- [ ] 已了解日志查看方法
- [ ] 已了解备份恢复方法
- [ ] 已了解故障排查流程

---

## 🔄 备份准备

### 数据备份
- [ ] 配置定期数据库备份
- [ ] 配置数据卷备份
- [ ] 测试备份恢复流程

**备份命令**：
```bash
# 数据库备份
podman exec cert_mysql_prod mysqldump -uroot -p common_db > backup_$(date +%Y%m%d).sql

# 数据卷备份
podman volume export mysql_data > mysql_data_backup.tar
```

---

## 📞 监控和告警

### 监控配置（可选）
- [ ] 配置邮件通知
- [ ] 配置 Slack 通知
- [ ] 配置日志监控
- [ ] 配置资源监控

### 告警测试
- [ ] 测试邮件通知
- [ ] 测试 Slack 通知
- [ ] 测试错误告警

---

## ✅ 最终验证

### 完整流程测试
1. [ ] 重启所有服务
   ```bash
   podman-compose -f docker-compose.prod.yml restart
   ```

2. [ ] 等待服务启动（1-2分钟）

3. [ ] 验证所有访问地址可用

4. [ ] 执行一次完整的业务流程

5. [ ] 检查日志无错误

### 生产环境切换
- [ ] 域名已配置
- [ ] DNS 已解析
- [ ] SSL 证书已配置（如使用 HTTPS）
- [ ] 修改 `CORS_ALLOWED_ORIGINS` 为实际域名

---

## 🎯 部署后24小时观察

### 第1小时
- [ ] 监控容器状态
- [ ] 查看错误日志
- [ ] 检查资源使用

### 第6小时
- [ ] 检查数据库连接
- [ ] 检查爬虫任务执行
- [ ] 查看系统日志

### 第24小时
- [ ] 备份数据库
- [ ] 检查磁盘空间
- [ ] 分析性能指标
- [ ] 记录问题和优化点

---

## 📋 问题记录

### 部署过程中遇到的问题
```
问题1: _______________________________
解决: _______________________________

问题2: _______________________________
解决: _______________________________

问题3: _______________________________
解决: _______________________________
```

### 优化建议
```
1. _______________________________
2. _______________________________
3. _______________________________
```

---

## ✍️ 签署确认

- **部署人员**: ___________________
- **部署日期**: ___________________
- **服务器信息**: ___________________
- **部署版本**: v2.0.0
- **检查结果**: ☐ 通过  ☐ 有问题（见问题记录）

---

**检查清单版本**: v2.0.0
**最后更新**: 2025-01-20
