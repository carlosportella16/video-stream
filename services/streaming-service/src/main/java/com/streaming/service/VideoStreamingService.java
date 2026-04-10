package com.streaming.service;

import com.streaming.domain.VideoEvent;
import com.streaming.integration.S3VideoStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoStreamingService {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamingService.class);

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
     * Returns the playlist.m3u8 content with transformed segment and init URLs.
     * Handles both legacy .ts and modern fMP4 (.m4s) segments.
     */
    public String getPlaylistContent(String videoId) {
        try {
            ResponseInputStream<GetObjectResponse> stream = getPlaylist(videoId);
            StringBuilder transformedPlaylist = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isSegmentReference(line)) {
                        // .ts  →  /video/{id}/playlist0.ts
                        // .m4s →  /video/{id}/seg0.m4s
                        line = buildSegmentUrl(videoId, line);
                    } else if (isInitMapReference(line)) {
                        // #EXT-X-MAP:URI="init.mp4"  →  #EXT-X-MAP:URI="/video/{id}/init.mp4"
                        line = rewriteInitMap(videoId, line);
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

    /**
     * Returns segment bytes from the in-memory Caffeine cache.
     * On first access the bytes are read from S3 and stored; subsequent calls
     * are sub-millisecond hits — the S3 round-trip is completely skipped.
     */
    @Cacheable(value = "segments", key = "#videoId + '_' + #chunk")
    public byte[] getChunkBytes(String videoId, String chunk) {
        String key = videoId + "/" + chunk;
        log.debug("[Cache] MISS — fetching from S3: {}", key);
        try (ResponseInputStream<GetObjectResponse> stream = s3Service.getObject(key)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read segment from S3: " + key, e);
        }
    }

    /**
     * Reads the raw playlist from S3 and returns all segment filenames
     * (e.g. "seg0.m4s", "init.mp4"). Used by the cache warmer.
     */
    public List<String> getSegmentNames(String videoId) {
        List<String> names = new ArrayList<>();
        try (ResponseInputStream<GetObjectResponse> stream = getPlaylist(videoId);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (isSegmentReference(trimmed)) {
                    names.add(trimmed);
                }
            }
        } catch (Exception e) {
            log.warn("[Cache] Could not read playlist for '{}': {}", videoId, e.getMessage());
        }
        return names;
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
     * Checks if a line is a reference to a video segment file
     * @param line The line to check
     * @return true if the line ends with .ts or .m4s, false otherwise
     */
    private boolean isSegmentReference(String line) {
        if (line == null) return false;
        String t = line.trim();
        return t.endsWith(".ts") || t.endsWith(".m4s");
    }

    /**
     * Checks if a line is an EXT-X-MAP reference for initialization segment
     * @param line The line to check
     * @return true if the line starts with #EXT-X-MAP, false otherwise
     */
    private boolean isInitMapReference(String line) {
        return line != null && line.trim().startsWith("#EXT-X-MAP");
    }

    /**
     * Builds the complete URL for a specific segment
     * @param videoId The video ID
     * @param filename The segment filename
     * @return The complete URL of the segment
     */
    private String buildSegmentUrl(String videoId, String filename) {
        return "/video/" + videoId + "/" + filename.trim();
    }

    /**
     * Rewrites the EXT-X-MAP line to point to the correct initialization segment URL
     * @param videoId The video ID
     * @param line The EXT-X-MAP line
     * @return The rewritten EXT-X-MAP line
     */
    private String rewriteInitMap(String videoId, String line) {
        // Replace URI="init.mp4" with URI="/video/{id}/init.mp4"
        return line.replaceAll("URI=\"([^\"]+)\"",
                "URI=\"/video/" + videoId + "/$1\"");
    }

    // Keep old name as alias for backwards compat
    private boolean isChunkReference(String line) { return isSegmentReference(line); }
    private String buildChunkUrl(String videoId, String f) { return buildSegmentUrl(videoId, f); }
}
