#!/bin/bash

# 设备认证风险监控系统 - Docker部署脚本
# 版本: 1.0.0
# 作者: System Administrator

set -e  # 遇到错误立即退出

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

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 显示横幅
show_banner() {
    echo -e "${CYAN}"
    echo "=================================================="
    echo "    设备认证风险监控系统 Docker 部署脚本"
    echo "    Device Certification Risk Monitor"
    echo "    Version: 1.0.0"
    echo "=================================================="
    echo -e "${NC}"
}

# 检查系统要求
check_requirements() {
    log_step "检查系统要求..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        echo "安装命令: curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh"
        exit 1
    fi
    log_success "Docker已安装: $(docker --version)"

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        echo "安装命令: sudo curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)\" -o /usr/local/bin/docker-compose"
        echo "然后执行: sudo chmod +x /usr/local/bin/docker-compose"
        exit 1
    fi
    log_success "Docker Compose已安装: $(docker-compose --version)"

    # 检查可用磁盘空间 (至少需要5GB)
    available_space=$(df / | awk 'NR==2 {print $4}')
    if [ "$available_space" -lt 5242880 ]; then  # 5GB in KB
        log_warning "可用磁盘空间不足5GB，建议清理磁盘空间"
    fi

    # 检查内存 (至少需要2GB)
    available_memory=$(free -m | awk 'NR==2{printf "%.0f", $7}')
    if [ "$available_memory" -lt 2048 ]; then
        log_warning "可用内存不足2GB，可能影响性能"
    fi

    log_success "系统要求检查完成"
}

# 设置环境变量
setup_environment() {
    log_step "设置环境变量..."
    
    export COMPOSE_FILE=docker-compose.prod.yml
    export COMPOSE_PROJECT_NAME=certification-monitor
    
    # 检查环境变量文件
    if [ ! -f .env.prod ]; then
        if [ -f docker-env-example.txt ]; then
            log_warning ".env.prod文件不存在，正在创建示例文件..."
            cp docker-env-example.txt .env.prod
            log_warning "请编辑.env.prod文件，填入真实的配置信息"
            echo "配置文件位置: $(pwd)/.env.prod"
            read -p "是否现在编辑配置文件? (y/n): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                ${EDITOR:-nano} .env.prod
            else
                log_error "请先配置.env.prod文件，然后重新运行部署脚本"
                exit 1
            fi
        else
            log_error "找不到环境变量配置文件，请创建.env.prod文件"
            exit 1
        fi
    fi
    
    # 加载环境变量
    set -a  # 自动导出变量
    source .env.prod
    set +a
    
    log_success "环境变量设置完成"
}

# 创建必要的目录
create_directories() {
    log_step "创建必要的目录..."
    
    mkdir -p logs
    mkdir -p database
    mkdir -p ssl
    mkdir -p nginx
    mkdir -p backups
    
    log_success "目录创建完成"
}

# 备份现有数据
backup_data() {
    log_step "备份现有数据..."
    
    if docker ps | grep -q cert_mysql_prod; then
        log_info "发现运行中的MySQL容器，正在备份数据..."
        backup_file="backups/mysql_backup_$(date +%Y%m%d_%H%M%S).sql"
        docker exec cert_mysql_prod mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > "$backup_file" 2>/dev/null || true
        if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
            log_success "数据库备份完成: $backup_file"
        else
            log_warning "数据库备份失败或为空"
        fi
    fi
}

# 构建Docker镜像
build_images() {
    log_step "构建Docker镜像..."
    
    log_info "构建后端镜像..."
    docker-compose -f $COMPOSE_FILE build --no-cache backend
    
    log_info "构建前端镜像..."
    docker-compose -f $COMPOSE_FILE build --no-cache frontend
    
    log_success "Docker镜像构建完成"
}

# 停止现有服务
stop_services() {
    log_step "停止现有服务..."
    
    if docker-compose -f $COMPOSE_FILE ps -q | grep -q .; then
        log_info "正在停止现有服务..."
        docker-compose -f $COMPOSE_FILE down --remove-orphans
        log_success "现有服务已停止"
    else
        log_info "没有运行中的服务"
    fi
}

# 清理Docker资源
cleanup_docker() {
    log_step "清理Docker资源..."
    
    log_info "清理未使用的镜像..."
    docker image prune -f
    
    log_info "清理未使用的容器..."
    docker container prune -f
    
    log_info "清理未使用的网络..."
    docker network prune -f
    
    log_success "Docker资源清理完成"
}

