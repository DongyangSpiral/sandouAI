package com.uams.controller;

import cn.hutool.core.util.StrUtil;
import com.uams.common.Result;
import com.uams.entity.UApp;
import com.uams.mapper.UAppMapper;
import com.uams.service.UasAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UAppMapper uAppMapper;
    private final UasAuthService uasAuthService;

    private final Map<String, String> authCodeStore = new ConcurrentHashMap<>();

    @GetMapping("/authorize")
    public Result<?> authorize(@RequestParam String appKey, @RequestParam String redirectUri) {
        UApp app = uAppMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UApp>()
                        .eq(UApp::getAppKey, appKey));
        if (app == null || app.getStatus() == 0) {
            return Result.error("应用不存在或已禁用");
        }
        String code = UUID.randomUUID().toString().replace("-", "");
        authCodeStore.put(code, appKey);
        return Result.ok(Map.of("code", code, "redirectUri", redirectUri));
    }

    @PostMapping("/token")
    public Result<?> token(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String appKey = body.get("appKey");
        String appSecret = body.get("appSecret");
        String storedAppKey = authCodeStore.get(code);
        if (storedAppKey == null || !storedAppKey.equals(appKey)) {
            return Result.error("授权码无效");
        }
        UApp app = uAppMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UApp>()
                        .eq(UApp::getAppKey, appKey));
        if (app == null || !app.getAppSecret().equals(appSecret)) {
            return Result.error("AppKey或AppSecret错误");
        }
        authCodeStore.remove(code);
        return Result.ok(Map.of("access_token", UUID.randomUUID().toString().replace("-", ""),
                "expires_in", 7200));
    }
}
