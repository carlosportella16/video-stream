package com.streaming.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine in-memory cache for immutable HLS segments.
 *
 * Weight budget: 150 MB (25 segments × avg ~1.1 MB + headroom).
 * After the async warm-up, every segment request is a sub-millisecond
 * cache hit — the S3 / LocalStack round-trip is completely eliminated.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final long MAX_CACHE_BYTES = 150L * 1024 * 1024; // 150 MB

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("segments");
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumWeight(MAX_CACHE_BYTES)
                .weigher((Object key, Object value) ->
                        value instanceof byte[] bytes ? bytes.length : 1024)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .recordStats());
        manager.setAllowNullValues(false);
        return manager;
    }
}


