package com.uams.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uams.common.Result;
import com.uams.entity.DfsFile;
import com.uams.entity.TeamFile;
import com.uams.mapper.TeamFileMapper;
import com.uams.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/team/file")
@RequiredArgsConstructor
public class TeamFileController {

    private final FileService fileService;
    private final TeamFileMapper teamFileMapper;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file, @RequestParam("teamId") Long teamId) {
        long userId = com.uams.common.AuthUtil.getUserId();
        log.info("=== TEST LOG: API /api/team/file/upload called ===");
        log.info("TeamId: {}, File name: {}, userId: {}", teamId, file.getOriginalFilename(), userId);
        
        try {
            // 1. Upload to MinIO/storage via FileService
            DfsFile dfsFile = fileService.upload(file, null, userId);
            log.info("=== TEST LOG: MinIO upload success, DfsFile ID: {} ===", dfsFile.getId());
            
            // 2. Link file to team (check for duplicate first)
            Long fileId = dfsFile.getId();
            Long count = teamFileMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamFile>()
                    .eq(TeamFile::getTeamId, teamId)
                    .eq(TeamFile::getFileId, fileId)
            );
            
            if (count == 0) {
                TeamFile teamFile = new TeamFile();
                teamFile.setTeamId(teamId);
                teamFile.setFileId(fileId);
                teamFile.setFolderId(0L);
                teamFile.setPermission("read/write");
                teamFile.setCreateBy(userId);
                teamFileMapper.insert(teamFile);
                log.info("=== TEST LOG: Team file link created successfully ===");
            } else {
                log.info("=== TEST LOG: Team file link already exists ===");
            }
            
            return Result.ok("File uploaded successfully");
        } catch (Exception e) {
            log.error("=== TEST LOG: Team file upload failed ===", e);
            return Result.error("Team upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<?> listFiles(@RequestParam("teamId") Long teamId) {
        log.info("=== TEST LOG: API /api/team/file/list called ===");
        log.info("Fetching files for teamId: {}", teamId);
        List<TeamFile> teamFiles = teamFileMapper.selectList(
                new LambdaQueryWrapper<TeamFile>().eq(TeamFile::getTeamId, teamId)
        );
        log.info("=== TEST LOG: Found {} file links for team ===", teamFiles.size());
        
        if (teamFiles.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        
        List<Long> fileIds = teamFiles.stream()
                .map(TeamFile::getFileId)
                .collect(Collectors.toList());
                
        List<DfsFile> files = fileService.listByIds(fileIds);
        log.info("=== TEST LOG: Found {} physical files ===", files.size());
        return Result.ok(files);
    }

    @DeleteMapping("/delete")
    public Result<?> deleteTeamFile(@RequestParam("teamId") Long teamId, @RequestParam("fileId") Long fileId) {
        long userId = com.uams.common.AuthUtil.getUserId();
        if (userId != 1L) {
            return Result.error("只有系统管理员才能删除团队文件");
        }
        teamFileMapper.delete(new LambdaQueryWrapper<TeamFile>()
                .eq(TeamFile::getTeamId, teamId)
                .eq(TeamFile::getFileId, fileId));
        return Result.ok("团队文件已移除");
    }
}