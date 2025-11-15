FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Maven wrapper is configured in "script" mode, so we rely on the Maven
# distribution provided by the builder image.
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src

RUN mvn -B -Pproduction -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
