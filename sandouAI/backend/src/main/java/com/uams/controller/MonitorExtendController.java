package com.uams.controller;

import com.uams.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorExtendController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/online/list")
    public Result<?> onlineList() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT id, username, nickname, login_ip, login_time FROM sys_user WHERE status=1 AND del_flag=0");
        return Result.ok(list);
    }

    @GetMapping("/job/list")
    public Result<?> jobList() {
        List<Map<String, Object>> jobs = new ArrayList<>();
        Map<String, Object> j1 = new LinkedHashMap<>();
        j1.put("id", 1); j1.put("jobName", "清理过期日志"); j1.put("cronExpression", "0 0 2 * * ?");
        j1.put("className", "com.uams.task.CleanLogTask"); j1.put("status", 1); j1.put("nextFireTime", "");
        jobs.add(j1);
        return Result.ok(jobs);
    }

    @PostMapping("/job/execute/{id}")
    public Result<?> executeJob(@PathVariable Long id) {
        return Result.ok("任务已触发");
    }
}
