# 简化版 Dockerfile for Zeabur - 避免启动脚本问题
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

# 使用 npm script 构建
RUN npm run build

# 验证构建结果
RUN ls -la dist/ && echo "=== 构建成功 ==="

# 生产阶段
FROM nginx:alpine

# 安装 wget 用于健康检查
RUN apk add --no-cache wget

# 复制独立的 Nginx 配置（直接使用，避免启动脚本问题）
COPY nginx.standalone.conf /etc/nginx/nginx.conf

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 验证文件复制
RUN ls -la /usr/share/nginx/html/ && echo "=== 文件复制成功 ==="
RUN echo "=== 检查 index.html 内容 ===" && head -5 /usr/share/nginx/html/index.html || echo "index.html 不存在或为空"
RUN echo "=== 检查 dist 目录结构 ===" && find /usr/share/nginx/html -type f -name "*.html" -o -name "*.js" -o -name "*.css" | head -10

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
