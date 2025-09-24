#!/bin/sh

# 设置默认后端地址
DEFAULT_BACKEND_HOST="eveindex2-0.zeabur.internal"
DEFAULT_BACKEND_PORT="8080"

# 使用环境变量或默认值
BACKEND_HOST=${BACKEND_HOST:-$DEFAULT_BACKEND_HOST}
BACKEND_PORT=${BACKEND_PORT:-$DEFAULT_BACKEND_PORT}

echo "=== 启动配置 ==="
echo "后端地址: $BACKEND_HOST:$BACKEND_PORT"
echo "================"

# 生成 nginx 配置文件
cat > /etc/nginx/conf.d/default.conf << EOF
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header X-Content-Type-Options "nosniff" always;
    }

    # API 代理到后端服务
    location /api/ {
        proxy_pass http://$BACKEND_HOST:$BACKEND_PORT/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # 错误处理
        proxy_intercept_errors on;
        error_page 502 503 504 = @api_fallback;
        
        # CORS 头部
        add_header Access-Control-Allow-Origin "*" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Content-Type, Authorization" always;
        
        # 处理 OPTIONS 预检请求
        if (\$request_method = 'OPTIONS') {
            return 204;
        }
    }
    
    # API 服务不可用时的回退处理
    location @api_fallback {
        add_header Content-Type application/json;
        add_header Access-Control-Allow-Origin "*" always;
        return 503 '{"error": "Backend service temporarily unavailable", "message": "Please try again later", "status": 503}';
    }

    # SPA 路由支持
    location / {
        try_files \$uri \$uri/ /index.html;
        
        # 安全头
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header X-Content-Type-Options "nosniff" always;
    }

    # 健康检查端点
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # 错误页面
    error_page 404 /index.html;
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
EOF

echo "=== 生成的 Nginx 配置 ==="
cat /etc/nginx/conf.d/default.conf
echo "=========================="

# 测试 nginx 配置
echo "=== 测试 Nginx 配置 ==="
nginx -t
if [ $? -eq 0 ]; then
    echo "Nginx 配置测试通过"
    echo "=== 启动 Nginx ==="
    exec nginx -g "daemon off;"
else
    echo "Nginx 配置测试失败"
    exit 1
fi
