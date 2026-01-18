# --- ETAPA 1: Construcción (Builder) ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew .
COPY gradle gradle/
COPY settings.gradle .
COPY build.gradle .

# # Copiar código fuente de shared (si existe)
# COPY shared shared/

# Copiar código fuente del servicio
COPY services/user-service services/user-service/

# Dar permisos de ejecución
RUN chmod +x gradlew

# Compilar solo este servicio
RUN ./gradlew :services:user-service:bootJar --no-daemon -x test

# --- ETAPA 2: Ejecución (Runner) ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiar el JAR generado
COPY --from=builder /app/services/user-service/build/libs/*.jar app.jar

# Exponer puerto
EXPOSE 8081

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Comando para arrancar
ENTRYPOINT ["java", "-jar", "app.jar"]