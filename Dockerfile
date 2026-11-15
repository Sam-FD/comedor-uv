FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache nginx gettext
COPY --from=builder /build/target/*.jar /app/app.jar
COPY docker/nginx.conf.template /app/nginx.conf.template
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r$//' /app/entrypoint.sh /app/nginx.conf.template && chmod +x /app/entrypoint.sh

EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
