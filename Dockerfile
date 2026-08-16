# ============================================================
# PRAGMATRIX 2026 — Multi-Module Dockerfile (Public App Default)
# ============================================================

# --- Stage 1: Build Module Artifacts ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY pragmatrix-common/pom.xml pragmatrix-common/
COPY pragmatrix-public/pom.xml pragmatrix-public/
COPY pragmatrix-admin/pom.xml pragmatrix-admin/

COPY pragmatrix-common/src pragmatrix-common/src
COPY pragmatrix-public/src pragmatrix-public/src
COPY pragmatrix-admin/src pragmatrix-admin/src

RUN mvn clean package -DskipTests

# --- Stage 2: Runtime Container ---
FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*

# Disable Tomcat socket shutdown listener (port 8005 -> -1) so container uses SIGTERM only
RUN sed -i 's/port="8005"/port="-1"/g' conf/server.xml

COPY --from=builder /build/pragmatrix-public/target/pragmatrix2026.war webapps/ROOT.war

ENV JAVA_OPTS="-Xms128m -Xmx320m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

CMD ["sh", "-c", "sed -i 's/port=\"8005\"/port=\"-1\"/g' conf/server.xml && sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/g\" conf/server.xml && exec catalina.sh run"]
