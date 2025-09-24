# 测试版 Dockerfile for Zeabur
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci

# 复制所有源代码文件
COPY . .

# 设置环境变量
ENV NODE_ENV=production
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL:-/api}

# 确保 public 目录存在（如果为空的话）
RUN mkdir -p public/

# 验证文件是否正确复制
RUN ls -la
RUN echo "=== 检查 index.html ==="
RUN ls -la index.html || echo "index.html 不存在"
RUN echo "=== 检查 src 目录 ==="
RUN ls -la src/ || echo "src 目录不存在"
RUN echo "=== 检查 public 目录 ==="
RUN ls -la public/ || echo "public 目录不存在"

# 使用 npm script 构建
RUN npm run build

# 验证构建结果
RUN ls -la dist/ && echo "=== 构建成功 ==="

# 生产阶段
FROM nginx:alpine

# 安装 wget 用于健康检查
RUN apk add --no-cache wget

# 复制 Nginx 配置（根据环境变量选择配置）
COPY nginx.zeabur.conf /etc/nginx/nginx.conf
COPY nginx.standalone.conf /etc/nginx/nginx.standalone.conf

# 创建启动脚本
RUN echo '#!/bin/sh' > /docker-entrypoint.sh && \
    echo 'if [ "$STANDALONE_MODE" = "true" ]; then' >> /docker-entrypoint.sh && \
    echo '  echo "Starting in standalone mode (no backend)"' >> /docker-entrypoint.sh && \
    echo '  cp /etc/nginx/nginx.standalone.conf /etc/nginx/nginx.conf' >> /docker-entrypoint.sh && \
    echo 'else' >> /docker-entrypoint.sh && \
    echo '  echo "Starting with backend proxy"' >> /docker-entrypoint.sh && \
    echo 'fi' >> /docker-entrypoint.sh && \
    echo 'exec nginx -g "daemon off;"' >> /docker-entrypoint.sh && \
    chmod +x /docker-entrypoint.sh

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 验证文件复制
RUN ls -la /usr/share/nginx/html/ && echo "=== 文件复制成功 ==="

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
