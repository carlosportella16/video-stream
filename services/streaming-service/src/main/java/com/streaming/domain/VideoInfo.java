package com.streaming.domain;

public record VideoInfo(
        String id,
        String title,
        String description,
        int durationSeconds,
        int segmentCount
) {}

