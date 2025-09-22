#!/bin/bash

# 设备认证风险监控系统 - 监控脚本
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

# 配置
COMPOSE_FILE=docker-compose.prod.yml
COMPOSE_PROJECT_NAME=certification-monitor

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
    echo "    设备认证风险监控系统 - 监控检查"
    echo "    System Health Monitor"
    echo "    $(date '+%Y-%m-%d %H:%M:%S')"
    echo "=================================================="
    echo -e "${NC}"
}

# 检查容器状态
check_containers() {
    echo -e "\n${PURPLE}📦 容器状态检查${NC}"
    echo "=================================="
    
    local containers=("cert_mysql_prod" "cert_redis_prod" "cert_backend_prod" "cert_frontend_prod" "cert_phpmyadmin_prod")
    local all_running=true
    
    for container in "${containers[@]}"; do
        if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container"; then
            status=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep "$container" | awk '{print $2}')
            if [[ "$status" == "Up" ]]; then
                echo -e "✅ $container: ${GREEN}运行中${NC}"
            else
                echo -e "⚠️  $container: ${YELLOW}$status${NC}"
                all_running=false
            fi
        else
            echo -e "❌ $container: ${RED}未运行${NC}"
            all_running=false
        fi
    done
    
    if $all_running; then
        log_success "所有容器运行正常"
    else
        log_warning "部分容器状态异常"
    fi
}

# 检查服务健康状态
check_services() {
    echo -e "\n${PURPLE}🏥 服务健康检查${NC}"
    echo "=================================="
    
    # 检查前端服务
    echo -n "前端服务: "
    if curl -f -s http://localhost/ > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 正常${NC}"
    else
        echo -e "${RED}❌ 异常${NC}"
    fi
    
    # 检查后端服务
    echo -n "后端服务: "
    if curl -f -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 正常${NC}"
        # 获取健康检查详情
        health_info=$(curl -s http://localhost:8080/api/health 2>/dev/null | jq -r '.status' 2>/dev/null || echo "unknown")
        echo "  状态: $health_info"
    else
        echo -e "${RED}❌ 异常${NC}"
    fi
    
    # 检查数据库服务
    echo -n "数据库服务: "
    if docker-compose -f $COMPOSE_FILE exec -T mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD:-password123} --silent 2>/dev/null; then
        echo -e "${GREEN}✅ 正常${NC}"
    else
        echo -e "${RED}❌ 异常${NC}"
    fi
    
    # 检查Redis服务
    echo -n "Redis服务: "
    if docker-compose -f $COMPOSE_FILE exec -T redis redis-cli ping > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 正常${NC}"
    else
        echo -e "${RED}❌ 异常${NC}"
    fi
    
    # 检查phpMyAdmin
    echo -n "phpMyAdmin: "
    if curl -f -s http://localhost:8081/ > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 正常${NC}"
    else
        echo -e "${YELLOW}⚠️  不可用${NC}"
    fi
}

# 检查资源使用情况
check_resources() {
    echo -e "\n${PURPLE}📊 资源使用情况${NC}"
    echo "=================================="
    
    echo "Docker容器资源使用:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" | head -6
    
    echo -e "\n系统资源使用:"
    echo "CPU使用率: $(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | awk -F'%' '{print $1}')%"
    echo "内存使用: $(free -h | awk 'NR==2{printf "%.1f/%.1f GB (%.1f%%)", $3/1024/1024, $2/1024/1024, $3*100/$2}')"
    echo "磁盘使用: $(df -h / | awk 'NR==2{printf "%s/%s (%s)", $3, $2, $5}')"
}

# 检查网络连接
check_network() {
    echo -e "\n${PURPLE}🌐 网络连接检查${NC}"
    echo "=================================="
    
    # 检查端口是否开放
    local ports=("80:前端" "8080:后端API" "3306:MySQL" "6379:Redis" "8081:phpMyAdmin")
    
    for port_info in "${ports[@]}"; do
        IFS=':' read -ra ADDR <<< "$port_info"
        port=${ADDR[0]}
        service=${ADDR[1]}
        
        if netstat -tuln | grep -q ":$port "; then
            echo -e "✅ 端口 $port ($service): ${GREEN}开放${NC}"
        else
            echo -e "❌ 端口 $port ($service): ${RED}未开放${NC}"
        fi
    done
}

