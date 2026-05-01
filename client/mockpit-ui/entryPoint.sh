#!/bin/sh
set -eu
TEMPLATE=/usr/share/nginx/html/assets/config/config.template.json
TARGET=/usr/share/nginx/html/assets/config/config.json
if [ -f "$TEMPLATE" ]; then
    envsubst < "$TEMPLATE" > "$TARGET"
fi
exec nginx -g 'daemon off;'
