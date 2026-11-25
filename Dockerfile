# Build the app first on the host (thin jar + external deps):
#   ./mvnw clean package -DskipTests
# Then:
#   docker build -t superconductor:1.7.4 .
# Or:
#   docker compose build

ARG APP_VERSION=1.7.4

FROM eclipse-temurin:22-jre-jammy

ARG APP_VERSION
ENV APP_VERSION=${APP_VERSION}

WORKDIR /app

COPY target/superconductor-${APP_VERSION}.jar /app/app.jar
COPY target/lib /app/lib

EXPOSE 5555

ENTRYPOINT ["java", "-Dloader.path=/app/lib", "-jar", "/app/app.jar"]
