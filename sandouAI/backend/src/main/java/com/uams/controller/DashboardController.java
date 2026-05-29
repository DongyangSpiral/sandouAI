package com.uams.controller;

import com.uams.common.Result;
import com.uams.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final UUserMapper uUserMapper;
    private final UCorpUserMapper uCorpUserMapper;
    private final UAppMapper uAppMapper;
    private final ULoginLogMapper uLoginLogMapper;

    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("sysUserCount", sysUserMapper.selectCount(null));
        result.put("sysRoleCount", sysRoleMapper.selectCount(null));
        result.put("naturalUserCount", uUserMapper.selectCount(null));
        result.put("corpUserCount", uCorpUserMapper.selectCount(null));
        result.put("appCount", uAppMapper.selectCount(null));
        result.put("loginLogCount", uLoginLogMapper.selectCount(null));

        return Result.ok(result);
    }

    @GetMapping("/chart")
    public Result<?> chart() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<String> months = Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月");
        List<Integer> userReg = Arrays.asList(12, 18, 25, 22, 30, 45, 38, 42, 35, 28, 33, 40);
        List<Integer> loginCount = Arrays.asList(80, 95, 110, 100, 130, 160, 145, 155, 140, 125, 150, 170);

        result.put("months", months);
        result.put("userRegistration", userReg);
        result.put("loginCount", loginCount);

        return Result.ok(result);
    }
}
