#!/bin/bash
envsubst < /usr/share/nginx/html/assets/config/config.template.json > /usr/share/nginx/html/assets/config/config.json
nginx -g 'daemon off;' &
java -jar /app/mockpit-server.jar