# ==========================================
# ETAPA 1: Construcción (Build)
# ==========================================
# Usamos una imagen que ya tiene Maven y Java 21 oficial instalado
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Le decimos a Docker que trabaje adentro de una carpeta llamada /app
WORKDIR /app

# Copiamos tu archivo pom.xml y tu código fuente hacia adentro de Docker
COPY pom.xml .
COPY src ./src

# Le decimos a Maven que compile tu proyecto y genere el archivo .jar
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Ejecución (Run)
# ==========================================
# Para que el archivo final sea muy liviano, usamos una versión de Java sin Maven
FROM eclipse-temurin:21-jre-alpine

# Volvemos a ubicarnos en /app
WORKDIR /app

# Vamos a la Etapa 1, agarramos el archivo .jar que se acaba de crear, y lo traemos para acá
COPY --from=build /app/target/*.jar app.jar

# Le avisamos a Docker que nuestra app usa el puerto 8080
EXPOSE 8080

# El comando exacto que tiene que correr Docker cuando encendamos el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]