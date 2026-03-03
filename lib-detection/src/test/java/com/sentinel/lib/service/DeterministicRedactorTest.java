package com.sentinel.lib.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeterministicRedactorTest {

    @Test
    public void testRedactionAndPseudonymization() {
        PseudonymizerService pseudonymizer = new PseudonymizerService();
        DeterministicRedactor redactor = new DeterministicRedactor(pseudonymizer);

        String tenantId = "tenant-1";
        String payload = "Hello, my email is alice@example.com and my CC is 1234-5678-9012-3456. " +
                         "Bob's SSN is 123-45-6789.";

        com.sentinel.lib.model.EventData event1 = com.sentinel.lib.model.EventData.builder()
                .tenantId(tenantId)
                .payload(payload)
                .build();

        String redacted = redactor.redact(event1);

        System.out.println("Original: " + payload);
        System.out.println("Redacted: " + redacted);

        // Assertions for presence of masked types
        assertTrue(redacted.contains("<EMAIL:"));
        assertTrue(redacted.contains("<CC:"));
        assertTrue(redacted.contains("<SSN:"));

        // Assert the raw PII is gone
        assertFalse(redacted.contains("alice@example.com"));
        assertFalse(redacted.contains("1234-5678-9012-3456"));
        assertFalse(redacted.contains("123-45-6789"));
        
        // Assert Audit Logs
        assertEquals(3, event1.getRedactions().size());

        // Assert determinism
        String anotherPayload = "Send it to alice@example.com.";
        com.sentinel.lib.model.EventData event2 = com.sentinel.lib.model.EventData.builder()
                .tenantId(tenantId)
                .payload(anotherPayload)
                .build();
        String anotherRedacted = redactor.redact(event2);
        
        // Extract the pseudonymized email from first result to ensure they match
        // Both strings should contain the same exact pseudo hash string for alice's email
        String emailHash1 = redacted.substring(redacted.indexOf("<EMAIL:") + 7, redacted.indexOf(">", redacted.indexOf("<EMAIL:")));
        String emailHash2 = anotherRedacted.substring(anotherRedacted.indexOf("<EMAIL:") + 7, anotherRedacted.indexOf(">", anotherRedacted.indexOf("<EMAIL:")));

        assertEquals(emailHash1, emailHash2, "Pseudonyms for same entity in same tenant must match");
        
        // Assert tenant isolation
        com.sentinel.lib.model.EventData event3 = com.sentinel.lib.model.EventData.builder()
                .tenantId("tenant-2")
                .payload(payload)
                .build();
        String redactedOtherTenant = redactor.redact(event3);
        String emailHashOther = redactedOtherTenant.substring(redactedOtherTenant.indexOf("<EMAIL:") + 7, redactedOtherTenant.indexOf(">", redactedOtherTenant.indexOf("<EMAIL:")));

        assertNotEquals(emailHash1, emailHashOther, "Pseudonyms for same entity in different tenants must differ");
    }
}
