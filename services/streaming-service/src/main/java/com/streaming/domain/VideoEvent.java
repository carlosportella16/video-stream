package com.streaming.domain;

public class VideoEvent {
    private String event;
    private String videoId;
    private long timestamp;
    private String sessionId;

    public VideoEvent(String event, String videoId, long timestamp, String sessionId) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
