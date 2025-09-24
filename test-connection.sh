#!/bin/bash

# 前后端连接测试脚本

echo "=== 前后端连接测试 ==="
echo "时间: $(date)"
echo ""

# 配置
FRONTEND_URL="https://eveindex.zeabur.app"
BACKEND_URL="https://your-backend-domain.zeabur.app"  # 替换为实际的后端域名

echo "=== 1. 测试前端服务 ==="
echo "前端 URL: $FRONTEND_URL"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" "$FRONTEND_URL"
echo ""

echo "=== 2. 测试后端健康检查 ==="
echo "直接访问后端: $BACKEND_URL/api/health"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" "$BACKEND_URL/api/health"
echo ""

echo "=== 3. 测试前端代理后端 ==="
echo "通过前端代理访问后端: $FRONTEND_URL/api/health"
response=$(curl -s -w "HTTP状态码: %{http_code}" "$FRONTEND_URL/api/health")
echo "$response"
echo ""

echo "=== 4. 测试设备数据统计 API ==="
echo "测试设备数据统计: $FRONTEND_URL/api/device-data/overview-statistics"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" "$FRONTEND_URL/api/device-data/overview-statistics"
echo ""

echo "=== 5. 测试美国爬虫健康状态 ==="
echo "测试美国爬虫: $FRONTEND_URL/api/us-crawler/health"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" "$FRONTEND_URL/api/us-crawler/health"
echo ""

echo "=== 6. 测试欧盟爬虫状态 ==="
echo "测试欧盟爬虫: $FRONTEND_URL/api/eu-crawler/status"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" "$FRONTEND_URL/api/eu-crawler/status"
echo ""

echo "=== 测试完成 ==="
echo "如果所有测试都返回 200 状态码，说明前后端连接正常"
echo "如果返回 503 状态码，说明后端服务未部署或未启动"
echo "如果返回 404 状态码，说明 API 路径不正确"
