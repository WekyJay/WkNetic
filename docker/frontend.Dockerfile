# ==================== Stage 1: 构建阶段 ====================
FROM node:22-alpine AS builder

WORKDIR /app

# 【关键点 1】启用 pnpm
# Node.js 16.13+ 自带 Corepack，可以直接启用 pnpm，无需 npm install -g pnpm
RUN corepack enable

# 设置淘宝镜像源 (pnpm 也需要设置，或者写在 .npmrc 里拷进去)
RUN npm config set registry https://registry.npmmirror.com

# 【关键点 2】拷贝 pnpm-lock.yaml
# 注意：这里假设你的 dockerfile 在 docker/ 目录下，构建上下文是根目录
# 所以路径要带上 wknetic-frontend/
COPY wknetic-frontend/package.json wknetic-frontend/pnpm-lock.yaml ./

# 【关键点 3】安装依赖
# --frozen-lockfile 等同于 npm ci，确保安装版本与 lock 文件完全一致
RUN pnpm install --frozen-lockfile

# 拷贝源码并编译
COPY wknetic-frontend/ .
RUN pnpm run buildSkipTsc

# ==================== Stage 2: 运行阶段 (Nginx) ====================
# Nginx 阶段完全不需要变，因为它只负责运行静态文件
FROM nginx:alpine

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 清理默认文件
RUN rm -rf /usr/share/nginx/html/*

# 拷贝构建产物 (dist)
COPY --from=builder /app/dist /usr/share/nginx/html

# 拷贝 Nginx 配置
# 假设你也把 nginx.conf 放在了 docker/ 目录下，如果还在 frontend 目录下，请改为 wknetic-frontend/nginx.conf
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]