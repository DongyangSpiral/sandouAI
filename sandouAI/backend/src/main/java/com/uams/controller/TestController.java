package com.uams.controller;

import com.uams.entity.SysUser;
import com.uams.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class TestController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/api/test/init-users")
    public String initUsers() {
        String hash = "$2a$10$c0egNfa7KlBRRi2o6M5FqOqv/zk8BpeibHYlg0H2nt.IFFt3LFAq2";
        for (int i = 1; i <= 3; i++) {
            SysUser u = new SysUser();
            u.setUsername("testuser" + i);
            u.setPassword(hash);
            u.setNickname("测试用户 " + i);
            u.setStatus(1);
            u.setDelFlag(0);
            u.setCreateTime(LocalDateTime.now());
            u.setUpdateTime(LocalDateTime.now());
            try {
                sysUserMapper.insert(u);
            } catch (Exception e) {} // ignore duplicates
        }
        return "Test users injected successfully!";
    }
}
