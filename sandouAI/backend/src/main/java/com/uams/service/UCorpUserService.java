package com.uams.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.UCorpUser;
import com.uams.mapper.UCorpUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UCorpUserService extends ServiceImpl<UCorpUserMapper, UCorpUser> {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Page<UCorpUser> page(Page<UCorpUser> page, String corpName, Integer status) {
        LambdaQueryWrapper<UCorpUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(corpName), UCorpUser::getCorpName, corpName);
        wrapper.eq(status != null, UCorpUser::getStatus, status);
        wrapper.orderByDesc(UCorpUser::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void add(UCorpUser user) {
        if (existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }
        if (existsByCreditCode(user.getCreditCode())) {
            throw new RuntimeException("统一社会信用代码已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.insert(user);
    }

    @Transactional
    public void update(UCorpUser user) {
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

    private boolean existsByPhone(String phone) {
        LambdaQueryWrapper<UCorpUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UCorpUser::getPhone, phone);
        return baseMapper.selectCount(wrapper) > 0;
    }

    private boolean existsByCreditCode(String creditCode) {
        LambdaQueryWrapper<UCorpUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UCorpUser::getCreditCode, creditCode);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
