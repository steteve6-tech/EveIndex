#!/bin/bash

# Zeabur数据库连接修复脚本
echo "🔧 修复Zeabur数据库连接问题..."

echo "📋 问题诊断："
echo "错误信息显示后端仍在尝试连接 'zeabur' 数据库"
echo "而不是 'common_db' 数据库"
echo ""

echo "🎯 解决方案："
echo "1. 在Zeabur Dashboard中检查后端服务环境变量"
echo "2. 确保没有手动设置 SPRING_DATASOURCE_URL"
echo "3. 使用正确的服务引用格式"
echo ""

echo "✅ 正确的环境变量配置："
echo ""
echo "MySQL服务环境变量："
echo "MYSQL_ROOT_PASSWORD=your-secure-root-password"
echo "MYSQL_DATABASE=common_db"
echo "MYSQL_USER=app_user"
echo "MYSQL_PASSWORD=your-secure-app-password"
echo ""
echo "后端服务环境变量："
echo "SPRING_PROFILES_ACTIVE=zeabur"
echo "SPRING_DATASOURCE_URL=\${{mysql.DATABASE_URL}}"
echo "SPRING_DATASOURCE_USERNAME=\${{mysql.USERNAME}}"
echo "SPRING_DATASOURCE_PASSWORD=\${{mysql.PASSWORD}}"
echo ""

echo "🚨 重要提醒："
echo "- 不要手动设置 SPRING_DATASOURCE_URL 的值"
echo "- 必须使用 \${{mysql.DATABASE_URL}} 格式"
echo "- 这会自动指向 common_db 数据库"
echo ""

echo "🔄 修复步骤："
echo "1. 进入Zeabur Dashboard"
echo "2. 选择后端服务"
echo "3. 进入环境变量设置"
echo "4. 删除任何手动设置的 SPRING_DATASOURCE_URL"
echo "5. 添加正确的环境变量（如上所示）"
echo "6. 保存并重新部署服务"
echo ""

echo "🔍 验证方法："
echo "部署完成后，查看后端服务日志，应该看到："
echo "Connected to database: common_db"
echo "而不是："
echo "Connected to database: zeabur"
echo ""

echo "📚 详细说明请参考："
echo "- ZEABUR_ENVIRONMENT_VARIABLES.md"
echo "- ZEABUR_DATABASE_SETUP.md"
echo ""

echo "🎉 修复完成后，你的后端将正确连接到 common_db 数据库！"
