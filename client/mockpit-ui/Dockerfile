# syntax=docker/dockerfile:1.7
# Multi-stage build for the Mockpit Angular client. Build is done with a pinned Node 22 LTS image
# (matches the local toolchain on the dev machine - 22.22.0); runtime is nginx-alpine with non-root user.

############################
# 1. Build
############################
FROM node:22-alpine AS build
WORKDIR /workspace

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
ARG configuration=production
RUN npm run build -- --configuration=$configuration --output-path=/workspace/dist

############################
# 2. Runtime
############################
FROM nginx:1.27-alpine

RUN apk add --no-cache gettext \
    && rm -rf /usr/share/nginx/html/*

COPY --from=build /workspace/dist /usr/share/nginx/html/
COPY ./entryPoint.sh /entryPoint.sh
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
RUN chmod +x /entryPoint.sh

EXPOSE 80

USER nginx

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1

ENTRYPOINT ["sh", "/entryPoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
