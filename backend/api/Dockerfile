FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY api/mvnw .
COPY api/.mvn .mvn
COPY api/pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY api/src src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine-3.22

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
