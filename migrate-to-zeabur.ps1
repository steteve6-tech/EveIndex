# 数据库迁移脚本 - 将本地数据迁移到 Zeabur (PowerShell 版本)
# 作者: AI Assistant
# 日期: Get-Date

Write-Host "=== 数据库迁移到 Zeabur ===" -ForegroundColor Green
Write-Host "时间: $(Get-Date)" -ForegroundColor Yellow
Write-Host ""

# Zeabur MySQL 连接信息
$ZEABUR_HOST = "sjc1.clusters.zeabur.com"
$ZEABUR_PORT = "32188"
$ZEABUR_USER = "root"
$ZEABUR_PASSWORD = "lSA1WT05oPUMyb746xzQ8EcwBRY932aq"
$ZEABUR_DATABASE = "zeabur"

# 本地备份文件
$LOCAL_BACKUP = "local_database_backup.sql"

Write-Host "=== 1. 检查本地备份文件 ===" -ForegroundColor Cyan
if (Test-Path $LOCAL_BACKUP) {
    $fileSize = (Get-Item $LOCAL_BACKUP).Length / 1MB
    Write-Host "✅ 找到本地备份文件: $LOCAL_BACKUP" -ForegroundColor Green
    Write-Host "📊 文件大小: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Yellow
} else {
    Write-Host "❌ 未找到本地备份文件: $LOCAL_BACKUP" -ForegroundColor Red
    Write-Host "请先运行: mysqldump -u root -p2020 --single-transaction --routines --triggers common_db > local_database_backup.sql" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

Write-Host "=== 2. 测试 Zeabur 数据库连接 ===" -ForegroundColor Cyan
$testConnection = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" -e "SELECT 1;" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Zeabur 数据库连接成功" -ForegroundColor Green
} else {
    Write-Host "❌ Zeabur 数据库连接失败" -ForegroundColor Red
    Write-Host "请检查连接信息是否正确" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

Write-Host "=== 3. 创建目标数据库（如果不存在）===" -ForegroundColor Cyan
$createDatabase = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS \`$ZEABUR_DATABASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 目标数据库准备就绪" -ForegroundColor Green
} else {
    Write-Host "❌ 创建目标数据库失败" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "=== 4. 开始数据迁移 ===" -ForegroundColor Cyan
Write-Host "⚠️  这将覆盖 Zeabur 数据库中的所有现有数据" -ForegroundColor Yellow
Write-Host "⏳ 正在导入数据，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

# 导入数据
$importResult = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" $ZEABUR_DATABASE < $LOCAL_BACKUP

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 数据迁移成功完成！" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "=== 5. 验证迁移结果 ===" -ForegroundColor Cyan
    Write-Host "📊 数据库表数量:" -ForegroundColor Yellow
    $tableCount = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" $ZEABUR_DATABASE -e "SHOW TABLES;" 2>$null | Measure-Object -Line | Select-Object -ExpandProperty Lines
    Write-Host "表数量: $tableCount" -ForegroundColor White
    
    Write-Host "📊 主要表的数据量:" -ForegroundColor Yellow
    $dataCount = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" $ZEABUR_DATABASE -e "
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
    " 2>$null
    
    Write-Host $dataCount -ForegroundColor White
    
    Write-Host ""
    Write-Host "🎉 数据库迁移完成！" -ForegroundColor Green
    Write-Host "🌐 现在可以访问 https://eveindex.zeabur.app/ 查看迁移后的数据" -ForegroundColor Cyan
    
} else {
    Write-Host "❌ 数据迁移失败" -ForegroundColor Red
    Write-Host "请检查错误信息并重试" -ForegroundColor Yellow
    exit 1
}
