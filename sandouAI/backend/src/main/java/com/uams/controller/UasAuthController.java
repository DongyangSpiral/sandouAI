package com.uams.controller;

import com.uams.common.Result;
import com.uams.entity.UCorpUser;
import com.uams.service.UasAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/uas/auth")
@RequiredArgsConstructor
public class UasAuthController {

    private final UasAuthService uasAuthService;

    @PostMapping("/password")
    public Result<?> passwordLogin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phone = body.get("phone");
        String password = body.get("password");
        String ip = getIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.ok(uasAuthService.passwordLogin(phone, password, ip, userAgent));
    }

    @PostMapping("/sms/send")
    public Result<?> sendSms(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        uasAuthService.sendSmsCode(phone);
        return Result.ok();
    }

    @PostMapping("/sms/login")
    public Result<?> smsLogin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phone = body.get("phone");
        String code = body.get("code");
        String ip = getIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.ok(uasAuthService.smsLogin(phone, code, ip, userAgent));
    }

    @PostMapping("/enterprise/corps")
    public Result<?> getCorps(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        List<UCorpUser> corps = uasAuthService.getCorpsByPhone(phone);
        return Result.ok(corps);
    }

    @PostMapping("/enterprise/login")
    public Result<?> enterpriseLogin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String corpId = body.get("corpId");
        String password = body.get("password");
        String ip = getIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.ok(uasAuthService.enterpriseLogin(corpId, password, ip, userAgent));
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
