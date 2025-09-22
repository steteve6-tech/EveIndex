#!/bin/bash

# 快速部署脚本 - 用于紧急部署或本地测试

set -e

# 配置
PROJECT_DIR="/opt/AAAA"
BACKUP_DIR="/opt/backups"

echo "🚀 开始快速部署..."

# 检查是否在项目目录
if [ ! -d "$PROJECT_DIR" ]; then
    echo "❌ 项目目录不存在: $PROJECT_DIR"
    exit 1
fi

cd $PROJECT_DIR

# 拉取最新代码
echo "📥 拉取最新代码..."
git pull origin main

# 备份当前运行的服务
echo "💾 备份当前服务..."
if pgrep -f "spring-boot-backend" > /dev/null; then
    pkill -f "spring-boot-backend"
    sleep 3
fi

# 构建后端
echo "🔨 构建后端..."
cd spring-boot-backend
mvn clean package -DskipTests -q

# 构建前端
echo "🔨 构建前端..."
cd ../vue-frontend
npm ci --silent
npm run build

# 部署前端
echo "📦 部署前端..."
sudo rm -rf /var/www/html/*
sudo cp -r dist/* /var/www/html/
sudo chown -R www-data:www-data /var/www/html

# 启动后端
echo "🚀 启动后端..."
cd ../spring-boot-backend
nohup java -jar target/*.jar \
    --spring.profiles.active=prod \
    > ../logs/quick-deploy.log 2>&1 &

# 重启Nginx
echo "🔄 重启Nginx..."
sudo systemctl restart nginx

echo "✅ 快速部署完成！"
echo "🌐 访问地址: http://$(curl -s ifconfig.me)"
echo "📋 查看日志: tail -f $PROJECT_DIR/logs/quick-deploy.log"
