# 数据库备份脚本
param(
    [string]$BackupName = "backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
)

Write-Host "开始备份数据库..." -ForegroundColor Green

try {
    # 创建备份目录
    $backupDir = "database_backups"
    if (!(Test-Path $backupDir)) {
        New-Item -ItemType Directory -Path $backupDir
        Write-Host "创建备份目录: $backupDir" -ForegroundColor Cyan
    }
    
    # 备份数据库结构
    $structureFile = "$backupDir/${BackupName}_structure.sql"
    Write-Host "备份数据库结构到: $structureFile" -ForegroundColor Yellow
    mysqldump -u root -p2020 --no-data --routines --triggers common_db > $structureFile
    
    # 备份数据库数据
    $dataFile = "$backupDir/${BackupName}_data.sql"
    Write-Host "备份数据库数据到: $dataFile" -ForegroundColor Yellow
    mysqldump -u root -p2020 --no-create-info --default-character-set=utf8mb4 --hex-blob common_db > $dataFile
    
    # 备份完整数据库
    $fullFile = "$backupDir/${BackupName}_full.sql"
    Write-Host "备份完整数据库到: $fullFile" -ForegroundColor Yellow
    mysqldump -u root -p2020 --default-character-set=utf8mb4 --hex-blob common_db > $fullFile
    
    Write-Host "数据库备份完成!" -ForegroundColor Green
    Write-Host "备份文件:" -ForegroundColor Cyan
    Write-Host "  结构: $structureFile" -ForegroundColor White
    Write-Host "  数据: $dataFile" -ForegroundColor White
    Write-Host "  完整: $fullFile" -ForegroundColor White
    
    # 显示备份文件大小
    Get-ChildItem "$backupDir/${BackupName}_*" | ForEach-Object {
        $size = [math]::Round($_.Length / 1MB, 2)
        Write-Host "  $($_.Name): $size MB" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "备份失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}


