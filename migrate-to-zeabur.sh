#!/bin/bash

# 数据库迁移脚本 - 将本地数据迁移到 Zeabur
# 作者: AI Assistant
# 日期: $(date)

echo "=== 数据库迁移到 Zeabur ==="
echo "时间: $(date)"
echo ""

# Zeabur MySQL 连接信息
ZEABUR_HOST="sjc1.clusters.zeabur.com"
ZEABUR_PORT="32188"
ZEABUR_USER="root"
ZEABUR_PASSWORD="lSA1WT05oPUMyb746xzQ8EcwBRY932aq"
ZEABUR_DATABASE="zeabur"

# 本地备份文件
LOCAL_BACKUP="local_database_backup.sql"

echo "=== 1. 检查本地备份文件 ==="
if [ -f "$LOCAL_BACKUP" ]; then
    echo "✅ 找到本地备份文件: $LOCAL_BACKUP"
    echo "📊 文件大小: $(du -h $LOCAL_BACKUP | cut -f1)"
else
    echo "❌ 未找到本地备份文件: $LOCAL_BACKUP"
    echo "请先运行: mysqldump -u root -p2020 --single-transaction --routines --triggers common_db > local_database_backup.sql"
    exit 1
fi
echo ""

echo "=== 2. 测试 Zeabur 数据库连接 ==="
mysql -h "$ZEABUR_HOST" -P "$ZEABUR_PORT" -u "$ZEABUR_USER" -p"$ZEABUR_PASSWORD" -e "SELECT 1;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Zeabur 数据库连接成功"
else
    echo "❌ Zeabur 数据库连接失败"
    echo "请检查连接信息是否正确"
    exit 1
fi
echo ""

echo "=== 3. 创建目标数据库（如果不存在）==="
mysql -h "$ZEABUR_HOST" -P "$ZEABUR_PORT" -u "$ZEABUR_USER" -p"$ZEABUR_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS \`$ZEABUR_DATABASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 目标数据库准备就绪"
else
    echo "❌ 创建目标数据库失败"
    exit 1
fi
echo ""

echo "=== 4. 开始数据迁移 ==="
echo "⚠️  这将覆盖 Zeabur 数据库中的所有现有数据"
echo "⏳ 正在导入数据，请耐心等待..."
echo ""

# 导入数据
mysql -h "$ZEABUR_HOST" -P "$ZEABUR_PORT" -u "$ZEABUR_USER" -p"$ZEABUR_PASSWORD" "$ZEABUR_DATABASE" < "$LOCAL_BACKUP"

if [ $? -eq 0 ]; then
    echo "✅ 数据迁移成功完成！"
    echo ""
    
    echo "=== 5. 验证迁移结果 ==="
    echo "📊 数据库表数量:"
    mysql -h "$ZEABUR_HOST" -P "$ZEABUR_PORT" -u "$ZEABUR_USER" -p"$ZEABUR_PASSWORD" "$ZEABUR_DATABASE" -e "SHOW TABLES;" 2>/dev/null | wc -l
    
    echo "📊 主要表的数据量:"
    mysql -h "$ZEABUR_HOST" -P "$ZEABUR_PORT" -u "$ZEABUR_USER" -p"$ZEABUR_PASSWORD" "$ZEABUR_DATABASE" -e "
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
    " 2>/dev/null
    
    echo ""
    echo "🎉 数据库迁移完成！"
    echo "🌐 现在可以访问 https://eveindex.zeabur.app/ 查看迁移后的数据"
    
else
    echo "❌ 数据迁移失败"
    echo "请检查错误信息并重试"
    exit 1
fi
