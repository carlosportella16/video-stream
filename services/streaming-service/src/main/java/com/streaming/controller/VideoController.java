package com.streaming.controller;

import com.streaming.service.VideoStreamingService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/video")
public class VideoController {

    private static final String PLAYLIST_CONTENT_TYPE = "application/vnd.apple.mpegurl";
    private static final String CHUNK_CONTENT_TYPE = "video/MP2T";

    private final VideoStreamingService videoStreamingService;

    public VideoController(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
    }

    /**
     * Returns the playlist.m3u8 file with transformed chunk URLs
     * @param videoId The video ID
     * @return ResponseEntity containing the .m3u8 file
     */
    @GetMapping("/{videoId}/playlist")
    public ResponseEntity<InputStreamResource> getPlaylist(@PathVariable String videoId) {
        // Get transformed content from service
        String playlistContent = videoStreamingService.getPlaylistContent(videoId);

        // Convert to bytes
        byte[] playlistBytes = playlistContent.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(playlistBytes);

        // Return with appropriate headers
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, PLAYLIST_CONTENT_TYPE)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(playlistBytes.length))
                .body(new InputStreamResource(byteStream));
    }

    /**
     * Returns a specific video chunk
     * @param videoId The video ID
     * @param chunk The chunk filename (e.g., playlist0.ts)
     * @return ResponseEntity containing the .ts file
     */
    @GetMapping("/{videoId}/{chunk}")
    public ResponseEntity<InputStreamResource> getChunk(
            @PathVariable String videoId,
            @PathVariable String chunk) {

        ResponseInputStream<GetObjectResponse> stream = videoStreamingService.getChunk(videoId, chunk);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, CHUNK_CONTENT_TYPE)
                .body(new InputStreamResource(stream));
    }

    /**
     * Alternative endpoint for retrieving chunk with explicit /chunk route
     * @param videoId The video ID
     * @param chunk The chunk filename (e.g., playlist0.ts)
     * @return ResponseEntity containing the .ts file
     */
    @GetMapping("/{videoId}/chunk/{chunk}")
    public ResponseEntity<InputStreamResource> getChunkExplicit(
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
