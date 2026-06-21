FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar
ADD https://dtdg.co/latest-java-tracer /app/dd-java-agent.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
