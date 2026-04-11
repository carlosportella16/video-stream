package com.streaming.controller;

import com.streaming.domain.VideoInfo;
import com.streaming.service.VideoInfoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoInfoController {

    private final VideoInfoService videoInfoService;

    public VideoInfoController(VideoInfoService videoInfoService) {
        this.videoInfoService = videoInfoService;
    }

    /** GET /videos — list all videos with metadata */
    @GetMapping
    public ResponseEntity<List<VideoInfo>> listVideos() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(videoInfoService.listVideos());
    }

    /** GET /videos/{videoId} — single video metadata */
    @GetMapping("/{videoId}")
    public ResponseEntity<VideoInfo> getVideoInfo(@PathVariable String videoId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(videoInfoService.getVideoInfo(videoId));
    }
}

