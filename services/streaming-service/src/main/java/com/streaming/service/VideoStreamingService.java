package com.streaming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
public class VideoStreamingService {

    private final S3VideoStorageService S3Service;

    public VideoStreamingService(S3VideoStorageService s3Service) {
        S3Service = s3Service;
    }

    public ResponseInputStream<GetObjectResponse> getPlaylist(String videoId) {
        String key = videoId + "/playlist.m3u8";
        return S3Service.getObject(key);
    }

    public ResponseInputStream<GetObjectResponse> getChunk(String videoId, String chunk) {
        String key = videoId + "/" + chunk;
        return S3Service.getObject(key);
    }
}
