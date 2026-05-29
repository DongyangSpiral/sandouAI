package com.uams.service;

import com.uams.config.DfsConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "dfs.storage.type", havingValue = "local")
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final DfsConfig dfsConfig;
    private Path basePath;

    @PostConstruct
    public void init() {
        basePath = Paths.get(dfsConfig.getLocal().getPath());
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            log.error("Failed to create storage directory: {}", basePath, e);
        }
        log.info("Local storage initialized at: {}", basePath);
    }

    @Override
    public String upload(String bucket, String fileName, InputStream inputStream, long size) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            ext = fileName.substring(dot);
        }
        String objectName = dateDir + "/" + uuid + ext;

        Path filePath = basePath.resolve(bucket).resolve(objectName);
        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath);
            log.info("File saved: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filePath, e);
        }
        return objectName;
    }

    @Override
    public void download(String storagePath, OutputStream outputStream) {
        Path filePath = basePath.resolve(storagePath);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + storagePath);
        }
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        Path filePath = basePath.resolve(storagePath);
        try {
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    @Override
    public String getPreviewUrl(String storagePath) {
        return basePath.resolve(storagePath).toAbsolutePath().toString();
    }
}