# 检查日志
check_logs() {
    echo -e "\n${PURPLE}📋 最近日志检查${NC}"
    echo "=================================="
    
    echo "后端服务最近错误日志:"
    docker-compose -f $COMPOSE_FILE logs --tail=5 backend 2>/dev/null | grep -i error || echo "无错误日志"
    
    echo -e "\n前端服务最近日志:"
    docker-compose -f $COMPOSE_FILE logs --tail=3 frontend 2>/dev/null || echo "无日志"
    
    echo -e "\n数据库最近日志:"
    docker-compose -f $COMPOSE_FILE logs --tail=3 mysql 2>/dev/null | grep -v "mysqld: ready for connections" || echo "无异常日志"
}

# 检查磁盘空间
check_disk_space() {
    echo -e "\n${PURPLE}💾 磁盘空间检查${NC}"
    echo "=================================="
    
    # 检查根分区
    root_usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$root_usage" -gt 90 ]; then
        echo -e "❌ 根分区使用率: ${RED}${root_usage}%${NC} (超过90%)"
    elif [ "$root_usage" -gt 80 ]; then
        echo -e "⚠️  根分区使用率: ${YELLOW}${root_usage}%${NC} (超过80%)"
    else
        echo -e "✅ 根分区使用率: ${GREEN}${root_usage}%${NC}"
    fi
    
    # 检查Docker卷使用情况
    echo -e "\nDocker卷使用情况:"
    docker system df
    
    # 检查日志文件大小
    if [ -d "logs" ]; then
        log_size=$(du -sh logs 2>/dev/null | awk '{print $1}')
        echo "应用日志大小: $log_size"
    fi
}

# 性能测试
performance_test() {
    echo -e "\n${PURPLE}⚡ 性能测试${NC}"
    echo "=================================="
    
    # 测试后端API响应时间
    if curl -f -s http://localhost:8080/api/health > /dev/null 2>&1; then
        response_time=$(curl -o /dev/null -s -w "%{time_total}" http://localhost:8080/api/health)
        echo "后端API响应时间: ${response_time}s"
        
        if (( $(echo "$response_time > 2.0" | bc -l) )); then
            echo -e "⚠️  ${YELLOW}响应时间较慢${NC}"
        else
            echo -e "✅ ${GREEN}响应时间正常${NC}"
        fi
    fi
    
    # 测试前端响应时间
    if curl -f -s http://localhost/ > /dev/null 2>&1; then
        frontend_time=$(curl -o /dev/null -s -w "%{time_total}" http://localhost/)
        echo "前端响应时间: ${frontend_time}s"
    fi
}

# 生成报告
generate_report() {
    echo -e "\n${PURPLE}📄 监控报告${NC}"
    echo "=================================="
    
    local report_file="monitoring_report_$(date +%Y%m%d_%H%M%S).txt"
    
    {
        echo "设备认证风险监控系统 - 监控报告"
        echo "生成时间: $(date)"
        echo "=================================="
        echo ""
        
        echo "容器状态:"
        docker-compose -f $COMPOSE_FILE ps
        echo ""
        
        echo "资源使用:"
        docker stats --no-stream
        echo ""
        
        echo "系统信息:"
        echo "CPU: $(nproc) 核心"
        echo "内存: $(free -h | awk 'NR==2{print $2}')"
        echo "磁盘: $(df -h / | awk 'NR==2{print $2}')"
        echo ""
        
        echo "最近错误日志:"
        docker-compose -f $COMPOSE_FILE logs --tail=10 backend | grep -i error || echo "无错误日志"
        
    } > "$report_file"
    
    echo "监控报告已生成: $report_file"
}

# 主函数
main() {
    show_banner
    
    # 加载环境变量
    if [ -f .env.prod ]; then
        set -a
        source .env.prod
        set +a
    fi
    
    check_containers
    check_services
    check_resources
    check_network
    check_disk_space
    check_logs
    performance_test
    
    echo -e "\n${GREEN}监控检查完成！${NC}"
    
    # 询问是否生成详细报告
    read -p "是否生成详细监控报告? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        generate_report
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
