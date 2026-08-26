#!/bin/bash
set -e

APP_DIR="/home/ec2-user/app"
cd "$APP_DIR"

ACTIVE_CONF="/etc/nginx/conf.d/service.conf"

if grep -q "127.0.0.1:8081" "$ACTIVE_CONF" 2>/dev/null; then
  CURRENT=green
  TARGET=blue
  TARGET_PORT=8080
else
  CURRENT=blue
  TARGET=green
  TARGET_PORT=8081
fi

echo "### $CURRENT -> $TARGET (port $TARGET_PORT) ###"

echo "1. pull $TARGET image"
docker compose pull "$TARGET"

echo "2. start $TARGET container"
docker compose up -d "$TARGET"

echo "3. health check ($TARGET, port $TARGET_PORT)"
READY=false
for i in $(seq 1 20); do
  sleep 3
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${TARGET_PORT}/actuator/health" || echo "000")
  echo "   attempt $i/20: HTTP $STATUS"
  if [ "$STATUS" = "200" ]; then
    READY=true
    break
  fi
done

if [ "$READY" != "true" ]; then
  echo "### health check FAILED: rolling back, $CURRENT stays live, stopping $TARGET ###"
  docker compose stop "$TARGET"
  exit 1
fi

echo "4. switch nginx to $TARGET"
sudo cp "/etc/nginx/conf.d/service.conf.${TARGET}" "$ACTIVE_CONF"
sudo nginx -t
sudo nginx -s reload

echo "5. stop $CURRENT container"
docker compose stop "$CURRENT"

echo "### deploy complete: now serving $TARGET ###"
