package com.streaming.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoEvent {

    @JsonProperty("event")
    private String event;

    @JsonProperty("videoId")
    private String videoId;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("sessionId")
    private String sessionId;


    public VideoEvent(String event, String videoId, Long timestamp, String sessionId) {
        this.event = event;
        this.videoId = videoId;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

