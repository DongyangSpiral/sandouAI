package com.uams.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.DfsFile;
import com.uams.entity.DfsFileFolder;
import com.uams.entity.DfsFileVersion;
import com.uams.mapper.DfsFileFolderMapper;
import com.uams.mapper.DfsFileMapper;
import com.uams.mapper.DfsFileVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService extends ServiceImpl<DfsFileMapper, DfsFile> {

    private final StorageService storageService;
    private final DfsFileFolderMapper dfsFileFolderMapper;
    private final DfsFileVersionMapper dfsFileVersionMapper;

    public DfsFile upload(MultipartFile file, Long folderId, Long userId) {
        String fileName = file.getOriginalFilename();
        String ext = "";
        int dot = fileName != null ? fileName.lastIndexOf('.') : -1;
        if (dot > 0) {
            ext = fileName.substring(dot).toLowerCase();
        }

        String md5 = calculateMd5(file);
        DfsFile existing = findByMd5(md5);
        DfsFile dfsFile;
        if (existing != null) {
            dfsFile = existing;
        } else {
            String storagePath = storageService.upload("personal", fileName, getInputStream(file), file.getSize());
            dfsFile = new DfsFile();
            dfsFile.setName(fileName);
            dfsFile.setExtension(ext);
            dfsFile.setMimeType(file.getContentType());
            dfsFile.setSize(file.getSize());
            dfsFile.setMd5(md5);
            dfsFile.setStoragePath(storagePath);
            dfsFile.setBucket("personal");
            dfsFile.setOwnerId(userId);
            dfsFile.setStatus(1);
            baseMapper.insert(dfsFile);
        }

        linkFileToFolder(dfsFile.getId(), folderId);

        return dfsFile;
    }

    public List<DfsFile> batchUpload(List<MultipartFile> files, Long folderId, Long userId) {
        List<DfsFile> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(upload(file, folderId, userId));
        }
        return result;
    }

    public void download(Long fileId, jakarta.servlet.http.HttpServletResponse response) {
        DfsFile dfsFile = baseMapper.selectById(fileId);
        if (dfsFile == null) {
            throw new RuntimeException("文件不存在");
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + dfsFile.getName());
        try {
            storageService.download(dfsFile.getBucket() + "/" + dfsFile.getStoragePath(), response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Transactional
    public void deleteFile(Long fileId) {
        baseMapper.deleteById(fileId);
        LambdaQueryWrapper<DfsFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFileFolder::getFileId, fileId);
        dfsFileFolderMapper.delete(wrapper);
    }

    @Transactional
    public void rename(Long fileId, String newName) {
        DfsFile dfsFile = baseMapper.selectById(fileId);
        if (dfsFile == null) {
            throw new RuntimeException("文件不存在");
        }
        dfsFile.setName(newName);
        baseMapper.updateById(dfsFile);
    }

    @Transactional
    public void move(Long fileId, Long targetFolderId) {
        LambdaQueryWrapper<DfsFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFileFolder::getFileId, fileId);
        DfsFileFolder relation = dfsFileFolderMapper.selectOne(wrapper);
        if (relation != null) {
            dfsFileFolderMapper.delete(wrapper);
        }
        linkFileToFolder(fileId, targetFolderId);
    }

    @Transactional
    public DfsFile copy(Long fileId, Long targetFolderId, Long userId) {
        DfsFile source = baseMapper.selectById(fileId);
        if (source == null) {
            throw new RuntimeException("文件不存在");
        }
        DfsFile copy = new DfsFile();
        copy.setName(source.getName());
        copy.setExtension(source.getExtension());
        copy.setMimeType(source.getMimeType());
        copy.setSize(source.getSize());
        copy.setMd5(source.getMd5());
        copy.setStoragePath(source.getStoragePath());
        copy.setBucket(source.getBucket());
        copy.setOwnerId(userId);
        copy.setStatus(1);
        baseMapper.insert(copy);
        linkFileToFolder(copy.getId(), targetFolderId);
        return copy;
    }

    public Page<DfsFile> listByFolder(Long folderId, Page<DfsFile> page, String name, String extension) {
        // 当未传入 folderId 时默认查询根目录（folderId = 0）
        if (folderId == null) {
            folderId = 0L;
        }
        LambdaQueryWrapper<DfsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(DfsFile::getId, "SELECT file_id FROM dfs_file_folder WHERE folder_id=" + folderId);
        if (name != null && !name.isEmpty()) {
            wrapper.like(DfsFile::getName, name);
        }
        if (extension != null && !extension.isEmpty()) {
            wrapper.eq(DfsFile::getExtension, extension);
        }
        wrapper.orderByDesc(DfsFile::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    private DfsFile findByMd5(String md5) {
        LambdaQueryWrapper<DfsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFile::getMd5, md5);
        return baseMapper.selectOne(wrapper);
    }

    private void linkFileToFolder(Long fileId, Long folderId) {
        Long finalFolderId = folderId != null ? folderId : 0L;
        LambdaQueryWrapper<DfsFileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsFileFolder::getFileId, fileId).eq(DfsFileFolder::getFolderId, finalFolderId);
        if (dfsFileFolderMapper.selectCount(wrapper) == 0) {
            try {
                DfsFileFolder relation = new DfsFileFolder();
                relation.setFileId(fileId);
                relation.setFolderId(finalFolderId);
                dfsFileFolderMapper.insert(relation);
            } catch (Exception e) {
                log.warn("Link file to folder ignored due to existing relation or error: {}", e.getMessage());
            }
        }
    }

    private String calculateMd5(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             DigestInputStream dis = new DigestInputStream(is, MessageDigest.getInstance("MD5"))) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
            }
            return HexFormat.of().formatHex(dis.getMessageDigest().digest());
        } catch (Exception e) {
            throw new RuntimeException("MD5 calculation failed", e);
        }
    }

    private InputStream getInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read upload file", e);
        }
    }
}
