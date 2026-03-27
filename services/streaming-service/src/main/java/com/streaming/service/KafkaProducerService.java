package com.streaming.service;

import com.streaming.domain.VideoEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, VideoEvent> kafkaTemplate;

    private static final String TOPIC = "video-events";

    public KafkaProducerService(KafkaTemplate<String, VideoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(VideoEvent event) {
        kafkaTemplate.send(TOPIC, event.getVideoId(), event);
    }
}
