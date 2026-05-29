package com.uams.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import com.uams.entity.DfsFile;
import com.uams.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, String> body) {
        long userId = StpUtil.getLoginIdAsLong();
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId")) : null;
        return Result.ok(folderService.create(body.get("name"), parentId, userId));
    }

    @PutMapping("/rename")
    public Result<?> rename(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        folderService.rename(id, body.get("name").toString());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return Result.ok();
    }

    @GetMapping("/tree")
    public Result<?> tree() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(folderService.tree(userId));
    }

    @PostMapping("/move")
    public Result<?> move(@RequestBody Map<String, Long> params) {
        folderService.move(params.get("id"), params.get("newParentId"));
        return Result.ok();
    }

    @GetMapping("/content")
    public Result<?> content(@RequestParam(value = "folderId", required = false) Long folderId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        Page<DfsFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(folderService.listContent(folderId, userId, page));
    }
}
