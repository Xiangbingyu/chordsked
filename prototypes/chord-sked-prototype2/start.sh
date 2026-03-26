#!/bin/zsh
set -e  

IMAGE_NAME="chordsked"
CONTAINER_NAME="chordsked-app"
HOST_PORT=3000
CONTAINER_PORT=3000
NODE_VERSION="22.22.1"

echo "清理旧容器..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true


echo "安装依赖..."
docker run --privileged --rm \
  -v "$(pwd)":/app \
  -w /app \
  node:$NODE_VERSION \
  npm install --legacy-peer-deps

docker build -t "$IMAGE_NAME" .

echo "启动容器..."
docker run --privileged -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p "$HOST_PORT":"$CONTAINER_PORT" \
  "$IMAGE_NAME"