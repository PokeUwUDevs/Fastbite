# --- ETAPA 1: Build ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copiar TODO el monorepo
COPY . .

# Permisos para Gradle
RUN chmod +x gradlew

# Compilar solo restaurant-service
RUN ./gradlew :services:restaurant-service:bootJar --no-daemon -x test

# --- ETAPA 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# curl para healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copiar el JAR generado
COPY --from=builder /app/services/restaurant-service/build/libs/*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
