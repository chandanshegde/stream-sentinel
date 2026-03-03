package com.sentinel.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sentinel.lib.model.EventData;
import com.sentinel.lib.service.DeterministicRedactor;
import com.sentinel.lib.service.PseudonymizerService;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class SentinelStreamingJob {

    private static final Logger log = LoggerFactory.getLogger(SentinelStreamingJob.class);

    public static void main(String[] args) throws TimeoutException, StreamingQueryException {
        SparkSession spark = SparkSession.builder()
                .appName("SentinelStreamingJob")
                .master("local[*]")
                .getOrCreate();

        Dataset<Row> pulsarStream = spark
                .readStream()
                .format("pulsar")
                .option("service.url", "pulsar://localhost:6650")
                .option("admin.url", "http://localhost:8080")
                .option("topic", "persistent://public/default/raw-events")
                .load();

        // Convert byte[] value to string
        Dataset<String> stringStream = pulsarStream.selectExpr("CAST(value AS STRING)").as(Encoders.STRING());

        // Process logic
        Dataset<String> processedStream = stringStream.map((MapFunction<String, String>) json -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            try {
                EventData event = mapper.readValue(json, EventData.class);

                // Initialize services (In a real distributed app, these would be broadcast or instantiated per partition)
                PseudonymizerService pseudonymizer = new PseudonymizerService();
                DeterministicRedactor redactor = new DeterministicRedactor(pseudonymizer);

                // 1. Deterministic Redaction (Regex)
                // This will mutate the event directly by adding Regex RedactionRecords and updating the payload
                String redactedPayload = redactor.redact(event);
                event.setPayload(redactedPayload);
                
                // 2. ML-based NER via Remote Service
                // In a production environment, we'd batch these calls or use a circuit breaker
                try {
                    String nerRedacted = callNerService(event);
                    event.setPayload(nerRedacted);
                } catch (Exception e) {
                    log.warn("NER service call failed, falling back to deterministic redaction only: {}", e.getMessage());
                }

                return mapper.writeValueAsString(event);
            } catch (Exception e) {
                log.error("Failed to process event", e);
                return json;
            }
        }, Encoders.STRING());

        StreamingQuery query = processedStream
                .writeStream()
                .outputMode("append")
                .format("console")
                .start();

        query.awaitTermination();
    }

    private static String callNerService(EventData event) throws Exception {
        String text = event.getPayload();
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8001/analyze"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        String.format("{\"text\": \"%s\", \"tenant_id\": \"%s\"}", 
                        text.replace("\"", "\\\""), event.getTenantId())))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(response.body());
            
            // Add audit records
            if (node.has("entities")) {
                for (com.fasterxml.jackson.databind.JsonNode entityNode : node.get("entities")) {
                    String entityType = entityNode.get("type").asText();
                    double score = entityNode.get("score").asDouble();
                    // Basic placeholder since Presidio doesn't return the exact masked token out-of-the-box in the main payload without tweaking
                    event.addRedaction(new com.sentinel.lib.model.RedactionRecord(
                        entityType,
                        "REDACTED", 
                        "<" + entityType + ">", // Use generic since Anonymizer typically replaces with <TYPE>
                        score,
                        "NER_PRESIDIO"
                    ));
                }
            }
            
            if (node.has("redacted_text")) {
                return node.get("redacted_text").asText();
            }
        }
        return text;
    }
}
