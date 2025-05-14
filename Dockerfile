# 🔧 Stap 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 🛠️ Stap 2: Runtime stage met beperkte resources
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/zorgmate-0.0.1-SNAPSHOT.jar app.jar

# ✅ Beperk geheugen voor Railway
ENV JAVA_OPTS="-Xmx256m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
