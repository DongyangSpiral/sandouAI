package com.uams.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface StorageService {

    String upload(String bucket, String fileName, InputStream inputStream, long size);

    void download(String storagePath, OutputStream outputStream);

    void delete(String storagePath);

    String getPreviewUrl(String storagePath);
}
