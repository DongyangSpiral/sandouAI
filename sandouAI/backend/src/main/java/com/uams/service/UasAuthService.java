package com.uams.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uams.entity.*;
import com.uams.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UasAuthService {

    private final UUserMapper uUserMapper;
    private final UCorpUserMapper uCorpUserMapper;
    private final ULoginLogMapper uLoginLogMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String SMS_PREFIX = "sms:";
    private static final String SMS_QUEUE = "sms.queue";

    public Map<String, Object> passwordLogin(String phone, String password, String ip, String userAgent) {
        UUser user = uUserMapper.selectOne(
                new LambdaQueryWrapper<UUser>().eq(UUser::getPhone, phone));
        if (user == null) {
            saveLog(null, "password", phone, ip, userAgent, 0, "用户不存在");
            throw new RuntimeException("手机号或密码错误");
        }
        if (user.getStatus() == 0) {
            saveLog(user.getId(), "password", phone, ip, userAgent, 0, "账号已禁用");
            throw new RuntimeException("账号已被禁用");
        }
        if (StrUtil.isBlank(user.getPassword()) || !passwordEncoder.matches(password, user.getPassword())) {
            saveLog(user.getId(), "password", phone, ip, userAgent, 0, "密码错误");
            throw new RuntimeException("手机号或密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        uUserMapper.updateById(user);
        saveLog(user.getId(), "password", phone, ip, userAgent, 1, null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        return result;
    }

    public void sendSmsCode(String phone) {
        UUser user = uUserMapper.selectOne(
                new LambdaQueryWrapper<UUser>().eq(UUser::getPhone, phone));
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(SMS_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        rabbitTemplate.convertAndSend(SMS_QUEUE, msg);
    }

    public Map<String, Object> smsLogin(String phone, String code, String ip, String userAgent) {
        String cachedCode = stringRedisTemplate.opsForValue().get(SMS_PREFIX + phone);
        if (cachedCode == null || !cachedCode.equals(code)) {
            saveLog(null, "sms", phone, ip, userAgent, 0, "验证码错误");
            throw new RuntimeException("验证码错误或已过期");
        }
        UUser user = uUserMapper.selectOne(
                new LambdaQueryWrapper<UUser>().eq(UUser::getPhone, phone));
        if (user == null || user.getStatus() == 0) {
            saveLog(null, "sms", phone, ip, userAgent, 0, "用户不存在或已禁用");
            throw new RuntimeException("用户不存在或已禁用");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        stringRedisTemplate.delete(SMS_PREFIX + phone);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        uUserMapper.updateById(user);
        saveLog(user.getId(), "sms", phone, ip, userAgent, 1, null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        return result;
    }

    public List<UCorpUser> getCorpsByPhone(String phone) {
        return uCorpUserMapper.selectList(
                new LambdaQueryWrapper<UCorpUser>().eq(UCorpUser::getPhone, phone));
    }

    public Map<String, Object> enterpriseLogin(String corpId, String password, String ip, String userAgent) {
        UCorpUser corpUser = uCorpUserMapper.selectById(corpId);
        if (corpUser == null) {
            saveLog(null, "enterprise", "", ip, userAgent, 0, "企业不存在");
            throw new RuntimeException("企业不存在");
        }
        if (corpUser.getStatus() == 0) {
            saveLog(null, "enterprise", corpUser.getPhone(), ip, userAgent, 0, "企业已禁用");
            throw new RuntimeException("企业已被禁用");
        }
        if (!passwordEncoder.matches(password, corpUser.getPassword())) {
            saveLog(null, "enterprise", corpUser.getPhone(), ip, userAgent, 0, "密码错误");
            throw new RuntimeException("密码错误");
        }
        StpUtil.login(corpUser.getId());
        String token = StpUtil.getTokenValue();
        corpUser.setLastLoginTime(LocalDateTime.now());
        corpUser.setLastLoginIp(ip);
        uCorpUserMapper.updateById(corpUser);
        saveLog(corpUser.getId(), "enterprise", corpUser.getPhone(), ip, userAgent, 1, null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("corpInfo", corpUser);
        return result;
    }

    private void saveLog(Long userId, String loginType, String username, String ip, String userAgent, Integer status, String errorMsg) {
        ULoginLog log = new ULoginLog();
        log.setUserId(userId);
        log.setLoginType(loginType);
        log.setUsername(username);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        uLoginLogMapper.insert(log);
    }
}
