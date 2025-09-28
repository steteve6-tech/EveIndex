# 数据库恢复脚本
Write-Host "开始恢复数据库..." -ForegroundColor Green

# 1. 首先创建数据库结构
Write-Host "步骤1: 创建数据库结构..." -ForegroundColor Yellow
try {
    # 使用包含表结构的备份文件
    $structureFile = "local_database_backup.sql"
    if (Test-Path $structureFile) {
        Write-Host "从 $structureFile 恢复数据库结构..." -ForegroundColor Cyan
        Get-Content $structureFile -Encoding UTF8 | mysql -u root -p2020 common_db
        Write-Host "数据库结构恢复完成!" -ForegroundColor Green
    } else {
        Write-Host "备份文件不存在: $structureFile" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "恢复数据库结构失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 验证表是否创建
Write-Host "步骤2: 验证表创建..." -ForegroundColor Yellow
try {
    $tables = mysql -u root -p2020 -e "USE common_db; SHOW TABLES;" 2>$null
    if ($tables) {
        Write-Host "表创建成功!" -ForegroundColor Green
        Write-Host "表列表:" -ForegroundColor Cyan
        $tables | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    } else {
        Write-Host "表创建失败!" -ForegroundColor Red
    }
} catch {
    Write-Host "验证表创建失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 检查数据
Write-Host "步骤3: 检查数据..." -ForegroundColor Yellow
try {
    $deviceCount = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM t_device_510k;" 2>$null
    if ($deviceCount) {
        Write-Host "t_device_510k 表数据: $deviceCount 条记录" -ForegroundColor Green
    } else {
        Write-Host "t_device_510k 表没有数据" -ForegroundColor Yellow
    }
} catch {
    Write-Host "检查数据失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "数据库恢复完成!" -ForegroundColor Green


