#!/bin/zsh
set -e

IMAGE_NAME="chordsked"
CONTAINER_NAME="chordsked-app"
HOST_PORT=3000
CONTAINER_PORT=80         
NODE_VERSION="22.22.1"

docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

docker run --privileged  --rm \
  -v "$(pwd)":/app \
  -w /app \
  node:$NODE_VERSION \
  sh -c "npm install --legacy-peer-deps && npm run build"

docker run --privileged -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p "$HOST_PORT":"$CONTAINER_PORT" \
  -v "$(pwd)/dist":/usr/share/nginx/html:ro \
  nginx:alpine
