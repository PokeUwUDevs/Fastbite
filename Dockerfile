# --- ETAPA 1: Construcción (Builder) ---
# Usamos una imagen con Java 21 para compilar el proyecto
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copiamos todos los archivos del proyecto al contenedor
COPY . .

# Damos permisos de ejecución al script de Gradle
RUN chmod +x gradlew

# Ejecutamos el build para generar el archivo .jar (saltando los tests para ir más rápido, ya que se validaron en el paso anterior)
RUN ./gradlew bootJar --no-daemon -x test

# --- ETAPA 2: Ejecución (Runner) ---
# Usamos una imagen limpia y ligera solo para correr la app
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiamos SOLAMENTE el archivo .jar generado en la etapa anterior
# Esto hace que tu imagen final pese mucho menos
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponemos el puerto 8080 (donde corre Spring Boot)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
