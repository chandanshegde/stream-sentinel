package com.sentinel.lib.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sentinel.lib.model.EventData;

/**
 * DeterministicRedactor performs high-speed regex-based redaction of standard PII
 * before invoking slower ML-based NER models.
 */
public class DeterministicRedactor {

    private static final Logger log = LoggerFactory.getLogger(DeterministicRedactor.class);

    // Simplified generic patterns for demonstration purposes
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,7}\\b");
    private static final Pattern CC_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    private final PseudonymizerService pseudonymizer;

    public DeterministicRedactor(PseudonymizerService pseudonymizer) {
        this.pseudonymizer = pseudonymizer;
    }

    /**
     * Replaces deterministic PII patterns with pseudonymized values and logs the redaction.
     * 
     * @param event The event being processed
     * @return Redacted text payload
     */
    public String redact(EventData event) {
        String payload = event.getPayload();
        if (payload == null || payload.isBlank()) {
            return payload;
        }

        String redacted = payload;
        redacted = applyRedaction(event, redacted, EMAIL_PATTERN, "EMAIL");
        redacted = applyRedaction(event, redacted, CC_PATTERN, "CC");
        redacted = applyRedaction(event, redacted, SSN_PATTERN, "SSN");

        return redacted;
    }

    private String applyRedaction(EventData event, String text, Pattern pattern, String entityType) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group();
            // Pseudonymize the found PII
            String pseudo = pseudonymizer.pseudonymize(match, event.getTenantId());
            String replacement = "<" + entityType + ":" + pseudo + ">";
            
            // Replace with <TYPE:pseudo>
            matcher.appendReplacement(sb, replacement);
            
            // Log redaction
            event.addRedaction(new com.sentinel.lib.model.RedactionRecord(
                entityType,
                match,
                replacement,
                1.0, // Regex is usually considered 100% confidence
                "REGEX"
            ));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
