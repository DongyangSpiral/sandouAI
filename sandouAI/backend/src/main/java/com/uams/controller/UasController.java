package com.uams.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import com.uams.entity.UApp;
import com.uams.entity.UCorpUser;
import com.uams.entity.ULoginLog;
import com.uams.entity.UUser;
import com.uams.service.UAppService;
import com.uams.service.UCorpUserService;
import com.uams.service.ULoginLogService;
import com.uams.service.UUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uas")
@RequiredArgsConstructor
public class UasController {

    private final UUserService uUserService;
    private final UCorpUserService uCorpUserService;
    private final UAppService uAppService;
    private final ULoginLogService uLoginLogService;

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) Integer status) {
        Page<UUser> page = new Page<>(pageNum, pageSize);
        return Result.ok(uUserService.page(page, phone, status));
    }

    @GetMapping("/user/{id}")
    public Result<?> getUser(@PathVariable Long id) {
        return Result.ok(uUserService.getById(id));
    }

    @PostMapping("/user")
    public Result<?> addUser(@RequestBody UUser user) {
        uUserService.add(user);
        return Result.ok();
    }

    @PutMapping("/user")
    public Result<?> updateUser(@RequestBody UUser user) {
        uUserService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        uUserService.delete(id);
        return Result.ok();
    }

    @PostMapping("/user/batchDelete")
    public Result<?> batchDeleteUsers(@RequestBody List<Long> ids) {
        uUserService.batchDelete(ids);
        return Result.ok();
    }

    @GetMapping("/corp/list")
    public Result<?> corpList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String corpName,
                              @RequestParam(required = false) Integer status) {
        Page<UCorpUser> page = new Page<>(pageNum, pageSize);
        return Result.ok(uCorpUserService.page(page, corpName, status));
    }

    @GetMapping("/corp/{id}")
    public Result<?> getCorp(@PathVariable Long id) {
        return Result.ok(uCorpUserService.getById(id));
    }

    @PostMapping("/corp")
    public Result<?> addCorp(@RequestBody UCorpUser user) {
        uCorpUserService.add(user);
        return Result.ok();
    }

    @PutMapping("/corp")
    public Result<?> updateCorp(@RequestBody UCorpUser user) {
        uCorpUserService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/corp/{id}")
    public Result<?> deleteCorp(@PathVariable Long id) {
        uCorpUserService.delete(id);
        return Result.ok();
    }

    @PostMapping("/corp/batchDelete")
    public Result<?> batchDeleteCorps(@RequestBody List<Long> ids) {
        uCorpUserService.batchDelete(ids);
        return Result.ok();
    }

    @GetMapping("/app/list")
    public Result<?> appList(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String appName) {
        Page<UApp> page = new Page<>(pageNum, pageSize);
        return Result.ok(uAppService.page(page, appName));
    }

    @GetMapping("/app/{id}")
    public Result<?> getApp(@PathVariable Long id) {
        return Result.ok(uAppService.getById(id));
    }

    @PostMapping("/app")
    public Result<?> addApp(@RequestBody UApp app) {
        uAppService.add(app);
        return Result.ok();
    }

    @PutMapping("/app")
    public Result<?> updateApp(@RequestBody UApp app) {
        uAppService.update(app);
        return Result.ok();
    }

    @DeleteMapping("/app/{id}")
    public Result<?> deleteApp(@PathVariable Long id) {
        uAppService.delete(id);
        return Result.ok();
    }

    @GetMapping("/log/list")
    public Result<?> logList(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String loginType,
                             @RequestParam(required = false) String beginTime,
                             @RequestParam(required = false) String endTime) {
        Page<ULoginLog> page = new Page<>(pageNum, pageSize);
        return Result.ok(uLoginLogService.page(page, loginType, beginTime, endTime));
    }
}
