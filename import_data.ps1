# 数据库导入脚本
# 使用 PowerShell 导入数据到 Zeabur

Write-Host "=== 开始导入数据到 Zeabur ===" -ForegroundColor Green
Write-Host "时间: $(Get-Date)" -ForegroundColor Yellow
Write-Host ""

# Zeabur 连接信息
$ZEABUR_HOST = "sjc1.clusters.zeabur.com"
$ZEABUR_PORT = "32188"
$ZEABUR_USER = "root"
$ZEABUR_PASSWORD = "lSA1WT05oPUMyb746xzQ8EcwBRY932aq"
$ZEABUR_DATABASE = "zeabur"

# 备份文件
$BACKUP_FILE = "local_database_clean.sql"

Write-Host "=== 1. 检查备份文件 ===" -ForegroundColor Cyan
if (Test-Path $BACKUP_FILE) {
    $fileSize = (Get-Item $BACKUP_FILE).Length / 1MB
    Write-Host "✅ 找到备份文件: $BACKUP_FILE" -ForegroundColor Green
    Write-Host "📊 文件大小: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Yellow
} else {
    Write-Host "❌ 未找到备份文件: $BACKUP_FILE" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "=== 2. 测试数据库连接 ===" -ForegroundColor Cyan
$testResult = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" -e "SELECT 1;" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 数据库连接成功" -ForegroundColor Green
} else {
    Write-Host "❌ 数据库连接失败" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "=== 3. 开始导入数据 ===" -ForegroundColor Cyan
Write-Host "⚠️  正在导入数据，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

# 使用 Get-Content 逐行读取并导入
$importResult = & {
    $content = Get-Content $BACKUP_FILE -Encoding UTF8
    $content | mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" $ZEABUR_DATABASE
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 数据导入成功！" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "=== 4. 验证导入结果 ===" -ForegroundColor Cyan
    Write-Host "📊 检查主要表的数据量:" -ForegroundColor Yellow
    
    $verification = & mysql -h $ZEABUR_HOST -P $ZEABUR_PORT -u $ZEABUR_USER -p"$ZEABUR_PASSWORD" $ZEABUR_DATABASE -e "
        SELECT 
            't_device_510k' as table_name, COUNT(*) as count FROM t_device_510k
        UNION ALL
        SELECT 
            't_guidance_document' as table_name, COUNT(*) as count FROM t_guidance_document
        UNION ALL
        SELECT 
            't_customs_case' as table_name, COUNT(*) as count FROM t_customs_case
        UNION ALL
        SELECT 
            't_device_recall' as table_name, COUNT(*) as count FROM t_device_recall
        UNION ALL
        SELECT 
            't_device_registration' as table_name, COUNT(*) as count FROM t_device_registration
        UNION ALL
        SELECT 
            't_device_event' as table_name, COUNT(*) as count FROM t_device_event;
    " 2>$null
    
    Write-Host $verification -ForegroundColor White
    
    Write-Host ""
    Write-Host "🎉 数据导入完成！" -ForegroundColor Green
    Write-Host "🌐 现在可以访问 https://eveindex.zeabur.app/ 查看数据" -ForegroundColor Cyan
    
} else {
    Write-Host "❌ 数据导入失败" -ForegroundColor Red
    Write-Host "错误代码: $LASTEXITCODE" -ForegroundColor Yellow
}



