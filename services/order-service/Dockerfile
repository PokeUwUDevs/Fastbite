# --- ETAPA 1: Build ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copiar TODO el repo
COPY . .

RUN chmod +x gradlew

# Compilar solo el servicio
RUN ./gradlew :services:order-service:bootJar --no-daemon -x test

# --- ETAPA 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/services/order-service/build/libs/*.jar app.jar

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
