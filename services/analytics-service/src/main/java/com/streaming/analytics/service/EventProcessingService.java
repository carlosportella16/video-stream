package com.streaming.analytics.service;

import com.streaming.analytics.model.VideoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

    private final EventStore eventStore;

    public EventProcessingService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void processEvent(VideoEvent event) {
        validateEvent(event);
        logEvent(event);
        persistEvent(event);
    }

    private void validateEvent(VideoEvent event) {
        if (event.getEvent() == null || event.getEvent().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (event.getVideoId() == null || event.getVideoId().isEmpty()) {
            throw new IllegalArgumentException("VideoId cannot be null");
        }
        if (event.getSessionId() == null || event.getSessionId().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null");
        }
    }

    private void logEvent(VideoEvent event) {
        log.info("Processing event: event={}, videoId={}, sessionId={}, timestamp={}",
            event.getEvent(), event.getVideoId(), event.getSessionId(), event.getTimestamp());
    }

    private void persistEvent(VideoEvent event) {
        eventStore.add(event);
        log.debug("Event persisted: {}", event);
    }
}

