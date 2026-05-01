#!/bin/sh
set -eu

# Render the SPA runtime config from env vars (e.g. backendUrl) before nginx starts serving it.
if [ -f /usr/share/nginx/html/assets/config/config.template.json ]; then
    envsubst < /usr/share/nginx/html/assets/config/config.template.json \
        > /usr/share/nginx/html/assets/config/config.json
fi

# Run nginx in the background so we can also start the JVM in the same container.
nginx -g 'daemon off;' &
NGINX_PID=$!

# Forward signals to both processes so docker stop terminates cleanly.
trap "kill -TERM ${NGINX_PID} 2>/dev/null || true; exit 0" INT TERM

# JVM tuning. JMX is intentionally disabled - the pre-2.0 entrypoint exposed it on port 9010 with
# no authentication or TLS, which is a remote-code-execution path. If you need JMX in production
# wire it through a secured port and JAAS auth.
JAVA_OPTS_DEFAULT="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

exec java ${JAVA_OPTS:-${JAVA_OPTS_DEFAULT}} -jar /app/mockpit-server.jar
