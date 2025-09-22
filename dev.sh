#!/bin/bash

# 设备认证风险监控系统 - 开发环境管理脚本
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示横幅
show_banner() {
    echo -e "${CYAN}"
    echo "=================================================="
    echo "    设备认证风险监控系统 - 开发环境管理"
    echo "    Development Environment Manager"
    echo "    Version: 1.0.0"
    echo "=================================================="
    echo -e "${NC}"
}

# 启动开发环境
start_dev() {
    log_info "启动开发环境..."
    
    # 加载环境变量
    if [ -f .env.dev ]; then
        export $(cat .env.dev | grep -v '#' | xargs)
    fi
    
    # 启动服务
    docker-compose -f docker-compose.dev.yml up -d
    
    log_success "开发环境启动成功！"
    log_info "前端地址: http://localhost:3100"
    log_info "后端API: http://localhost:8080/api"
    log_info "数据库管理: http://localhost:8081"
}

# 停止开发环境
stop_dev() {
    log_info "停止开发环境..."
    docker-compose -f docker-compose.dev.yml down
    log_success "开发环境已停止"
}

# 重启开发环境
restart_dev() {
    log_info "重启开发环境..."
    docker-compose -f docker-compose.dev.yml restart
    log_success "开发环境已重启"
}

# 查看日志
logs_dev() {
    if [ -z "$1" ]; then
        docker-compose -f docker-compose.dev.yml logs -f
    else
        docker-compose -f docker-compose.dev.yml logs -f "$1"
    fi
}

# 查看状态
status_dev() {
    echo -e "${CYAN}=== 开发环境状态 ===${NC}"
    docker-compose -f docker-compose.dev.yml ps
}

# 清理环境
clean_dev() {
    log_warning "清理开发环境（将删除所有数据）..."
    read -p "确认清理? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose -f docker-compose.dev.yml down -v
        docker system prune -f
        log_success "开发环境已清理"
    else
        log_info "取消清理操作"
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 {start|stop|restart|logs|status|clean|help}"
    echo ""
    echo "命令说明:"
    echo "  start   - 启动开发环境"
    echo "  stop    - 停止开发环境"
    echo "  restart - 重启开发环境"
    echo "  logs    - 查看日志 (可指定服务名)"
    echo "  status  - 查看服务状态"
    echo "  clean   - 清理开发环境"
    echo "  help    - 显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start              # 启动开发环境"
    echo "  $0 logs backend-dev   # 查看后端日志"
    echo "  $0 logs frontend-dev  # 查看前端日志"
}

# 主函数
main() {
    show_banner
    
    case "$1" in
        start)
            start_dev
            ;;
        stop)
            stop_dev
            ;;
        restart)
            restart_dev
            ;;
        logs)
            logs_dev "$2"
            ;;
        status)
            status_dev
            ;;
        clean)
            clean_dev
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
