package com.streaming.integration;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Service
public class S3VideoStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3VideoStorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3VideoStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void initializeBucket() {
        try {
            // Check if bucket exists
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket '{}' already exists", bucket);
        } catch (Exception e) {
            // If it doesn't exist, create it
            log.info("Creating bucket '{}'...", bucket);
            try {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build());
                log.info("Bucket '{}' created successfully!", bucket);
            } catch (Exception createException) {
                log.error("Error creating bucket '{}': {}", bucket, createException.getMessage(), createException);
                throw new RuntimeException("Failed to initialize S3 bucket", createException);
            }
        }
    }

    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }
}
