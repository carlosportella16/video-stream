package com.streaming.analytics.config;

import com.streaming.analytics.model.VideoEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:analytics-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, VideoEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", JsonDeserializer.class);
        props.put("spring.json.type.mapping", "videoEvent:com.streaming.analytics.model.VideoEvent");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.streaming.analytics.model.VideoEvent");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", true);
        props.put("auto.commit.interval.ms", 1000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VideoEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

