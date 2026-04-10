package com.streaming.analytics.controller;

import com.streaming.analytics.model.VideoEvent;
import com.streaming.analytics.service.EventStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final EventStore eventStore;

    public AnalyticsController(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    /**
     * Returns recent video events (newest first).
     * Optional ?limit=N to cap the results.
     */
    @GetMapping("/events")
    public ResponseEntity<List<VideoEvent>> getEvents(
            @RequestParam(defaultValue = "50") int limit) {
        List<VideoEvent> all = eventStore.getAll();
        List<VideoEvent> result = all.size() > limit ? all.subList(0, limit) : all;
        return ResponseEntity.ok(result);
    }
}

