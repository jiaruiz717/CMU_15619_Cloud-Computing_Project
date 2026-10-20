FROM maven:3.9-eclipse-temurin-17 AS build
ARG STACK=vertx
WORKDIR /src
COPY . .
# Skip old integration tests that require an application server / external database.
RUN mvn -B -DskipTests package && \
    if [ "$STACK" = vertx ]; then \
      jar=$(find target -maxdepth 1 -name '*-fat.jar' -print -quit); \
    else \
      jar=$(find target -maxdepth 1 -name '*.jar' ! -name 'original-*' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print -quit); \
    fi && test -n "$jar" && cp "$jar" /application.jar

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=build /application.jar ./application.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/application.jar"]
