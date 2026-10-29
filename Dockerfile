# One Dockerfile for every service: docker build --build-arg MODULE=vehicle-service .
# Multi-stage: the JDK and Maven stay in the build stage, the image ships only a JRE and the app.

FROM eclipse-temurin:21-jdk-alpine AS build
ARG MODULE
WORKDIR /workspace
COPY . .
# The cache mount keeps downloaded dependencies between builds of all four services;
# sharing=locked stops parallel builds from reading a half-written Maven install.
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw -B -q -ntp -pl "${MODULE}" -am package -DskipTests \
 && cp "${MODULE}"/target/"${MODULE}"-*.jar application.jar \
 && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fleetcare && adduser -S fleetcare -G fleetcare
USER fleetcare
# Least to most frequently changing, so a code change rebuilds only the last layer.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]
