#!/bin/bash

# Zeabur前后端连接部署脚本
echo "🚀 开始Zeabur前后端连接部署..."

# 检查必要文件
echo "📋 检查必要文件..."
required_files=(
    "zeabur.yaml"
    "vue-frontend/Dockerfile.zeabur"
    "vue-frontend/nginx.zeabur.conf"
    "spring-boot-backend/Dockerfile.zeabur"
    "spring-boot-backend/src/main/resources/application-zeabur.yml"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ 缺少必要文件: $file"
        exit 1
    fi
done

echo "✅ 所有必要文件存在"

# 构建前端镜像
echo "🔨 构建前端镜像..."
cd vue-frontend
docker build -f Dockerfile.zeabur -t certification-frontend:latest .
if [ $? -eq 0 ]; then
    echo "✅ 前端镜像构建成功"
else
    echo "❌ 前端镜像构建失败"
    exit 1
fi

# 构建后端镜像
echo "🔨 构建后端镜像..."
cd ../spring-boot-backend
docker build -f Dockerfile.zeabur -t certification-backend:latest .
if [ $? -eq 0 ]; then
    echo "✅ 后端镜像构建成功"
else
    echo "❌ 后端镜像构建失败"
    exit 1
fi

echo "🎉 所有镜像构建完成！"

# 显示部署指南
echo ""
echo "📝 接下来请在Zeabur Dashboard中："
echo ""
echo "1. 创建服务："
echo "   - 后端服务: 构建路径 ./spring-boot-backend"
echo "   - 前端服务: 构建路径 ./vue-frontend"
echo "   - MySQL服务: 版本 8.0"
echo "   - Redis服务: 版本 7.0"
echo ""
echo "2. 配置环境变量："
echo "   后端服务："
echo "   - SPRING_PROFILES_ACTIVE=zeabur"
echo "   - SPRING_DATASOURCE_URL=\${MYSQL_URL}"
echo "   - SPRING_DATASOURCE_USERNAME=\${MYSQL_USERNAME}"
echo "   - SPRING_DATASOURCE_PASSWORD=\${MYSQL_PASSWORD}"
echo "   - SPRING_DATA_REDIS_HOST=\${REDIS_HOST}"
echo "   - SPRING_DATA_REDIS_PORT=\${REDIS_PORT}"
echo ""
echo "   前端服务："
echo "   - VITE_API_BASE_URL=https://your-backend-domain.zeabur.app/api"
echo "   - BACKEND_URL=https://your-backend-domain.zeabur.app"
echo ""
echo "3. 设置服务依赖："
echo "   - 前端服务依赖后端服务"
echo "   - 后端服务依赖MySQL和Redis"
echo ""
echo "4. 部署顺序："
echo "   1. 先部署MySQL和Redis"
echo "   2. 再部署后端服务"
echo "   3. 最后部署前端服务"
echo ""
echo "5. 测试连接："
echo "   - 访问前端: https://your-frontend-domain.zeabur.app"
echo "   - 测试API: https://your-frontend-domain.zeabur.app/test-api.html"
echo "   - 后端健康检查: https://your-backend-domain.zeabur.app/api/health"
echo ""
echo "📚 详细配置请参考: ZEABUR_FRONTEND_BACKEND_CONNECTION.md"
