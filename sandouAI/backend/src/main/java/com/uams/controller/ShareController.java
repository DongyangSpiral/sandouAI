package com.uams.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import com.uams.entity.DfsShare;
import com.uams.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        Long fileId = body.get("fileId") != null ? Long.valueOf(body.get("fileId").toString()) : null;
        Long folderId = body.get("folderId") != null ? Long.valueOf(body.get("folderId").toString()) : null;
        String password = (String) body.get("password");
        LocalDateTime expireTime = body.get("expireTime") != null
                ? LocalDateTime.parse(body.get("expireTime").toString())
                : null;
        Boolean allowDownload = body.get("allowDownload") != null
                ? Boolean.valueOf(body.get("allowDownload").toString())
                : true;
        return Result.ok(shareService.create(fileId, folderId, password, expireTime, allowDownload, userId));
    }

    @PostMapping("/access/{code}")
    public Result<?> access(@PathVariable String code,
                            @RequestBody(required = false) Map<String, String> body) {
        String password = body != null ? body.get("password") : null;
        return Result.ok(shareService.access(code, password));
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        Page<DfsShare> page = new Page<>(pageNum, pageSize);
        return Result.ok(shareService.listByUser(userId, page));
    }

    @DeleteMapping("/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        shareService.cancel(id, userId);
        return Result.ok();
    }
}
