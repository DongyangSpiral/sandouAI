package com.uams.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.UApp;
import com.uams.mapper.UAppMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UAppService extends ServiceImpl<UAppMapper, UApp> {

    public Page<UApp> page(Page<UApp> page, String appName) {
        LambdaQueryWrapper<UApp> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(appName), UApp::getAppName, appName);
        wrapper.orderByDesc(UApp::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void add(UApp app) {
        app.setAppKey(IdUtil.fastSimpleUUID().substring(0, 16));
        app.setAppSecret(IdUtil.fastSimpleUUID());
        baseMapper.insert(app);
    }

    @Transactional
    public void update(UApp app) {
        baseMapper.updateById(app);
    }

    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
}
