package com.uams.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.*;
import com.uams.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Page<SysUser> page(Page<SysUser> page, String username, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(username), SysUser::getUsername, username);
        wrapper.eq(status != null, SysUser::getStatus, status);
        wrapper.orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void add(SysUser user) {
        if (exists(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.insert(user);
    }

    @Transactional
    public void update(SysUser user) {
        SysUser exist = baseMapper.selectById(user.getId());
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!exist.getUsername().equals(user.getUsername()) && exists(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        baseMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);
        if (roleIds != null && !roleIds.isEmpty()) {
            roleIds.forEach(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            });
        }
    }

    public List<Long> getUserRoleIds(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return sysUserRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    public SysUser login(String username, String password) {
        log.info("login attempt: username={}", username);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = baseMapper.selectOne(wrapper);
        log.info("found user: {}", user != null ? user.getUsername() : "null");
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        log.info("password stored length: {}", user.getPassword() != null ? user.getPassword().length() : 0);
        boolean pwMatch = passwordEncoder.matches(password, user.getPassword());
        log.info("password match: {}", pwMatch);
        if (!pwMatch) {
            throw new RuntimeException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        user.setLoginIp("127.0.0.1");
        user.setLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);
        return user;
    }

    private boolean exists(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
