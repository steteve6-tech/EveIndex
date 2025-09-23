#!/bin/bash

# Zeabur 部署脚本
# 这个脚本帮助快速部署到 Zeabur 平台

set -e

echo "🚀 开始 Zeabur 部署流程..."

# 检查必要的工具
check_requirements() {
    echo "📋 检查部署要求..."
    
    if ! command -v git &> /dev/null; then
        echo "❌ Git 未安装，请先安装 Git"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo "⚠️  Docker 未安装，建议安装 Docker 进行本地测试"
    fi
    
    echo "✅ 环境检查完成"
}

# 准备部署文件
prepare_deployment() {
    echo "📦 准备部署文件..."
    
    # 确保所有文件都已提交
    if [ -n "$(git status --porcelain)" ]; then
        echo "⚠️  检测到未提交的更改，请先提交所有更改"
        git status
        read -p "是否继续部署？(y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    echo "✅ 部署文件准备完成"
}

# 推送代码到远程仓库
push_to_remote() {
    echo "📤 推送代码到远程仓库..."
    
    # 获取当前分支
    current_branch=$(git branch --show-current)
    
    echo "当前分支: $current_branch"
    
    # 推送到远程
    git push origin $current_branch
    
    echo "✅ 代码推送完成"
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo "🎉 部署准备完成！"
    echo ""
    echo "📋 下一步操作："
    echo "1. 访问 Zeabur Dashboard: https://dash.zeabur.com"
    echo "2. 创建新项目或选择现有项目"
    echo "3. 添加以下服务："
    echo "   - MySQL 数据库"
    echo "   - Redis 缓存"
    echo "   - 后端服务 (Spring Boot)"
    echo "   - 前端服务 (Vue.js)"
    echo ""
    echo "📖 详细部署指南请查看: ZEABUR_DEPLOYMENT.md"
    echo ""
    echo "🔧 环境变量配置："
    echo "   MYSQL_ROOT_PASSWORD=your-secure-password"
    echo "   MYSQL_USERNAME=app_user"
    echo "   MYSQL_PASSWORD=your-app-password"
    echo "   VOLCENGINE_ACCESS_KEY=your-volcengine-access-key"
    echo "   VOLCENGINE_SECRET_KEY=your-volcengine-secret-key"
    echo "   ARK_API_KEY=your-ark-api-key"
    echo ""
}

# 主函数
main() {
    check_requirements
    prepare_deployment
    push_to_remote
    show_deployment_info
}

# 运行主函数
main "$@"
