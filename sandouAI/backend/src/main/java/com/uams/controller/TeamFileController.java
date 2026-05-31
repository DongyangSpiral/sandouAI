package com.uams.controller;

import com.uams.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/team/file")
@RequiredArgsConstructor
public class TeamFileController {

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file, @RequestParam("teamId") Long teamId) {
        return Result.ok("File uploaded to team " + teamId);
    }

    @GetMapping("/list")
    public Result<?> listFiles(@RequestParam("teamId") Long teamId) {
        return Result.ok();
    }
}