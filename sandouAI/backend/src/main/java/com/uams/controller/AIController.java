package com.uams.controller;

import com.uams.common.Result;
import com.uams.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        return Result.ok(aiService.chat(prompt));
    }

    @GetMapping("/summarize")
    public Result<?> summarize(@RequestParam("fileId") Long fileId) {
        return Result.ok(aiService.summarize(fileId));
    }

    @PostMapping("/analyze")
    public Result<?> analyze(@RequestBody Map<String, Object> body) {
        Long fileId = Long.valueOf(body.get("fileId").toString());
        String question = body.get("question").toString();
        return Result.ok(aiService.analyze(fileId, question));
    }
}