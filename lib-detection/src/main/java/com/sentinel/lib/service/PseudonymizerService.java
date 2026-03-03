package com.sentinel.lib.service;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PseudonymizerService {

    // In a real system, this key material comes from a KMS (Key Management Service)
    private static final String MASTER_KEY = "super-secret-master-key";

    /**
     * Deterministically pseudonymizes an entity value, salted by the tenantId.
     * This ensures the same email turns into the same pseudonym for a specific tenant,
     * but a different pseudonym for a different tenant.
     *
     * @param entityValue The raw PII (e.g. email)
     * @param tenantId    The tenant ID
     * @return Base64Url encoded truncated HMAC
     */
    public String pseudonymize(String entityValue, String tenantId) {
        if (entityValue == null || entityValue.isBlank()) {
            return entityValue;
        }
        
        // Tenant-specific salt derived from master key + tenantId
        // Allows deleting a single tenant's key (Crypto-erasure)
        String tenantKey = MASTER_KEY + ":" + tenantId;

        // Generate HMAC-SHA256
        byte[] hmacValue = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, tenantKey).hmac(entityValue);

        // Base64Url encode and truncate to 12 chars to keep payloads manageable
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacValue).substring(0, 12);
    }
}
