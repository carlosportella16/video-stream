package com.streaming.analytics.consumer;

import com.streaming.analytics.model.VideoEvent;
import com.streaming.analytics.service.EventProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VideoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoEventConsumer.class);
    private static final String TOPIC = "video-events";
    private static final String GROUP = "analytics-group";

    private final EventProcessingService eventProcessingService;

    public VideoEventConsumer(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP)
    public void consume(VideoEvent event) {
        try {
            log.info("Received event from Kafka: {}", event);
            eventProcessingService.processEvent(event);
        } catch (Exception e) {
            log.error("Error processing event: {}", event, e);
        }
    }
}

