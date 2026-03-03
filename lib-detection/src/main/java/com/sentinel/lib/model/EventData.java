package com.sentinel.lib.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventData {
    private String id;
    private String tenantId;
    private Instant timestamp;
    private String payload;
    private Map<String, String> metadata;
    private List<RedactionRecord> redactions = new ArrayList<>();

    public EventData() {}

    public EventData(String id, String tenantId, Instant timestamp, String payload, Map<String, String> metadata, List<RedactionRecord> redactions) {
        this.id = id;
        this.tenantId = tenantId;
        this.timestamp = timestamp;
        this.payload = payload;
        this.metadata = metadata;
        this.redactions = redactions != null ? redactions : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public List<RedactionRecord> getRedactions() { return redactions; }
    public void setRedactions(List<RedactionRecord> redactions) { this.redactions = redactions; }
    public void addRedaction(RedactionRecord record) { this.redactions.add(record); }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String tenantId;
        private Instant timestamp;
        private String payload;
        private Map<String, String> metadata;
        private List<RedactionRecord> redactions = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder metadata(Map<String, String> metadata) { this.metadata = metadata; return this; }
        public Builder redactions(List<RedactionRecord> redactions) { this.redactions = redactions; return this; }

        public EventData build() {
            return new EventData(id, tenantId, timestamp, payload, metadata, redactions);
        }
    }
}

