# 🔧 Stap 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 🛠️ Stap 2: Runtime stage
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/zorgmate-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
