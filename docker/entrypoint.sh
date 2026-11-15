#!/bin/sh
set -eu

export PORT="${PORT:-10000}"
mkdir -p /tmp/client_temp /tmp/proxy_temp /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp
envsubst '${PORT}' < /app/nginx.conf.template > /etc/nginx/nginx.conf

nginx

exec java -XX:MaxRAMPercentage=70.0 -jar /app/app.jar \
  --server.port=18080 \
  --comedor.socket.host=127.0.0.1 \
  --comedor.socket.port=19092
