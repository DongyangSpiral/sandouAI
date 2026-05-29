package com.uams.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import com.uams.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "folderId", required = false) Long folderId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(fileService.upload(file, folderId, userId));
    }

    @PostMapping("/batchUpload")
    public Result<?> batchUpload(@RequestParam("files") MultipartFile[] files,
                                 @RequestParam(value = "folderId", required = false) Long folderId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(fileService.batchUpload(java.util.Arrays.asList(files), folderId, userId));
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.ok();
    }

    @PutMapping("/rename")
    public Result<?> rename(@RequestBody com.uams.entity.DfsFile file) {
        fileService.rename(file.getId(), file.getName());
        return Result.ok();
    }

    @PostMapping("/move")
    public Result<?> move(@RequestBody java.util.Map<String, Long> params) {
        fileService.move(params.get("id"), params.get("targetFolderId"));
        return Result.ok();
    }

    @PostMapping("/copy")
    public Result<?> copy(@RequestBody java.util.Map<String, Long> params) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(fileService.copy(params.get("id"), params.get("targetFolderId"), userId));
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(value = "folderId", required = false) Long folderId,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String extension) {
        Page<com.uams.entity.DfsFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(fileService.listByFolder(folderId, page, name, extension));
    }

    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Long id) {
        return Result.ok(fileService.getById(id));
    }
}
