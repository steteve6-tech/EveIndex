# 数据库迁移指南

## 概述
本指南将帮助你将本地 MySQL 数据库迁移到 Zeabur 上的 MySQL 服务。

## 前提条件
- 本地 MySQL 数据库运行正常
- 已创建本地数据库备份文件 `local_database_backup.sql`
- 已获得 Zeabur MySQL 连接信息

## Zeabur MySQL 连接信息
```
主机: sjc1.clusters.zeabur.com
端口: 32188
用户名: root
密码: lSA1WT05oPUMyb746xzQ8EcwBRY932aq
数据库: zeabur
```

## 迁移方法

### 方法一：使用 PowerShell 脚本（推荐）

1. **运行迁移脚本**：
   ```powershell
   .\migrate-to-zeabur.ps1
   ```

### 方法二：手动执行命令

1. **测试连接**：
   ```bash
   mysql -h sjc1.clusters.zeabur.com -P 32188 -u root -p"lSA1WT05oPUMyb746xzQ8EcwBRY932aq" -e "SELECT 1;"
   ```

2. **创建数据库**：
   ```bash
   mysql -h sjc1.clusters.zeabur.com -P 32188 -u root -p"lSA1WT05oPUMyb746xzQ8EcwBRY932aq" -e "CREATE DATABASE IF NOT EXISTS \`zeabur\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```

3. **导入数据**：
   ```bash
   mysql -h sjc1.clusters.zeabur.com -P 32188 -u root -p"lSA1WT05oPUMyb746xzQ8EcwBRY932aq" zeabur < local_database_backup.sql
   ```

### 方法三：使用 MySQL Workbench

1. **创建连接**：
   - 主机: `sjc1.clusters.zeabur.com`
   - 端口: `32188`
   - 用户名: `root`
   - 密码: `lSA1WT05oPUMyb746xzQ8EcwBRY932aq`

2. **导入数据**：
   - 打开 `local_database_backup.sql` 文件
   - 执行 SQL 脚本

## 验证迁移结果

### 检查表数量
```sql
SHOW TABLES;
```

### 检查数据量
```sql
SELECT 
    'device_510k' as table_name, COUNT(*) as count FROM device_510k
UNION ALL
SELECT 
    'guidance_document' as table_name, COUNT(*) as count FROM guidance_document
UNION ALL
SELECT 
    'customs_case' as table_name, COUNT(*) as count FROM customs_case
UNION ALL
SELECT 
    'device_registration_record' as table_name, COUNT(*) as count FROM device_registration_record
UNION ALL
SELECT 
    'device_recall_record' as table_name, COUNT(*) as count FROM device_recall_record
UNION ALL
SELECT 
    'device_event_report' as table_name, COUNT(*) as count FROM device_event_report;
```

## 故障排除

### 连接问题
- 检查网络连接
- 确认 Zeabur MySQL 服务正在运行
- 验证连接信息是否正确

### 导入问题
- 检查备份文件是否完整
- 确认目标数据库有足够的权限
- 查看错误日志

### 数据问题
- 验证字符集设置
- 检查数据类型兼容性
- 确认外键约束

## 迁移后的步骤

1. **重启后端服务**：让 Spring Boot 重新连接数据库
2. **测试应用功能**：访问 https://eveindex.zeabur.app/
3. **验证数据完整性**：检查所有功能是否正常工作

## 注意事项

- ⚠️ 迁移过程会覆盖 Zeabur 数据库中的现有数据
- 💾 建议在迁移前备份 Zeabur 数据库
- ⏱️ 迁移时间取决于数据量大小
- 🔒 确保连接信息的安全性
