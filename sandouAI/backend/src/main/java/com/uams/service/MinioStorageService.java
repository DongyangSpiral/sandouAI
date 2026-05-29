package com.uams.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Service
@ConditionalOnProperty(name = "dfs.storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    @Override
    public String upload(String bucket, String fileName, InputStream inputStream, long size) {
        throw new UnsupportedOperationException("MinIO not yet implemented");
    }

    @Override
    public void download(String storagePath, OutputStream outputStream) {
        throw new UnsupportedOperationException("MinIO not yet implemented");
    }

    @Override
    public void delete(String storagePath) {
        throw new UnsupportedOperationException("MinIO not yet implemented");
    }

    @Override
    public String getPreviewUrl(String storagePath) {
        throw new UnsupportedOperationException("MinIO not yet implemented");
    }
}
