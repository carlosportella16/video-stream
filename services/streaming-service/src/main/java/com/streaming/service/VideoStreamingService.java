package com.streaming.service;

import com.streaming.domain.VideoEvent;
import com.streaming.integration.S3VideoStorageService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class VideoStreamingService {

    private final S3VideoStorageService s3Service;
    private final KafkaProducerService kafkaProducer;

    public VideoStreamingService(S3VideoStorageService s3Service, KafkaProducerService kafkaProducer) {
        this.s3Service = s3Service;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Returns the raw playlist.m3u8 stream from S3
     * @param videoId The video ID
     * @return ResponseInputStream with the playlist content
     */
    public ResponseInputStream<GetObjectResponse> getPlaylist(String videoId) {
        String key = videoId + "/playlist.m3u8";
        return s3Service.getObject(key);
    }

    /**
     * Returns the playlist.m3u8 content with transformed chunk URLs
     * Converts relative references (playlist0.ts) to absolute URLs (/video/{videoId}/playlist0.ts)
     * @param videoId The video ID
     * @return String with the transformed playlist content
     */
    public String getPlaylistContent(String videoId) {
        try {
            ResponseInputStream<GetObjectResponse> stream = getPlaylist(videoId);
            StringBuilder transformedPlaylist = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Transform lines with .ts extension to complete URLs
                    if (isChunkReference(line)) {
                        line = buildChunkUrl(videoId, line);
                    }
                    transformedPlaylist.append(line).append("\n");
                }
            }

            return transformedPlaylist.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error processing playlist for videoId: " + videoId, e);
        }
    }

    /**
     * Returns the stream of a specific video chunk
     * @param videoId The video ID
     * @param chunk The chunk filename (e.g., playlist0.ts)
     * @return ResponseInputStream with the chunk content
     */
    public ResponseInputStream<GetObjectResponse> getChunk(String videoId, String chunk) {
        String key = videoId + "/" + chunk;
        return s3Service.getObject(key);
    }

    public void sendPlayEvent(String videoId) {
        kafkaProducer.sendEvent(new VideoEvent(
                "PLAY",
                videoId,
                System.currentTimeMillis(),
                generateSessionId()
        ));
    }

    public void sendPauseEvent(String videoId) {
        kafkaProducer.sendEvent(new VideoEvent(
                "PAUSE",
                videoId,
                System.currentTimeMillis(),
                generateSessionId()
        ));
    }

    public void sendStopEvent(String videoId) {
        kafkaProducer.sendEvent(new VideoEvent(
                "STOP",
                videoId,
                System.currentTimeMillis(),
                generateSessionId()
        ));
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Checks if a line is a reference to a video chunk file
     * @param line The line to check
     * @return true if the line ends with .ts, false otherwise
     */
    private boolean isChunkReference(String line) {
        return line != null && line.trim().endsWith(".ts");
    }

    /**
     * Builds the complete URL for a specific chunk
     * @param videoId The video ID
     * @param chunkFilename The chunk filename
     * @return The complete URL of the chunk
     */
    private String buildChunkUrl(String videoId, String chunkFilename) {
        return "/video/" + videoId + "/" + chunkFilename.trim();
    }
}
