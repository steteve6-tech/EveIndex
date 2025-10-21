#!/bin/bash

# ================================================================================
# 本地开发环境快速启动脚本（Linux/Mac）
# ================================================================================
# 功能: 启动本地开发所需的所有服务
# 使用: bash start-local-dev.sh
# ================================================================================

set -e

echo "========================================"
echo "本地开发环境 - 快速启动"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查 Docker/Podman
if command -v podman-compose &> /dev/null; then
    COMPOSE_CMD="podman-compose"
    echo -e "${GREEN}✓ 使用 Podman Compose${NC}"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
    echo -e "${GREEN}✓ 使用 Docker Compose${NC}"
else
    echo -e "${RED}× 未找到 podman-compose 或 docker-compose${NC}"
    echo ""
    echo "你可以选择："
    echo "1. 安装 Podman Desktop 或 Docker Desktop"
    echo "2. 手动启动数据库和服务（参考 LOCAL_DEVELOPMENT_GUIDE.md）"
    exit 1
fi

# 检查环境变量文件
if [ ! -f .env.dev ]; then
    echo ""
    echo -e "${YELLOW}[配置环境变量]${NC}"
    if [ -f .env.dev.example ]; then
        echo "未找到 .env.dev 文件，正在从模板创建..."
        cp .env.dev.example .env.dev
        echo -e "${GREEN}✓ 已创建 .env.dev${NC}"
        echo ""
        echo -e "${YELLOW}重要提醒:${NC}"
        echo "1. 已使用默认配置创建 .env.dev"
        echo "2. 如需使用 AI 功能，请编辑 .env.dev 填入 API 密钥"
        echo ""
        read -p "是否现在编辑配置文件? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-vi} .env.dev
        fi
    else
        echo -e "${RED}× 错误: 未找到 .env.dev.example 模板文件${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✓ 环境配置文件已存在${NC}"
fi

echo ""
echo -e "${BLUE}[启动数据库服务]${NC}"
echo "正在启动 MySQL 和 Redis..."
$COMPOSE_CMD -f docker-compose.dev.yml up -d mysql redis phpmyadmin

echo -e "${GREEN}✓ 数据库服务已启动${NC}"
echo ""
echo "等待数据库初始化（约 30 秒）..."
sleep 30

echo ""
echo -e "${BLUE}[检查服务状态]${NC}"
$COMPOSE_CMD -f docker-compose.dev.yml ps

echo ""
echo "========================================"
echo -e "${GREEN}数据库服务已启动！${NC}"
echo "========================================"
echo ""
echo "📊 可用服务:"
echo "  MySQL:           localhost:3306"
echo "  phpMyAdmin:      http://localhost:8081 (root/dev123)"
echo "  Redis:           localhost:6379"
echo ""
echo "🚀 下一步操作:"
echo ""
echo "1. 启动后端服务（在新的终端）:"
echo "   cd spring-boot-backend"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=local"
echo ""
echo "2. 启动前端服务（在新的终端）:"
echo "   cd vue-frontend"
echo "   npm install    # 首次运行"
echo "   npm run dev"
echo ""
echo "3. 访问应用:"
echo "   前端:  http://localhost:3000"
echo "   后端:  http://localhost:8080/api"
echo "   文档:  http://localhost:8080/api/doc.html"
echo ""
echo "📖 详细说明请查看: LOCAL_DEVELOPMENT_GUIDE.md"
echo ""
echo "🛑 停止服务:"
echo "   $COMPOSE_CMD -f docker-compose.dev.yml stop"
echo ""
