# ---------- Build stage ----------
FROM maven:3.9.3-eclipse-temurin-21-jammy AS build

WORKDIR /app

# Kopieer projectbestanden en download dependencies + build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Kopieer de JAR uit de build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Start de Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
