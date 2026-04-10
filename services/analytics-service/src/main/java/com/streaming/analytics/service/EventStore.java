package com.streaming.analytics.service;

import com.streaming.analytics.model.VideoEvent;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * In-memory store for the last N video events.
 * Thread-safe via synchronized methods.
 */
@Component
public class EventStore {

    private static final int MAX_EVENTS = 100;

    private final Deque<VideoEvent> events = new ArrayDeque<>();

    public synchronized void add(VideoEvent event) {
        if (events.size() >= MAX_EVENTS) {
            events.pollFirst(); // remove oldest
        }
        events.addLast(event);
    }

    /** Returns events newest-first. */
    public synchronized List<VideoEvent> getAll() {
        List<VideoEvent> list = new ArrayList<>(events);
        // reverse so newest is first
        Collections.reverse(list);
        return list;
    }
}

