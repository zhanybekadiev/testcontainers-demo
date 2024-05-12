package com.example.testcontainersdemo.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    @Value("${kafka.consumer.max-attempts}")
    private final long maxAttempts;

    @Bean
    public RecordMessageConverter msgConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(TopicNames.ORDER_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderTopicDlt() {
        return TopicBuilder.name(TopicNames.ORDER_TOPIC_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public KafkaTemplate<String, Object> jsonKafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf, Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, byte[]> kafkaTemplate) {
        var deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        deadLetterPublishingRecoverer.setLogRecoveryRecord(true);

        return new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
                new FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, maxAttempts)
        );
    }
}
