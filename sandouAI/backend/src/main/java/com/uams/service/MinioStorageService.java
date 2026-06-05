package com.uams.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "dfs.storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    @Value("${dfs.storage.minio.endpoint}")
    private String endpoint;

    @Value("${dfs.storage.minio.access-key}")
    private String accessKey;

    @Value("${dfs.storage.minio.secret-key}")
    private String secretKey;

    @Value("${dfs.storage.minio.bucket}")
    private String defaultBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            log.error("MinIO bucket initialization failed for bucket: " + bucket, e);
        }
    }

    @Override
    public String upload(String bucket, String fileName, InputStream inputStream, long size) {
        String targetBucket = (bucket != null && !bucket.isEmpty()) ? bucket : defaultBucket;
        ensureBucketExists(targetBucket);
        
        String storagePath = UUID.randomUUID().toString() + "-" + fileName;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(storagePath)
                    .stream(inputStream, size, -1)
                    .build());
            return storagePath;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    @Override
    public void download(String storagePath, OutputStream outputStream) {
        String[] parts = storagePath.split("/", 2);
        String bucket = parts.length > 1 ? parts[0] : defaultBucket;
        String path = parts.length > 1 ? parts[1] : storagePath;

        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build())) {
            stream.transferTo(outputStream);
        } catch (Exception e) {
            log.error("MinIO download failed", e);
            throw new RuntimeException("MinIO download failed", e);
        }
    }

    @Override
    public void delete(String storagePath) {
        String[] parts = storagePath.split("/", 2);
        String bucket = parts.length > 1 ? parts[0] : defaultBucket;
        String path = parts.length > 1 ? parts[1] : storagePath;
        
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
        } catch (Exception e) {
            log.error("MinIO delete failed", e);
            throw new RuntimeException("MinIO delete failed", e);
        }
    }

    @Override
    public String getPreviewUrl(String storagePath) {
        String[] parts = storagePath.split("/", 2);
        String bucket = parts.length > 1 ? parts[0] : defaultBucket;
        String path = parts.length > 1 ? parts[1] : storagePath;
        
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .method(Method.GET)
                    .build());
        } catch (Exception e) {
            log.error("MinIO get preview url failed", e);
            return null;
        }
    }
}
