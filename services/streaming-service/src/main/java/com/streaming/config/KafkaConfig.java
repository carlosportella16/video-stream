package com.streaming.config;

import com.streaming.domain.VideoEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, VideoEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private ProducerFactory<String, VideoEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put("value.serializer", JsonSerializer.class);
        configProps.put("acks", "all");
        configProps.put("retries", 3);
        configProps.put("linger.ms", 10);

        return new DefaultKafkaProducerFactory<>(configProps);
    }
}

