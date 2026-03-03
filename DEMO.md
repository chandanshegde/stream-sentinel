# Stream Sentinel Demo Guide

This guide allows you to demo each stage of the PII filtering pipeline.

## Stage 1: Core Redaction (Java Library)

The `lib-detection` module handles high-speed regex redaction and deterministic pseudonymization.

```bash
./gradlew :lib-detection:test
```

_Observe the console output to see how email, CC, and SSN are masked._

## Stage 2: ML-based NER (Python Service)

The `ner-service` uses Microsoft Presidio for context-aware PII detection (Names, Locations).

1. **Start the service:**
   ```bash
   docker-compose up -d ner-service
   ```
2. **Test via curl:**
   ```bash
   curl -X POST http://localhost:8001/analyze \
     -H "Content-Type: application/json" \
     -d '{"text": "My name is John Doe and I live in New York.", "tenant_id": "tenant-1"}'
   ```
   _Observe the `redacted_text` field in the response._

## Stage 3: Ingestion Flow (API -> Pulsar)

1. **Start Pulsar:**
   ```bash
   docker-compose up -d pulsar
   ```
2. **Run Ingest API:**
   ```bash
   ./gradlew :ingest-api:bootRun
   ```
3. **Send an event:**
   ```bash
   curl -X POST http://localhost:8081/api/v1/ingest \
     -H "Content-Type: application/json" \
     -H "X-Tenant-ID: my-tenant" \
     -d '{"payload": "User alice@example.com logged in."}'
   ```

## Stage 4: Full Pipeline (Spark Integration)

1. **Start all services:**
   ```bash
   docker-compose up -d
   ```
2. **Run Spark Job:**

   ```bash
   ./gradlew :spark-job:run # (Note: requires Spark local setup or running the shadowJar)
   ```

   _Alternatively, run the main class `SentinelStreamingJob` from your IDE._

3. **Observe Output:**
   The Spark console will show events being processed, first by Java Regex, then by Python NER.
