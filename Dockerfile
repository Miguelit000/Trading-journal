# ==========================================
# ETAPA 1: Compilación (Build)
# ==========================================
# Usamos una imagen oficial de Maven con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el pom.xml primero. Esto es un truco de Docker para cachear 
# las dependencias y no tener que descargarlas cada vez que cambias un archivo .java
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos todo el código fuente
COPY src ./src

# Compilamos el proyecto y generamos el .jar (saltando los tests para mayor velocidad en el despliegue)
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Producción (Run)
# ==========================================
# Usamos una imagen de Java 21 JRE "Alpine" (es una versión de Linux ultra ligera y segura)
FROM eclipse-temurin:21-jre-alpine

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos ÚNICAMENTE el archivo .jar generado en la Etapa 1
COPY --from=build /app/target/trading-journal-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8080 (Cloud Run inyectará el tráfico por aquí)
EXPOSE 8080

# Comando final que ejecutará el contenedor al encender
ENTRYPOINT ["java", "-jar", "app.jar"]