package com.uams.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uams.entity.DfsFile;
import com.uams.entity.DfsFolder;
import com.uams.entity.DfsShare;
import com.uams.mapper.DfsFileMapper;
import com.uams.mapper.DfsFolderMapper;
import com.uams.mapper.DfsShareMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShareService extends ServiceImpl<DfsShareMapper, DfsShare> {

    private final BCryptPasswordEncoder passwordEncoder;
    private final DfsFileMapper dfsFileMapper;
    private final DfsFolderMapper dfsFolderMapper;

    @Transactional
    public DfsShare create(Long fileId, Long folderId, String password,
                           LocalDateTime expireTime, Boolean allowDownload, Long userId) {
        DfsShare share = new DfsShare();
        share.setCode(IdUtil.fastSimpleUUID());
        share.setFileId(fileId);
        share.setFolderId(folderId);
        if (password != null && !password.isEmpty()) {
            share.setPassword(passwordEncoder.encode(password));
        }
        share.setExpireTime(expireTime);
        share.setAllowDownload(allowDownload != null && allowDownload ? 1 : 0);
        share.setVisitCount(0);
        share.setStatus(1);
        share.setCreateBy(userId);
        baseMapper.insert(share);
        return share;
    }

    public Map<String, Object> access(String code, String password) {
        LambdaQueryWrapper<DfsShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsShare::getCode, code);
        DfsShare share = baseMapper.selectOne(wrapper);
        if (share == null) {
            throw new RuntimeException("分享链接不存在");
        }
        if (share.getStatus() == 0) {
            throw new RuntimeException("分享已失效");
        }
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            share.setStatus(0);
            baseMapper.updateById(share);
            throw new RuntimeException("分享已过期");
        }
        if (share.getPassword() != null && !share.getPassword().isEmpty()) {
            if (password == null || !passwordEncoder.matches(password, share.getPassword())) {
                throw new RuntimeException("访问密码错误");
            }
        }
        share.setVisitCount(share.getVisitCount() + 1);
        baseMapper.updateById(share);

        Map<String, Object> result = new HashMap<>();
        result.put("share", share);
        if (share.getFileId() != null) {
            result.put("file", dfsFileMapper.selectById(share.getFileId()));
        }
        if (share.getFolderId() != null) {
            result.put("folder", dfsFolderMapper.selectById(share.getFolderId()));
        }
        return result;
    }

    public Page<DfsShare> listByUser(Long userId, Page<DfsShare> page) {
        LambdaQueryWrapper<DfsShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DfsShare::getCreateBy, userId);
        wrapper.orderByDesc(DfsShare::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void cancel(Long id, Long userId) {
        DfsShare share = baseMapper.selectById(id);
        if (share == null) {
            throw new RuntimeException("分享不存在");
        }
        if (!share.getCreateBy().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        baseMapper.deleteById(id);
    }
}
