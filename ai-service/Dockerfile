FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY eureka/pom.xml eureka/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY question-service/pom.xml question-service/pom.xml
COPY exam-service/pom.xml exam-service/pom.xml
COPY ai-service/pom.xml ai-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
RUN mvn -B -pl ai-service -am dependency:go-offline

COPY ai-service/src ai-service/src
RUN mvn -B -pl ai-service -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/ai-service/target/ai-service-*.jar app.jar
RUN mkdir -p /data/ai-documents && chown app:app /data/ai-documents
USER app
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

