FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY artifact-registry-sa.json /tmp/ar-sa.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/tmp/ar-sa.json
COPY email-service/pom.xml .
RUN mvn dependency:go-offline -B
COPY email-service/src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
