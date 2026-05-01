# Postgres 18 (matches the latest available locally; aligns with the JDBC driver pinned in
# server/pom.xml). Bump in lock-step with the server module when a new minor is published.
FROM postgres:18-alpine

ENV POSTGRES_DB=mockpitdb
ENV POSTGRES_USER=mockpituser
ENV POSTGRES_PASSWORD=mockpitpassword

EXPOSE 5432
