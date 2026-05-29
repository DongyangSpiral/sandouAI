package com.uams.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.ULoginLog;
import com.uams.mapper.ULoginLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ULoginLogService extends ServiceImpl<ULoginLogMapper, ULoginLog> {

    public Page<ULoginLog> page(Page<ULoginLog> page, String loginType, String beginTime, String endTime) {
        LambdaQueryWrapper<ULoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(loginType), ULoginLog::getLoginType, loginType);
        wrapper.ge(StrUtil.isNotBlank(beginTime), ULoginLog::getCreateTime, beginTime);
        wrapper.le(StrUtil.isNotBlank(endTime), ULoginLog::getCreateTime, endTime);
        wrapper.orderByDesc(ULoginLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
