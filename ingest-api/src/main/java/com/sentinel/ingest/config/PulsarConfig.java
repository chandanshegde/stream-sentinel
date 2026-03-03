package com.sentinel.ingest.config;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {

    @Value("${pulsar.service-url}")
    private String serviceUrl;

    @Value("${pulsar.topic.raw-events}")
    private String rawEventsTopic;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(serviceUrl)
                .build();
    }

    @Bean
    public Producer<byte[]> rawEventsProducer(PulsarClient client) throws PulsarClientException {
        return client.newProducer(Schema.BYTES)
                .topic(rawEventsTopic)
                .blockIfQueueFull(true)
                .maxPendingMessages(1000)
                .create();
    }
}
