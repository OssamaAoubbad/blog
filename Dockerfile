# Base image avec Maven et JDK
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le pom et le code
COPY pom.xml .
COPY src ./src

# Build le jar
RUN mvn clean package -DskipTests

# Deuxième stage pour réduire la taille
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copier le jar du stage précédent
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]