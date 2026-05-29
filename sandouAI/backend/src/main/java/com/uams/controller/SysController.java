package com.uams.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uams.common.Result;
import com.uams.entity.SysMenu;
import com.uams.entity.SysRole;
import com.uams.entity.SysUser;
import com.uams.service.SysMenuService;
import com.uams.service.SysRoleService;
import com.uams.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SysController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody SysUser loginUser) {
        SysUser user = sysUserService.login(loginUser.getUsername(), loginUser.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("userInfo", user);
        data.put("roles", StpUtil.getRoleList());
        return Result.ok(data);
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return Result.ok(sysUserService.page(page, username, status));
    }

    @PostMapping("/user")
    public Result<?> addUser(@RequestBody SysUser user) {
        sysUserService.add(user);
        return Result.ok();
    }

    @PutMapping("/user")
    public Result<?> updateUser(@RequestBody SysUser user) {
        sysUserService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok();
    }

    @GetMapping("/user/{id}")
    public Result<?> getUser(@PathVariable Long id) {
        return Result.ok(sysUserService.getById(id));
    }

    @GetMapping("/user/roles/{userId}")
    public Result<?> getUserRoles(@PathVariable Long userId) {
        return Result.ok(sysUserService.getUserRoleIds(userId));
    }

    @PostMapping("/user/assignRoles")
    public Result<?> assignUserRoles(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) params.get("roleIds");
        sysUserService.assignRoles(userId, roleIds);
        return Result.ok();
    }

    @GetMapping("/role/list")
    public Result<?> roleList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        return Result.ok(sysRoleService.page(page, roleName));
    }

    @GetMapping("/role/all")
    public Result<?> roleAll() {
        return Result.ok(sysRoleService.listAll());
    }

    @PostMapping("/role")
    public Result<?> addRole(@RequestBody SysRole role) {
        sysRoleService.add(role);
        return Result.ok();
    }

    @PutMapping("/role")
    public Result<?> updateRole(@RequestBody SysRole role) {
        sysRoleService.update(role);
        return Result.ok();
    }

    @DeleteMapping("/role/{id}")
    public Result<?> deleteRole(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/role/{id}")
    public Result<?> getRole(@PathVariable Long id) {
        return Result.ok(sysRoleService.getById(id));
    }

    @GetMapping("/role/menus/{roleId}")
    public Result<?> getRoleMenuIds(@PathVariable Long roleId) {
        return Result.ok(sysRoleService.getRoleMenuIds(roleId));
    }

    @PostMapping("/role/assignMenus")
    public Result<?> assignRoleMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuIds = (List<Long>) params.get("menuIds");
        sysRoleService.assignMenus(roleId, menuIds);
        return Result.ok();
    }

    @GetMapping("/menu/tree")
    public Result<?> menuTree() {
        return Result.ok(sysMenuService.tree());
    }

    @GetMapping("/menu/list")
    public Result<?> menuList() {
        return Result.ok(sysMenuService.listAll());
    }

    @PostMapping("/menu")
    public Result<?> addMenu(@RequestBody SysMenu menu) {
        sysMenuService.add(menu);
        return Result.ok();
    }

    @PutMapping("/menu")
    public Result<?> updateMenu(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return Result.ok();
    }

    @DeleteMapping("/menu/{id}")
    public Result<?> deleteMenu(@PathVariable Long id) {
        sysMenuService.delete(id);
        return Result.ok();
    }
}
