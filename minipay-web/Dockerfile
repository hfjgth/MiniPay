# 构建阶段
FROM node:22-alpine AS builder

WORKDIR /app

# 复制依赖文件并安装
COPY package*.json ./
RUN npm install

# 复制源码并构建
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露前端端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]