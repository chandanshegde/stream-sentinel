package com.sentinel.lib.model;

public class RedactionRecord {
    private String entityType;
    private String originalValue;
    private String redactedValue;
    private double confidence;
    private String source; // e.g., "REGEX", "NER_PRESIDIO"

    public RedactionRecord() {}

    public RedactionRecord(String entityType, String originalValue, String redactedValue, double confidence, String source) {
        this.entityType = entityType;
        this.originalValue = originalValue;
        this.redactedValue = redactedValue;
        this.confidence = confidence;
        this.source = source;
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getOriginalValue() { return originalValue; }
    public void setOriginalValue(String originalValue) { this.originalValue = originalValue; }
    public String getRedactedValue() { return redactedValue; }
    public void setRedactedValue(String redactedValue) { this.redactedValue = redactedValue; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
