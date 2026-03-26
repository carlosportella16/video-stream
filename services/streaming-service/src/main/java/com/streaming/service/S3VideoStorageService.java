package com.streaming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
public class S3VideoStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket")
    private String bucket;

    public S3VideoStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }
}
