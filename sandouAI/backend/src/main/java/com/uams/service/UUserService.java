package com.uams.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.UUser;
import com.uams.mapper.UUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UUserService extends ServiceImpl<UUserMapper, UUser> {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Page<UUser> page(Page<UUser> page, String phone, Integer status) {
        LambdaQueryWrapper<UUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(phone), UUser::getPhone, phone);
        wrapper.eq(status != null, UUser::getStatus, status);
        wrapper.orderByDesc(UUser::getCreateTime);
        Page<UUser> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::maskSensitive);
        return result;
    }

    public UUser getById(Long id) {
        UUser user = baseMapper.selectById(id);
        if (user != null) {
            maskSensitive(user);
        }
        return user;
    }

    @Transactional
    public void add(UUser user) {
        if (exists(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        baseMapper.insert(user);
    }

    @Transactional
    public void update(UUser user) {
        UUser exist = baseMapper.selectById(user.getId());
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!exist.getPhone().equals(user.getPhone()) && exists(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
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
    public void batchDelete(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    private boolean exists(String phone) {
        LambdaQueryWrapper<UUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UUser::getPhone, phone);
        return baseMapper.selectCount(wrapper) > 0;
    }

    private void maskSensitive(UUser user) {
        if (user.getPhone() != null && user.getPhone().length() >= 7) {
            user.setPhoneMasked(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
        }
        if (user.getIdCardNo() != null && user.getIdCardNo().length() >= 8) {
            user.setIdCardNoMasked(user.getIdCardNo().substring(0, 4) + "**********" + user.getIdCardNo().substring(user.getIdCardNo().length() - 4));
        }
    }
}