# 启动服务
start_services() {
    log_step "启动服务..."
    
    log_info "启动数据库和缓存服务..."
    docker-compose -f $COMPOSE_FILE up -d mysql redis
    
    log_info "等待数据库启动..."
    for i in {1..30}; do
        if docker-compose -f $COMPOSE_FILE exec -T mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} --silent; then
            log_success "数据库启动成功"
            break
        else
            log_info "等待数据库启动... ($i/30)"
            sleep 10
        fi
        
        if [ $i -eq 30 ]; then
            log_error "数据库启动超时"
            docker-compose -f $COMPOSE_FILE logs mysql
            exit 1
        fi
    done
    
    log_info "启动后端服务..."
    docker-compose -f $COMPOSE_FILE up -d backend
    
    log_info "等待后端服务启动..."
    for i in {1..20}; do
        if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
            log_success "后端服务启动成功"
            break
        else
            log_info "等待后端服务启动... ($i/20)"
            sleep 15
        fi
        
        if [ $i -eq 20 ]; then
            log_error "后端服务启动超时"
            docker-compose -f $COMPOSE_FILE logs backend
            exit 1
        fi
    done
    
    log_info "启动前端服务..."
    docker-compose -f $COMPOSE_FILE up -d frontend
    
    log_info "启动管理工具..."
    docker-compose -f $COMPOSE_FILE up -d phpmyadmin
    
    log_success "所有服务启动完成"
}

# 检查服务状态
check_services() {
    log_step "检查服务状态..."
    
    echo -e "\n${CYAN}=== 容器状态 ===${NC}"
    docker-compose -f $COMPOSE_FILE ps
    
    echo -e "\n${CYAN}=== 服务健康检查 ===${NC}"
    
    # 检查前端服务
    if curl -f http://localhost/ > /dev/null 2>&1; then
        log_success "前端服务: 正常运行"
    else
        log_error "前端服务: 异常"
    fi
    
    # 检查后端服务
    if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
        log_success "后端服务: 正常运行"
    else
        log_error "后端服务: 异常"
    fi
    
    # 检查数据库服务
    if docker-compose -f $COMPOSE_FILE exec -T mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} --silent; then
        log_success "数据库服务: 正常运行"
    else
        log_error "数据库服务: 异常"
    fi
    
    # 检查Redis服务
    if docker-compose -f $COMPOSE_FILE exec -T redis redis-cli auth ${REDIS_PASSWORD} ping > /dev/null 2>&1; then
        log_success "Redis服务: 正常运行"
    else
        log_error "Redis服务: 异常"
    fi
}

# 显示部署信息
show_deployment_info() {
    log_step "部署信息"
    
    echo -e "\n${GREEN}🎉 部署完成！${NC}\n"
    
    echo -e "${CYAN}=== 访问地址 ===${NC}"
    echo -e "📱 前端应用: ${GREEN}http://localhost${NC} 或 ${GREEN}http://$(hostname -I | awk '{print $1}')${NC}"
    echo -e "🔧 后端API: ${GREEN}http://localhost:8080/api${NC}"
    echo -e "📊 API文档: ${GREEN}http://localhost:8080/api/doc.html${NC}"
    echo -e "🗄️  数据库管理: ${GREEN}http://localhost:8081${NC} (phpMyAdmin)"
    
    echo -e "\n${CYAN}=== 数据库信息 ===${NC}"
    echo -e "数据库: ${MYSQL_DATABASE}"
    echo -e "用户名: ${MYSQL_USER}"
    echo -e "密码: ${MYSQL_PASSWORD}"
    
    echo -e "\n${CYAN}=== 常用命令 ===${NC}"
    echo -e "查看服务状态: ${YELLOW}docker-compose -f $COMPOSE_FILE ps${NC}"
    echo -e "查看日志: ${YELLOW}docker-compose -f $COMPOSE_FILE logs -f [service-name]${NC}"
    echo -e "重启服务: ${YELLOW}docker-compose -f $COMPOSE_FILE restart [service-name]${NC}"
    echo -e "停止所有服务: ${YELLOW}docker-compose -f $COMPOSE_FILE down${NC}"
    echo -e "进入容器: ${YELLOW}docker exec -it [container-name] bash${NC}"
    
    echo -e "\n${CYAN}=== 监控脚本 ===${NC}"
    echo -e "运行监控检查: ${YELLOW}./monitor.sh${NC}"
    
    echo -e "\n${GREEN}部署成功完成！${NC}"
}

# 主函数
main() {
    show_banner
    check_requirements
    setup_environment
    create_directories
    backup_data
    stop_services
    cleanup_docker
    build_images
    start_services
    sleep 10  # 等待服务完全启动
    check_services
    show_deployment_info
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
