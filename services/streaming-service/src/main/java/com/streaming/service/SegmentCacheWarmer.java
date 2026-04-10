package com.streaming.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Eagerly pre-fetches every HLS segment into the Caffeine cache as soon as
 * the application is ready.  By calling through the Spring proxy, the
 * {@code @Cacheable} advice on {@link VideoStreamingService#getChunkBytes}
 * is properly triggered, so all segments land in cache before the first
 * user ever presses Play.
 *
 * <p>Runs on a virtual thread (via {@code @Async}) so it never delays
 * the HTTP server from accepting requests.
 */
@Component
public class SegmentCacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(SegmentCacheWarmer.class);

    /** Comma-separated list of video IDs to pre-warm on startup. */
    private static final String[] WARMUP_VIDEOS = {"movie1"};

    private final VideoStreamingService streamingService;

    public SegmentCacheWarmer(VideoStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        for (String videoId : WARMUP_VIDEOS) {
            warmUpVideo(videoId);
        }
    }

    private void warmUpVideo(String videoId) {
        log.info("[CacheWarmer] Pre-fetching segments for '{}'…", videoId);
        long start = System.currentTimeMillis();
        int loaded = 0;

        try {
            // Always pre-fetch the init segment
            streamingService.getChunkBytes(videoId, "init.mp4");
            loaded++;

            // Read playlist → get all segment names → pre-fetch each one
            List<String> segments = streamingService.getSegmentNames(videoId);
            for (String seg : segments) {
                try {
                    streamingService.getChunkBytes(videoId, seg);
                    loaded++;
                } catch (Exception e) {
                    log.warn("[CacheWarmer] Skipping {}/{}: {}", videoId, seg, e.getMessage());
                }
            }

            long elapsed = System.currentTimeMillis() - start;
            log.info("[CacheWarmer] '{}' ready — {} segments cached in {} ms", videoId, loaded, elapsed);

        } catch (Exception e) {
            log.warn("[CacheWarmer] Warm-up failed for '{}' after {} ms: {}",
                    videoId, System.currentTimeMillis() - start, e.getMessage());
        }
    }
}

