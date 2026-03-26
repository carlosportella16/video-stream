package com.streaming.controller;

import com.streaming.service.VideoStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoStreamingService service;

    public VideoController(VideoStreamingService service) {
        this.service = service;
    }

    @GetMapping("/{videoId}/playlist")
    public ResponseEntity<InputStreamResource> getPlaylist(@PathVariable String videoId) {

        ResponseInputStream<GetObjectResponse> stream = service.getPlaylist(videoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/{videoId}/chunk/{chunk}")
    public ResponseEntity<InputStreamResource> getChunk(
            @PathVariable String videoId,
            @PathVariable String chunk) {

        ResponseInputStream<GetObjectResponse> stream = service.getChunk(videoId, chunk);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/MP2T")
                .body(new InputStreamResource(stream));
    }
}
