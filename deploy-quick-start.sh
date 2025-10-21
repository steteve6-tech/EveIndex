#!/bin/bash

# ================================================================================
# 医疗器械认证监控系统 - 快速部署脚本
# ================================================================================
# 功能: 一键部署完整系统（包含 MySQL 数据库）
# 使用: bash deploy-quick-start.sh
# ================================================================================

set -e  # 遇到错误立即退出

echo "========================================"
echo "医疗器械认证监控系统 - 快速部署"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否为 root 用户
if [ "$EUID" -eq 0 ]; then
   echo -e "${YELLOW}警告: 不建议使用 root 用户运行${NC}"
   read -p "是否继续? (y/n) " -n 1 -r
   echo
   if [[ ! $REPLY =~ ^[Yy]$ ]]; then
       exit 1
   fi
fi

# 步骤1: 检查依赖
echo -e "${GREEN}[1/7] 检查系统依赖...${NC}"
if ! command -v podman-compose &> /dev/null && ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: 未找到 podman-compose 或 docker-compose${NC}"
    echo "请先安装 Podman Desktop 或 Docker"
    exit 1
fi

# 使用 podman-compose 或 docker-compose
if command -v podman-compose &> /dev/null; then
    COMPOSE_CMD="podman-compose"
    echo "✓ 使用 Podman Compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
    echo "✓ 使用 Docker Compose"
fi

# 步骤2: 创建环境配置文件
echo -e "${GREEN}[2/7] 配置环境变量...${NC}"
if [ ! -f .env.prod ]; then
    if [ -f .env.prod.minimal ]; then
        cp .env.prod.minimal .env.prod
        echo "✓ 已创建 .env.prod（使用最小化配置）"
        echo ""
        echo -e "${YELLOW}重要提醒:${NC}"
        echo "1. 请编辑 .env.prod 文件"
        echo "2. 修改所有密码（MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD 等）"
        echo "3. 修改 CORS_ALLOWED_ORIGINS 为实际服务器IP或域名"
        echo ""
        read -p "是否现在编辑配置文件? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-vi} .env.prod
        else
            echo -e "${YELLOW}请稍后手动编辑 .env.prod 文件并重新部署${NC}"
        fi
    else
        echo -e "${RED}错误: 未找到 .env.prod.minimal 模板文件${NC}"
        exit 1
    fi
else
    echo "✓ .env.prod 文件已存在"
fi

# 步骤3: 检查数据库初始化脚本
echo -e "${GREEN}[3/7] 检查数据库初始化脚本...${NC}"
if [ ! -f database/init_database_full.sql ]; then
    echo -e "${RED}错误: 未找到 database/init_database_full.sql${NC}"
    echo "请确保数据库初始化脚本存在"
    exit 1
fi
echo "✓ 数据库初始化脚本就绪"

# 步骤4: 停止旧容器（如果存在）
echo -e "${GREEN}[4/7] 清理旧容器...${NC}"
if $COMPOSE_CMD -f docker-compose.prod.yml ps | grep -q "Up"; then
    echo "发现运行中的容器，正在停止..."
    $COMPOSE_CMD -f docker-compose.prod.yml down
    echo "✓ 旧容器已停止"
else
    echo "✓ 无需清理"
fi

# 步骤5: 构建镜像
echo -e "${GREEN}[5/7] 构建 Docker 镜像...${NC}"
echo "这可能需要几分钟时间，请耐心等待..."
$COMPOSE_CMD -f docker-compose.prod.yml build --no-cache
echo "✓ 镜像构建完成"

# 步骤6: 启动所有服务
echo -e "${GREEN}[6/7] 启动所有服务...${NC}"
$COMPOSE_CMD -f docker-compose.prod.yml up -d

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 步骤7: 验证部署
echo -e "${GREEN}[7/7] 验证部署状态...${NC}"
$COMPOSE_CMD -f docker-compose.prod.yml ps

echo ""
echo "========================================"
echo -e "${GREEN}部署完成！${NC}"
echo "========================================"
echo ""
echo "📋 服务访问地址:"
echo "  前端应用:        http://localhost"
echo "  后端API:         http://localhost:8080/api"
echo "  API文档:         http://localhost:8080/api/doc.html"
echo "  数据库管理:      http://localhost:8081"
echo "  Druid监控:       http://localhost:8080/druid"
echo ""
echo "🔑 默认登录信息:"
echo "  数据库管理 (phpMyAdmin):"
echo "    用户名: root"
echo "    密码: 查看 .env.prod 中的 MYSQL_ROOT_PASSWORD"
echo ""
echo "  Druid监控:"
echo "    用户名: admin"
echo "    密码: 查看 .env.prod 中的 DRUID_PASSWORD"
echo ""
echo "📊 查看日志:"
echo "  所有服务:  $COMPOSE_CMD -f docker-compose.prod.yml logs -f"
echo "  后端:      $COMPOSE_CMD -f docker-compose.prod.yml logs -f backend"
echo "  前端:      $COMPOSE_CMD -f docker-compose.prod.yml logs -f frontend"
echo "  数据库:    $COMPOSE_CMD -f docker-compose.prod.yml logs -f mysql"
echo ""
echo "🛑 停止服务:"
echo "  $COMPOSE_CMD -f docker-compose.prod.yml stop"
echo ""
echo "🔄 重启服务:"
echo "  $COMPOSE_CMD -f docker-compose.prod.yml restart"
echo ""
echo "❌ 完全清理:"
echo "  $COMPOSE_CMD -f docker-compose.prod.yml down -v"
echo ""
echo -e "${YELLOW}注意: 如果服务无法访问，请检查防火墙设置${NC}"
echo ""
