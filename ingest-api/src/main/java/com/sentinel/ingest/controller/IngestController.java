package com.sentinel.ingest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sentinel.lib.model.EventData;
import org.apache.pulsar.client.api.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1")
public class IngestController {

    private static final Logger log = LoggerFactory.getLogger(IngestController.class);
    private final Producer<byte[]> producer;
    private final ObjectMapper mapper;

    public IngestController(Producer<byte[]> producer) {
        this.producer = producer;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestBody Map<String, Object> body) {
        
        try {
            EventData event = EventData.builder()
                    .id(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .timestamp(Instant.now())
                    .payload(body.getOrDefault("payload", "").toString())
                    .metadata((Map<String, String>) body.get("metadata"))
                    .build();

            byte[] jsonBytes = mapper.writeValueAsBytes(event);

            // Async send
            CompletableFuture<org.apache.pulsar.client.api.MessageId> future = producer.newMessage()
                    .key(event.getId())
                    .value(jsonBytes)
                    .sendAsync();

            future.whenComplete((msgId, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish message: {}", event.getId(), ex);
                }
            });

            return ResponseEntity.accepted().body(new IngestResponse(event.getId(), "accepted"));

        } catch (JsonProcessingException e) {
            log.error("Error serializing event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    public static class IngestResponse {
        public String eventId;
        public String status;

        public IngestResponse(String eventId, String status) {
            this.eventId = eventId;
            this.status = status;
        }
    }
}
