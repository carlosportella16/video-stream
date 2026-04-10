package com.streaming.controller;

import com.streaming.service.VideoStreamingService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/video")
public class VideoController {

    private static final String PLAYLIST_CONTENT_TYPE = "application/vnd.apple.mpegurl";
    private static final String CHUNK_CONTENT_TYPE     = "video/MP2T";   // legacy .ts
    private static final String FMP4_SEGMENT_TYPE      = "video/mp4";    // .m4s / init.mp4

    private final VideoStreamingService videoStreamingService;

    public VideoController(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
    }

    /** Returns the playlist.m3u8 with rewritten segment URLs (never cached). */
    @GetMapping("/{videoId}/playlist")
    public ResponseEntity<InputStreamResource> getPlaylist(@PathVariable String videoId) {
        String playlistContent = videoStreamingService.getPlaylistContent(videoId);
        byte[] playlistBytes = playlistContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, PLAYLIST_CONTENT_TYPE)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(playlistBytes.length))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(new InputStreamResource(new ByteArrayInputStream(playlistBytes)));
    }

    /**
     * Serves a segment (seg*.m4s, init.mp4, *.ts) from the Caffeine
     * in-memory cache.  On a cache hit the entire response is assembled
     * in RAM and sent without any S3 or LocalStack involvement.
     */
    @GetMapping("/{videoId}/{chunk}")
    public ResponseEntity<byte[]> getChunk(
            @PathVariable String videoId,
            @PathVariable String chunk) {

        byte[] data = videoStreamingService.getChunkBytes(videoId, chunk);

        String contentType = (chunk.endsWith(".m4s") || chunk.endsWith(".mp4"))
                ? FMP4_SEGMENT_TYPE
                : CHUNK_CONTENT_TYPE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .header("ETag", "\"" + videoId + "-" + chunk + "\"")
                .body(data);
    }

    /** Alias: /video/{id}/chunk/{file} → same handler */
    @GetMapping("/{videoId}/chunk/{chunk}")
    public ResponseEntity<byte[]> getChunkExplicit(
            @PathVariable String videoId,
            @PathVariable String chunk) {

        return getChunk(videoId, chunk);
    }

    @PostMapping("/{videoId}/play")
    public void play(@PathVariable String videoId) {
        videoStreamingService.sendPlayEvent(videoId);
    }

    @PostMapping("/{videoId}/pause")
    public void pause(@PathVariable String videoId) {
        videoStreamingService.sendPauseEvent(videoId);
    }

    @PostMapping("/{videoId}/stop")
    public void stop(@PathVariable String videoId) {
        videoStreamingService.sendStopEvent(videoId);
    }
}
