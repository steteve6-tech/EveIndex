# 恢复缺失数据的脚本
Write-Host "开始恢复缺失的数据..." -ForegroundColor Green

# 1. 恢复指导文档数据
Write-Host "步骤1: 尝试恢复指导文档数据..." -ForegroundColor Yellow
try {
    # 先清空表再导入
    mysql -u root -p2020 -e "USE common_db; TRUNCATE TABLE t_guidance_document;" 2>$null
    Get-Content guidance_document_clean.sql -Encoding UTF8 | mysql -u root -p2020 common_db 2>$null
    $guidanceCount = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM t_guidance_document;" 2>$null
    Write-Host "指导文档数据: $guidanceCount 条记录" -ForegroundColor Green
} catch {
    Write-Host "指导文档恢复失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 恢复国家风险统计数据
Write-Host "步骤2: 尝试恢复国家风险统计数据..." -ForegroundColor Yellow
try {
    # 先清空表再导入
    mysql -u root -p2020 -e "USE common_db; TRUNCATE TABLE daily_country_risk_stats;" 2>$null
    # 从备份文件中提取只包含daily_country_risk_stats的数据
    Select-String -Path "local_database_clean.sql" -Pattern "INSERT INTO \`daily_country_risk_stats\`" | ForEach-Object { $_.Line } | mysql -u root -p2020 common_db 2>$null
    $statsCount = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM daily_country_risk_stats;" 2>$null
    Write-Host "国家风险统计数据: $statsCount 条记录" -ForegroundColor Green
} catch {
    Write-Host "国家风险统计恢复失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 恢复其他表的数据
Write-Host "步骤3: 尝试恢复其他表的数据..." -ForegroundColor Yellow
try {
    # 恢复crawler_data
    $crawlerData = Select-String -Path "local_database_backup.sql" -Pattern "INSERT INTO \`t_crawler_data\`" | Select-Object -First 10
    if ($crawlerData) {
        $crawlerData | ForEach-Object { $_.Line } | mysql -u root -p2020 common_db 2>$null
        $crawlerCount = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM t_crawler_data;" 2>$null
        Write-Host "爬虫数据: $crawlerCount 条记录" -ForegroundColor Green
    }
} catch {
    Write-Host "其他数据恢复失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 显示最终统计
Write-Host "步骤4: 最终数据统计..." -ForegroundColor Yellow
Write-Host "各表数据统计:" -ForegroundColor Cyan
$tables = @("t_device_510k", "t_guidance_document", "daily_country_risk_stats", "t_crawler_data", "t_standard", "t_product", "t_country")
foreach ($table in $tables) {
    try {
        $count = mysql -u root -p2020 -e "USE common_db; SELECT COUNT(*) FROM $table;" 2>$null
        Write-Host "  $table`: $count 条记录" -ForegroundColor White
    } catch {
        Write-Host "  $table`: 查询失败" -ForegroundColor Red
    }
}

Write-Host "数据恢复完成!" -ForegroundColor Green


