#!/bin/bash

# ================================
# 一键部署脚本
# ================================
# 用途: 在服务器上快速部署应用
# 使用方式: ./deploy.sh [start|stop|restart|logs|status]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

# 打印消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    print_success "Docker 环境检查通过"
}

# 检查环境配置文件
check_env() {
    if [ ! -f "$ENV_FILE" ]; then
        print_error "环境配置文件 ${ENV_FILE} 不存在"
        print_info "请从 .env.prod.example 复制并配置环境变量"
        print_info "cp .env.prod.example .env.prod"
        exit 1
    fi
    print_success "环境配置文件检查通过"
}

# 启动服务
start_services() {
    print_info "启动服务..."
    check_docker
    check_env

    # 拉取最新镜像（如果使用远程镜像）
    print_info "拉取最新镜像..."
    docker-compose -f "$COMPOSE_FILE" pull || print_warning "无法拉取镜像，将使用本地镜像"

    # 启动服务
    docker-compose -f "$COMPOSE_FILE" up -d

    print_success "服务启动完成！"
    print_info "等待服务就绪..."
    sleep 10

    # 显示服务状态
    show_status
}

# 停止服务
stop_services() {
    print_info "停止服务..."
    docker-compose -f "$COMPOSE_FILE" down
    print_success "服务已停止"
}

# 重启服务
restart_services() {
    print_info "重启服务..."
    stop_services
    sleep 3
    start_services
}

# 显示日志
show_logs() {
    SERVICE=${1:-}
    if [ -z "$SERVICE" ]; then
        print_info "显示所有服务日志 (Ctrl+C 退出)..."
        docker-compose -f "$COMPOSE_FILE" logs -f --tail=100
    else
        print_info "显示 ${SERVICE} 服务日志 (Ctrl+C 退出)..."
        docker-compose -f "$COMPOSE_FILE" logs -f --tail=100 "$SERVICE"
    fi
}

# 显示服务状态
show_status() {
    print_info "服务运行状态:"
    docker-compose -f "$COMPOSE_FILE" ps

    echo ""
    print_info "服务健康检查:"

    # 检查后端健康
    if curl -f http://localhost:8080/api/health &> /dev/null; then
        print_success "后端服务: 运行正常"
    else
        print_warning "后端服务: 未就绪或异常"
    fi

    # 检查前端
    if curl -f http://localhost/ &> /dev/null; then
        print_success "前端服务: 运行正常"
    else
        print_warning "前端服务: 未就绪或异常"
    fi

    # 检查 MySQL
    if docker-compose -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -h localhost &> /dev/null; then
        print_success "MySQL 数据库: 运行正常"
    else
        print_warning "MySQL 数据库: 未就绪或异常"
    fi

    # 检查 Redis
    if docker-compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping &> /dev/null; then
        print_success "Redis 缓存: 运行正常"
    else
        print_warning "Redis 缓存: 未就绪或异常"
    fi

    echo ""
    print_info "访问地址:"
    print_info "  前端: http://localhost"
    print_info "  后端: http://localhost:8080/api"
    print_info "  Swagger: http://localhost:8080/api/doc.html"
    print_info "  Druid监控: http://localhost:8080/api/druid"
    print_info "  phpMyAdmin: http://localhost:8081"
}

# 数据库备份
backup_database() {
    print_info "备份数据库..."

    BACKUP_DIR="./database_backups"
    mkdir -p "$BACKUP_DIR"

    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.sql"

    docker-compose -f "$COMPOSE_FILE" exec -T mysql mysqldump \
        -u root -p"${MYSQL_ROOT_PASSWORD}" \
        --all-databases \
        --single-transaction \
        --quick \
        --lock-tables=false \
        > "$BACKUP_FILE"

    # 压缩备份文件
    gzip "$BACKUP_FILE"

    print_success "数据库备份完成: ${BACKUP_FILE}.gz"

    # 清理旧备份（保留最近30天）
    find "$BACKUP_DIR" -name "backup_*.sql.gz" -mtime +30 -delete
    print_info "已清理30天前的旧备份"
}

# 更新应用
update_app() {
    print_info "更新应用..."

    # 备份数据库
    backup_database

    # 拉取最新代码（如果使用Git部署）
    if [ -d ".git" ]; then
        print_info "拉取最新代码..."
        git pull
    fi

    # 重新构建镜像
    print_info "重新构建镜像..."
    docker-compose -f "$COMPOSE_FILE" build --no-cache

    # 重启服务
    restart_services

    print_success "应用更新完成"
}

# 清理资源
cleanup() {
    print_info "清理未使用的 Docker 资源..."
    docker system prune -f
    print_success "清理完成"
}

# 打印使用说明
print_usage() {
    cat << EOF
使用方式: $0 [命令]

命令:
    start               启动所有服务
    stop                停止所有服务
    restart             重启所有服务
    status              显示服务状态
    logs [service]      查看日志 (可选指定服务: backend, frontend, mysql, redis)
    backup              备份数据库
    update              更新应用 (拉取代码、重新构建、重启)
    cleanup             清理未使用的 Docker 资源
    help                显示此帮助信息

示例:
    $0 start                启动服务
    $0 logs backend         查看后端日志
    $0 status               查看服务状态

EOF
}

# 主逻辑
case "${1:-}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "${2:-}"
        ;;
    backup)
        backup_database
        ;;
    update)
        update_app
        ;;
    cleanup)
        cleanup
        ;;
    help|--help|-h)
        print_usage
        ;;
    *)
        print_error "未知命令: ${1:-}"
        echo ""
        print_usage
        exit 1
        ;;
esac
