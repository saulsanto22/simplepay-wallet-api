# --- TAHAP 1: Build aplikasi menjadi file .jar menggunakan JDK 21 ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# --- TAHAP 2: Jalankan aplikasi menggunakan JRE 21 yang ringan ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

