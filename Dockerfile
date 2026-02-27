# Base image Java 17
FROM eclipse-temurin:17-jdk

# Dossier de travail
WORKDIR /app

# Copier le jar compilé
COPY target/blog-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port Spring Boot
EXPOSE 8080

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]