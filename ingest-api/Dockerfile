FROM gradle:8.5-jdk17 AS builder

WORKDIR /build

COPY settings.gradle .
COPY build.gradle .
COPY lib-detection/build.gradle lib-detection/
COPY ingest-api/build.gradle ingest-api/
COPY spark-job/build.gradle spark-job/

# Copy sources
COPY lib-detection/src lib-detection/src
COPY ingest-api/src ingest-api/src

# Build
RUN gradle :ingest-api:bootJar -x test

# Run stage
FROM ghcr.io/graalvm/jdk:java17
WORKDIR /app

COPY --from=builder /build/ingest-api/build/libs/ingest-api.jar /app/ingest-api.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/ingest-api.jar"]
