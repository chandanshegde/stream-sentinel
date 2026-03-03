# Stream Sentinel

Abuse & PII Filtering Stream Ingestion Pipeline.

## Tech Stack

- **Language**: Java 17+, Python 3.10+
- **API**: Spring Boot 3.x (Native via GraalVM)
- **Message Bus**: Apache Pulsar
- **Stream Engine**: Spark Structured Streaming (v1) / Flink (v2 roadmap)
- **PII Detection**: Microsoft Presidio
- **Database**: Postgres (pgvector)
- **Storage**: Apache Iceberg on MinIO (S3 Compatible)
- **Observability**: Prometheus & Grafana

## Getting Started

### Prerequisites

- Docker & Docker Compose
- JDK 17
- Maven

### Running the Stack

```bash
docker-compose up -d
```

### Components

- **ingest-api**: REST endpoint for event ingestion.
- **lib-detection**: Shared logic for redaction and classification.
- **ner-service**: Python worker for PII detection using Presidio.
- **spark-job**: Real-time processing pipeline.

## Senior Engineering Signal

This project demonstrates:

- **Engine-Agnostic Design**: Business logic is separated from the streaming framework.
- **GraalVM**: Native compilation for instant scaling and low memory footprint.
- **Presidio**: Security-grade PII detection.
- **Lakehouse Architecture**: Streaming ingest into Iceberg tables on MinIO.
