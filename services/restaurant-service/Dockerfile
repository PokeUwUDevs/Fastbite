# --- ETAPA 1: Construcción ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copiar archivos de Gradle
COPY gradlew .
COPY gradle gradle/
COPY settings.gradle .
COPY build.gradle .

# Copiar shared si existe
COPY shared shared/

# Copiar código del servicio
COPY services/restaurant-service services/restaurant-service/

# Dar permisos
RUN chmod +x gradlew

# Compilar
RUN ./gradlew :services:restaurant-service:bootJar --no-daemon -x test

# --- ETAPA 2: Ejecución ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copiar JAR
COPY --from=builder /app/services/restaurant-service/build/libs/*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]