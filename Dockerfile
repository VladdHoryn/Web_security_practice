FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENTRYPOINT ["-jar", "java", "app.jar"]

COPY target/Code-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081