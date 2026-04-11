package com.streaming.service;

import com.streaming.domain.VideoInfo;
import com.streaming.integration.S3VideoStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class VideoInfoService {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoService.class);

    /**
     * Fallback descriptions per video ID.
     * When no dedicated metadata file exists, these are used.
     */
    private static final Map<String, String> DESCRIPTIONS = Map.of(
            "movie1", "Uma demo imersiva construída com HLS, Kafka e LocalStack S3. " +
                      "Cada interação — play, pause, stop — dispara um evento em tempo real pelo pipeline.",
            "movie2", "Whatever It Takes — Imagine Dragons. " +
                      "Transmitido via HLS fMP4 diretamente do LocalStack S3."
    );

    private static final String DEFAULT_DESCRIPTION =
            "Conteúdo transmitido via HLS fMP4 com segmentos armazenados no LocalStack S3. " +
            "Eventos de play, pause e stop são publicados em tempo real no Kafka.";

    private final S3Client s3Client;
    private final S3VideoStorageService s3Service;
    private final String bucket;

    public VideoInfoService(S3Client s3Client,
                            S3VideoStorageService s3Service,
                            @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket}") String bucket) {
        this.s3Client  = s3Client;
        this.s3Service = s3Service;
        this.bucket    = bucket;
    }

    /** Lists all video IDs found as "folders" in the S3 bucket. */
    public List<String> listVideoIds() {
        try {
            var response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .delimiter("/")
                            .build());

            return response.commonPrefixes().stream()
                    .map(p -> p.prefix().replaceAll("/$", ""))
                    .filter(id -> !id.isBlank())
                    .sorted()
                    .toList();
        } catch (Exception e) {
            log.warn("[VideoInfoService] Could not list S3 prefixes: {}", e.getMessage());
            return List.of();
        }
    }

    /** Builds a full VideoInfo DTO for the given videoId. */
    public VideoInfo getVideoInfo(String videoId) {
        PlaylistMeta meta = parsePlaylist(videoId);
        return new VideoInfo(
                videoId,
                formatTitle(videoId),
                DESCRIPTIONS.getOrDefault(videoId, DEFAULT_DESCRIPTION),
                meta.durationSeconds(),
                meta.segmentCount()
        );
    }

    /** Returns VideoInfo for every video in the bucket. */
    public List<VideoInfo> listVideos() {
        return listVideoIds().stream()
                .map(this::getVideoInfo)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private record PlaylistMeta(int durationSeconds, int segmentCount) {}

    private PlaylistMeta parsePlaylist(String videoId) {
        try (var stream = s3Service.getObject(videoId + "/playlist.m3u8");
             var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            double totalSeconds = 0;
            int segments = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                // #EXTINF:4.170833,
                if (line.startsWith("#EXTINF:")) {
                    String val = line.substring(8).replace(",", "").trim();
                    totalSeconds += Double.parseDouble(val);
                    segments++;
                }
            }
            return new PlaylistMeta((int) Math.round(totalSeconds), segments);

        } catch (Exception e) {
            log.debug("[VideoInfoService] Could not parse playlist for '{}': {}", videoId, e.getMessage());
            return new PlaylistMeta(0, 0);
        }
    }

    /**
     * Converts a snake_case or kebab-case videoId to a human-readable title.
     * Examples:
     *   "movie1"          → "Movie1"
     *   "whatever_it_takes_imagine_dragons" → "Whatever It Takes Imagine Dragons"
     */
    private String formatTitle(String videoId) {
        return Arrays.stream(videoId.split("[_\\-]+"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .reduce((a, b) -> a + " " + b)
                .orElse(videoId);
    }
}

