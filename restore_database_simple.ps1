# 简单数据库恢复脚本
param(
    [string]$BackupFile = "database_backups/backup_20250924_143000_full.sql"
)

Write-Host "开始恢复数据库..." -ForegroundColor Green

if (!(Test-Path $BackupFile)) {
    Write-Host "备份文件不存在: $BackupFile" -ForegroundColor Red
    Write-Host "可用的备份文件:" -ForegroundColor Yellow
    if (Test-Path "database_backups") {
        Get-ChildItem "database_backups/*.sql" | ForEach-Object {
            Write-Host "  $($_.Name)" -ForegroundColor White
        }
    }
    exit 1
}

try {
    Write-Host "从 $BackupFile 恢复数据库..." -ForegroundColor Yellow
    
    # 清空数据库（可选）
    $confirm = Read-Host "是否清空现有数据库? (y/N)"
    if ($confirm -eq "y" -or $confirm -eq "Y") {
        Write-Host "清空现有数据库..." -ForegroundColor Yellow
        mysql -u root -p2020 -e "DROP DATABASE IF EXISTS common_db; CREATE DATABASE common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    }
    
    # 恢复数据库
    Get-Content $BackupFile -Encoding UTF8 | mysql -u root -p2020 common_db
    
    Write-Host "数据库恢复完成!" -ForegroundColor Green
    
    # 验证恢复结果
    Write-Host "验证恢复结果..." -ForegroundColor Yellow
    $deviceCount = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM t_device_510k;" 2>$null
    if ($deviceCount) {
        Write-Host "t_device_510k 表数据: $deviceCount 条记录" -ForegroundColor Green
    }
    
} catch {
    Write-Host "恢复失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}


