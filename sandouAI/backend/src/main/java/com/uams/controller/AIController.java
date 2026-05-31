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
}