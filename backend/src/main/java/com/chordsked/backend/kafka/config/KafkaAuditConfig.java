package com.chordsked.backend.kafka.config;

import com.chordsked.backend.config.properties.KafkaAuditProperties;
import com.chordsked.backend.kafka.model.AuditLogEvent;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaAuditConfig {
    @Resource(name = "kafkaAuditProperties")
    private KafkaAuditProperties kafkaAuditProperties;

    @Resource(name = "environment")
    private Environment environment;

    @Bean("auditLogProducerFactory")
    public ProducerFactory<String, AuditLogEvent> auditLogProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, environment.getProperty("spring.kafka.producer.acks", "all"));
        config.put(ProducerConfig.RETRIES_CONFIG,
                Integer.parseInt(environment.getProperty("spring.kafka.producer.retries", "3")));
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                Boolean.parseBoolean(environment.getProperty("spring.kafka.producer.properties.enable.idempotence",
                        "true")));
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean("auditLogKafkaTemplate")
    public KafkaTemplate<String, AuditLogEvent> auditLogKafkaTemplate() {
        return new KafkaTemplate<>(auditLogProducerFactory());
    }

    @Bean("auditLogConsumerFactory")
    public ConsumerFactory<String, AuditLogEvent> auditLogConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaAuditProperties.getGroupId());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                Boolean.parseBoolean(environment.getProperty("spring.kafka.consumer.enable-auto-commit", "false")));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                environment.getProperty("spring.kafka.consumer.auto-offset-reset", "latest"));
        JsonDeserializer<AuditLogEvent> valueDeserializer = new JsonDeserializer<>(AuditLogEvent.class);
        valueDeserializer.addTrustedPackages("com.chordsked.backend.kafka.model");
        valueDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    private String kafkaBootstrapServers() {
        return environment.getProperty("spring.kafka.bootstrap-servers", "127.0.0.1:9092");
    }

    @Bean("auditLogKafkaErrorHandler")
    public DefaultErrorHandler auditLogKafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                auditLogKafkaTemplate(),
                (record, exception) -> new TopicPartition(record.topic() + kafkaAuditProperties.getDltSuffix(),
                        record.partition())
        );
        FixedBackOff fixedBackOff = new FixedBackOff(
                kafkaAuditProperties.getBackoffMillis(),
                kafkaAuditProperties.getMaxRetries()
        );
        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

    @Bean("auditLogKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> auditLogKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auditLogConsumerFactory());
        factory.setConcurrency(kafkaAuditProperties.getConcurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(auditLogKafkaErrorHandler());
        return factory;
    }
}
