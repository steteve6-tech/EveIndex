#!/bin/bash

# ================================
# Docker 镜像构建和部署脚本
# ================================
# 用途: 自动构建 Docker 镜像并推送到容器仓库
# 使用方式: ./docker-build.sh [选项]

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置项
PROJECT_NAME="certification-monitor"
VERSION=${VERSION:-"1.0.0"}
REGISTRY=${DOCKER_REGISTRY:-""}  # 容器仓库地址，如: registry.example.com
NAMESPACE=${DOCKER_NAMESPACE:-"cert"}  # 命名空间

# 镜像标签
BACKEND_IMAGE="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-backend:${VERSION}"
FRONTEND_IMAGE="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-frontend:${VERSION}"
BACKEND_LATEST="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-backend:latest"
FRONTEND_LATEST="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-frontend:latest"

# 打印带颜色的消息
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

# 打印使用说明
print_usage() {
    cat << EOF
使用方式: $0 [选项]

选项:
    -h, --help              显示此帮助信息
    -v, --version VERSION   指定版本号 (默认: 1.0.0)
    -r, --registry URL      指定容器仓库地址
    -n, --namespace NAME    指定命名空间 (默认: cert)
    -b, --backend-only      仅构建后端镜像
    -f, --frontend-only     仅构建前端镜像
    -p, --push              构建后推送到仓库
    --no-cache              构建时不使用缓存
    --clean                 构建前清理旧镜像

示例:
    # 构建所有镜像
    $0

    # 构建并推送到仓库
    $0 --push --registry registry.example.com

    # 仅构建后端，指定版本
    $0 --backend-only --version 2.0.0

环境变量:
    VERSION             镜像版本号
    DOCKER_REGISTRY     容器仓库地址
    DOCKER_NAMESPACE    命名空间

EOF
}

# 解析命令行参数
BUILD_BACKEND=true
BUILD_FRONTEND=true
PUSH_IMAGE=false
NO_CACHE=""
CLEAN_IMAGES=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            print_usage
            exit 0
            ;;
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -b|--backend-only)
            BUILD_FRONTEND=false
            shift
            ;;
        -f|--frontend-only)
            BUILD_BACKEND=false
            shift
            ;;
        -p|--push)
            PUSH_IMAGE=true
            shift
            ;;
        --no-cache)
            NO_CACHE="--no-cache"
            shift
            ;;
        --clean)
            CLEAN_IMAGES=true
            shift
            ;;
        *)
            print_error "未知选项: $1"
            print_usage
            exit 1
            ;;
    esac
done

# 更新镜像标签
BACKEND_IMAGE="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-backend:${VERSION}"
FRONTEND_IMAGE="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-frontend:${VERSION}"
BACKEND_LATEST="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-backend:latest"
FRONTEND_LATEST="${REGISTRY:+$REGISTRY/}${NAMESPACE}/${PROJECT_NAME}-frontend:latest"

# 打印配置信息
print_info "================================"
print_info "Docker 构建配置"
print_info "================================"
print_info "项目名称: ${PROJECT_NAME}"
print_info "版本号: ${VERSION}"
print_info "仓库地址: ${REGISTRY:-本地构建}"
print_info "命名空间: ${NAMESPACE}"
print_info "构建后端: ${BUILD_BACKEND}"
print_info "构建前端: ${BUILD_FRONTEND}"
print_info "推送镜像: ${PUSH_IMAGE}"
print_info "================================"
echo ""

# 检查 .env.prod 文件
if [ ! -f ".env.prod" ]; then
    print_warning ".env.prod 文件不存在"
    print_info "请复制 .env.prod.example 并配置生产环境变量"
    read -p "是否继续构建? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "构建已取消"
        exit 0
    fi
fi

# 清理旧镜像
if [ "$CLEAN_IMAGES" = true ]; then
    print_info "清理旧镜像..."
    docker images | grep "${PROJECT_NAME}" | awk '{print $3}' | xargs -r docker rmi -f || true
    print_success "旧镜像已清理"
fi

# 构建后端镜像
if [ "$BUILD_BACKEND" = true ]; then
    print_info "开始构建后端镜像..."
    print_info "镜像标签: ${BACKEND_IMAGE}"

    docker build ${NO_CACHE} \
        -t "${BACKEND_IMAGE}" \
        -t "${BACKEND_LATEST}" \
        -f spring-boot-backend/Dockerfile \
        spring-boot-backend/

    print_success "后端镜像构建完成"
    echo ""
fi

# 构建前端镜像
if [ "$BUILD_FRONTEND" = true ]; then
    print_info "开始构建前端镜像..."
    print_info "镜像标签: ${FRONTEND_IMAGE}"

    docker build ${NO_CACHE} \
        -t "${FRONTEND_IMAGE}" \
        -t "${FRONTEND_LATEST}" \
        -f vue-frontend/Dockerfile \
        vue-frontend/

    print_success "前端镜像构建完成"
    echo ""
fi

# 推送镜像到仓库
if [ "$PUSH_IMAGE" = true ]; then
    if [ -z "$REGISTRY" ]; then
        print_error "未指定容器仓库地址，无法推送镜像"
        print_info "请使用 --registry 参数指定仓库地址"
        exit 1
    fi

    print_info "开始推送镜像到仓库..."

    # 登录容器仓库（如果需要）
    # docker login ${REGISTRY}

    if [ "$BUILD_BACKEND" = true ]; then
        print_info "推送后端镜像: ${BACKEND_IMAGE}"
        docker push "${BACKEND_IMAGE}"
        docker push "${BACKEND_LATEST}"
        print_success "后端镜像推送完成"
    fi

    if [ "$BUILD_FRONTEND" = true ]; then
        print_info "推送前端镜像: ${FRONTEND_IMAGE}"
        docker push "${FRONTEND_IMAGE}"
        docker push "${FRONTEND_LATEST}"
        print_success "前端镜像推送完成"
    fi

    echo ""
fi

# 显示构建的镜像
print_info "构建的镜像列表:"
docker images | grep "${PROJECT_NAME}" | head -10

echo ""
print_success "所有任务完成！"
print_info "================================"
print_info "后续步骤:"
print_info "1. 配置 .env.prod 文件中的环境变量"
print_info "2. 在服务器上创建 .env.prod 文件"
print_info "3. 运行: docker-compose -f docker-compose.prod.yml up -d"
print_info "================================"
